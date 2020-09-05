package com.atguigu.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.to.SeckillOrderTo;
import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.order.constant.OrderConstant;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.entity.PaymentInfoEntity;
import com.atguigu.gulimall.order.enume.OrderStatusEnum;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.ProductFeignService;
import com.atguigu.gulimall.order.feign.WareFeignService;
import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.atguigu.gulimall.order.service.PaymentInfoService;
import com.atguigu.gulimall.order.to.OrderCreateTo;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> confirmVoThreadLocal = new ThreadLocal<OrderSubmitVo>();

    @Autowired
    PaymentInfoService paymentInfoService;
    @Autowired
    OrderItemService orderItemService;
    @Autowired
    MemberFeignService memberFeignService;
    @Autowired
    CartFeignService cartFeignService;
    @Autowired
    ThreadPoolExecutor executor;
    @Autowired
    WareFeignService wareFeignService;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUserThreadLocal.get();

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> address = memberFeignService.getAddress(memberRespVo.getId());
            confirmVo.setAddress(address);

        }, executor);

        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> orderItemVos = cartFeignService.currentUserCartItems();
            confirmVo.setItems(orderItemVos);
        }, executor).thenRunAsync(() -> {
            if (confirmVo.getItems() != null) {
                List<Long> skuIds = confirmVo.getItems().stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
                R r = wareFeignService.getSkusHasStock(skuIds);
                List<SkuStockVo> data = r.getData(new TypeReference<List<SkuStockVo>>() {
                });
                if (data != null) {
                    Map<Long, Boolean> stocks = data.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                    confirmVo.setStocks(stocks);
                }
            }
        }, executor);


        Integer integration = memberRespVo.getIntegration();
        confirmVo.setIntegration(integration);

        String token = UUID.randomUUID().toString().replace("_", "");
        stringRedisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId(), token, 30, TimeUnit.MINUTES);
        confirmVo.setOrderToken(token);

        CompletableFuture.allOf(getAddressFuture, cartFuture).get();

        return confirmVo;
    }

    // @GlobalTransactional
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) throws NoStockException {
        confirmVoThreadLocal.set(vo);
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        responseVo.setCode(0);
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUserThreadLocal.get();
        String delScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String orderToken = vo.getOrderToken();
        Long result = stringRedisTemplate.execute(new DefaultRedisScript<Long>(delScript, Long.class),
                Collections.singletonList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId()), orderToken);
        if (result == null || result == 0L) {
            responseVo.setCode(1);
            return responseVo;
        } else {
            OrderCreateTo order = createOrder();
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            if (payAmount.subtract(payPrice).abs().doubleValue() < 0.01) {
                saveOrder(order);

                WareSkuLockVo lockVo = new WareSkuLockVo();
                lockVo.setOrderSn(order.getOrder().getOrderSn());

                List<OrderItemVo> locks = order.getOrderItems().stream().map((item) -> {
                    OrderItemVo itemVo = new OrderItemVo();
                    itemVo.setSkuId(item.getSkuId());
                    itemVo.setCount(item.getSkuQuantity());
                    itemVo.setTitle(item.getSkuName());
                    return itemVo;
                }).collect(Collectors.toList());
                lockVo.setLocks(locks);
                R r = wareFeignService.orderLockStock(lockVo);
                if (r.getCode() == 0) {
                    // int a = 10/0;
                    responseVo.setOrder(order.getOrder());

                    rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order.getOrder());
                    System.out.println("MQ: 订单提交成功 " + order.getOrder());
                } else {
                    throw new NoStockException((String) r.get("msg"));
                    // responseVo.setCode(3);
                }
            } else {
                responseVo.setCode(2);
            }
        }
        return responseVo;
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        OrderEntity orderEntity = getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        return orderEntity;
    }

    @Override
    public void closeOrder(OrderEntity orderEntity) {
        OrderEntity byId = getById(orderEntity.getId());
        if (byId.getStatus().equals(OrderStatusEnum.CREATE_NEW.getCode())) {
            OrderEntity update = new OrderEntity();
            update.setId(orderEntity.getId());
            update.setStatus(OrderStatusEnum.CANCLED.getCode());
            updateById(update);

            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity, orderTo);

            rabbitTemplate.convertAndSend("order-event-exchange",
                    "order.release.other", orderTo);
        }
    }

    @Override
    public PayVo getOrderPay(String orderSn) {
        PayVo payVo = new PayVo();
        OrderEntity order = getOrderByOrderSn(orderSn);
        BigDecimal scaled = order.getPayAmount().setScale(2, RoundingMode.UP);
        payVo.setTotal_amount(scaled.toString());
        payVo.setOut_trade_no(order.getOrderSn());

        List<OrderItemEntity> itemEntities = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));
        OrderItemEntity itemEntity = itemEntities.get(0);

        payVo.setSubject(itemEntity.getSkuName());
        payVo.setSubject(itemEntity.getSkuAttrsVals());
        return payVo;
    }

    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUserThreadLocal.get();
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>().eq("member_id", memberRespVo.getId()).orderByDesc("id")
        );

        List<OrderEntity> orderEntities = page.getRecords().stream().peek((order) -> {
            List<OrderItemEntity> itemEntities = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", order.getOrderSn()));
            order.setItemEntities(itemEntities);
        }).collect(Collectors.toList());

        page.setRecords(orderEntities);

        return new PageUtils(page);
    }

    @Override
    public String handlePayResult(PayAsyncVo vo) {
        PaymentInfoEntity infoEntity=new PaymentInfoEntity();

        infoEntity.setAlipayTradeNo(vo.getTrade_no());
        infoEntity.setOrderSn(vo.getOut_trade_no());
        infoEntity.setPaymentStatus(vo.getTrade_status());
        infoEntity.setCallbackTime(vo.getNotify_time());

        paymentInfoService.save(infoEntity);

        if (vo.getTrade_status().equals("TRADE_SUCCESS")||vo.getTrade_status().equals("TRADE_FINISHED")) {
            String outTradeNo = vo.getOut_trade_no();
            baseMapper.updateOrderStatus(outTradeNo,OrderStatusEnum.PAYED.getCode());
        }

        return "success";
    }

    @Override
    public void createSeckillOrder(SeckillOrderTo orderTo) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderTo.getOrderSn());
        orderEntity.setMemberId(orderTo.getMemberId());
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());

        BigDecimal multiply = orderTo.getSeckillPrice().multiply(BigDecimal.valueOf(orderTo.getNum()));
        orderEntity.setPayAmount(multiply);
        save(orderEntity);

        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderSn(orderTo.getOrderSn());
        orderItemEntity.setRealAmount(multiply);
        orderItemEntity.setSkuQuantity(orderTo.getNum());

        orderItemService.save(orderItemEntity);
    }

    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        save(orderEntity);

        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);
    }

    private OrderCreateTo createOrder() {
        OrderCreateTo newOrder = new OrderCreateTo();
        String orderSn = IdWorker.getTimeId();
        OrderEntity orderEntity = buildOrder(orderSn);
        newOrder.setOrder(orderEntity);

        List<OrderItemEntity> itemEntities = buildOrderItems(orderSn);
        newOrder.setOrderItems(itemEntities);

        computePrice(orderEntity, itemEntities);

        return newOrder;
    }

    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> itemEntities) {

        BigDecimal total = BigDecimal.valueOf(0);
        BigDecimal coupon = BigDecimal.valueOf(0);
        BigDecimal integration = BigDecimal.valueOf(0);
        BigDecimal promotion = BigDecimal.valueOf(0);
        BigDecimal gift = BigDecimal.valueOf(0);
        BigDecimal growth = BigDecimal.valueOf(0);
        for (OrderItemEntity itemEntity : itemEntities) {
            total = total.add(itemEntity.getRealAmount());
            coupon = coupon.add(itemEntity.getCouponAmount());
            integration = integration.add(itemEntity.getIntegrationAmount());
            promotion = promotion.add(itemEntity.getPromotionAmount());
            gift = gift.add(BigDecimal.valueOf(itemEntity.getGiftIntegration()));
            growth = growth.add(BigDecimal.valueOf(itemEntity.getGiftGrowth()));
        }
        orderEntity.setTotalAmount(total);
        orderEntity.setCouponAmount(coupon);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setPromotionAmount(promotion);

        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));

        orderEntity.setIntegration(gift.intValue());
        orderEntity.setGrowth(growth.intValue());
        orderEntity.setDeleteStatus(0);

    }

    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        List<OrderItemVo> userCartItems = cartFeignService.currentUserCartItems();
        if (userCartItems != null && userCartItems.size() > 0) {
            return userCartItems.stream()
                    .map(this::buildOrderItem)
                    .peek((item) -> item.setOrderSn(orderSn))
                    .collect(Collectors.toList());
        }
        return null;
    }

    private OrderItemEntity buildOrderItem(OrderItemVo item) {
        OrderItemEntity itemEntity = new OrderItemEntity();

        Long skuId = item.getSkuId();
        R r = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo spuInfoVo = r.getData(new TypeReference<SpuInfoVo>() {
        });
        itemEntity.setSpuId(spuInfoVo.getId());
        itemEntity.setSpuBrand(spuInfoVo.getBrandId().toString());
        itemEntity.setSpuName(spuInfoVo.getSpuName());
        itemEntity.setCategoryId(spuInfoVo.getCatalogId());

        itemEntity.setSkuId(item.getSkuId());
        itemEntity.setSkuName(item.getTitle());
        itemEntity.setSkuPic(item.getImage());
        itemEntity.setSkuPrice(item.getPrice());
        String skuAttr = StringUtils.collectionToDelimitedString(item.getSkuAttr(), ";");
        itemEntity.setSkuAttrsVals(skuAttr);
        itemEntity.setSkuQuantity(item.getCount());

        itemEntity.setGiftGrowth(item.getPrice().multiply(BigDecimal.valueOf(item.getCount())).intValue());
        itemEntity.setGiftIntegration(item.getPrice().multiply(BigDecimal.valueOf(item.getCount())).intValue());

        itemEntity.setPromotionAmount(BigDecimal.valueOf(0));
        itemEntity.setCouponAmount(BigDecimal.valueOf(0));
        itemEntity.setIntegrationAmount(BigDecimal.valueOf(0));
        BigDecimal originalAmount = itemEntity.getSkuPrice()
                .multiply(BigDecimal.valueOf(itemEntity.getSkuQuantity()));


        BigDecimal realAmount = originalAmount.subtract(itemEntity.getPromotionAmount())
                .subtract(itemEntity.getCouponAmount())
                .subtract(itemEntity.getIntegrationAmount());
        itemEntity.setRealAmount(realAmount);

        return itemEntity;
    }

    private OrderEntity buildOrder(String orderSn) {
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUserThreadLocal.get();
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderSn);
        orderEntity.setMemberId(memberRespVo.getId());

        OrderSubmitVo orderSubmitVo = confirmVoThreadLocal.get();

        R r = wareFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo fareResp = r.getData(new TypeReference<FareVo>() {
        });

        orderEntity.setFreightAmount(fareResp.getFare());

        orderEntity.setReceiverName(fareResp.getAddress().getName());
        orderEntity.setReceiverRegion(fareResp.getAddress().getRegion());
        orderEntity.setReceiverDetailAddress(fareResp.getAddress().getDetailAddress());
        orderEntity.setReceiverPhone(fareResp.getAddress().getPhone());
        orderEntity.setReceiverProvince(fareResp.getAddress().getProvince());
        orderEntity.setReceiverCity(fareResp.getAddress().getCity());
        orderEntity.setReceiverPostCode(fareResp.getAddress().getPostCode());

        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setAutoConfirmDay(7);

        return orderEntity;
    }

}
