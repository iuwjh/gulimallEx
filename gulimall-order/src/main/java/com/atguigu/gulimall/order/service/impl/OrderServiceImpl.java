package com.atguigu.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.to.SeckillOrderTo;
import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.order.constant.OrderConstant;
import com.atguigu.gulimall.order.constant.OrderSubmitStatus;
import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.dao.OrderItemDao;
import com.atguigu.gulimall.order.dao.OrderRedisDao;
import com.atguigu.gulimall.order.dao.amqp.OrderRabbitSender;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.entity.PaymentInfoEntity;
import com.atguigu.gulimall.order.enume.OrderStatusEnum;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.ProductFeignService;
import com.atguigu.gulimall.order.feign.WareFeignService;
import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.service.PaymentInfoService;
import com.atguigu.gulimall.order.to.OrderCreateTo;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;


@Service("orderService")
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private static final ThreadLocal<OrderSubmitVo> orderSubmitVoThreadLocal = new ThreadLocal<OrderSubmitVo>();

    private final PaymentInfoService paymentInfoService;

    private final OrderItemService orderItemService;

    private final OrderItemDao orderItemDao;

    private final MemberFeignService memberFeignService;

    private final CartFeignService cartFeignService;

    private final ThreadPoolExecutor executor;

    private final WareFeignService wareFeignService;

    private final ProductFeignService productFeignService;

    private final OrderDao orderDao;

    private final OrderRedisDao orderRedisDao;

    private final OrderRabbitSender orderRabbitSender;

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
            List<OrderItemVo> orderItemVos = cartFeignService.currentUserCartCheckedItems();
            confirmVo.setItems(orderItemVos);
        }, executor).thenRunAsync(() -> {
            if (confirmVo.getItems() != null) {
                List<Long> skuIds = confirmVo.getItems().stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
                R r = wareFeignService.getSkusHasStock(skuIds);
                List<SkuStockVo> data = r.getData(new TypeReference<List<SkuStockVo>>() {});
                if (data != null) {
                    Map<Long, Boolean> stocks = data.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                    confirmVo.setStocks(stocks);
                }
            }
        }, executor);


        Integer integration = memberRespVo.getIntegration();
        confirmVo.setIntegration(integration);

        String token = UUID.randomUUID().toString().replace("_", "");
        // 设置令牌，使得当前结算页信息30分钟内有效
        orderRedisDao.setOrderToken(memberRespVo.getId(), token, Duration.ofMinutes(30));
        confirmVo.setOrderToken(token);

        CompletableFuture.allOf(getAddressFuture, cartFuture).get();

        return confirmVo;
    }

    // @GlobalTransactional
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo submitVo) throws NoStockException {
        orderSubmitVoThreadLocal.set(submitVo);
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo().setStatus(OrderSubmitStatus.OK);
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUserThreadLocal.get();
        String orderToken = submitVo.getOrderToken();
        // 确认当前令牌有效后移除该令牌，以防止重复提交
        boolean result = orderRedisDao.removeOrderTokenIfExist(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId(), orderToken);

        if (!result) {
            responseVo.setStatus(OrderSubmitStatus.INFO_OUTDATED);
            return responseVo;
        } else {
            OrderCreateTo order = createOrder();
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = submitVo.getPayPrice();
            if (payAmount.subtract(payPrice).abs().doubleValue() < 0.01) {
                // 持久化订单
                order.getOrder().setModifyTime(new Date());
                orderDao.insert(order.getOrder());

                // 持久化订单项信息
                List<OrderItemEntity> orderItems = order.getOrderItems();
                orderItemService.saveBatch(orderItems);

                WareSkuLockVo lockVo = new WareSkuLockVo().setOrderSn(order.getOrder().getOrderSn());

                List<OrderItemVo> itemsToLock = order.getOrderItems().stream().map((item) ->
                    new OrderItemVo()
                        .setSkuId(item.getSkuId())
                        .setCount(item.getSkuQuantity())
                        .setTitle(item.getSkuName())).collect(Collectors.toList());
                lockVo.setItemsToLock(itemsToLock);
                R r = wareFeignService.orderLockStock(lockVo);
                if (r.getCode() == 0) {
                    // int a = 10/0;
                    responseVo.setOrder(order.getOrder());

                    orderRabbitSender.createOrder(order.getOrder());
                    System.out.println("MQ: 订单提交成功 " + order.getOrder());
                } else {
                    // throw new NoStockException((String) r.get("msg"));
                    responseVo.setStatus(OrderSubmitStatus.INSUFFICIENT_STOCK);
                }
            } else {
                responseVo.setStatus(OrderSubmitStatus.PRICE_CHANGED);
            }
        }
        return responseVo;
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        OrderEntity orderEntity = orderDao.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        return orderEntity;
    }

    @Override
    public void closeOrder(OrderEntity orderEntity) {
        OrderEntity byId = orderDao.selectById(orderEntity.getId());
        if (byId.getStatus().equals(OrderStatusEnum.CREATE_NEW.getCode())) {
            OrderEntity update = new OrderEntity();
            update.setId(orderEntity.getId());
            update.setStatus(OrderStatusEnum.CANCLED.getCode());
            orderDao.updateById(update);

            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity, orderTo);

            orderRabbitSender.closeOrder(orderTo);
        }
    }

    @Override
    public PayVo getOrderPay(String orderSn) {
        PayVo payVo = new PayVo();
        OrderEntity order = getOrderByOrderSn(orderSn);
        BigDecimal scaled = order.getPayAmount().setScale(2, RoundingMode.UP);
        payVo.setTotal_amount(scaled.toString());
        payVo.setOut_trade_no(order.getOrderSn());

        List<OrderItemEntity> itemEntities = orderItemDao.selectList(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));
        OrderItemEntity itemEntity = itemEntities.get(0);

        payVo.setSubject(itemEntity.getSkuName());
        payVo.setSubject(itemEntity.getSkuAttrsVals());
        return payVo;
    }

    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUserThreadLocal.get();
        IPage<OrderEntity> page = orderDao.selectPage(
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
        PaymentInfoEntity infoEntity = new PaymentInfoEntity();

        infoEntity.setAlipayTradeNo(vo.getTrade_no());
        infoEntity.setOrderSn(vo.getOut_trade_no());
        infoEntity.setPaymentStatus(vo.getTrade_status());
        infoEntity.setCallbackTime(vo.getNotify_time());

        paymentInfoService.save(infoEntity);

        if (vo.getTrade_status().equals("TRADE_SUCCESS") || vo.getTrade_status().equals("TRADE_FINISHED")) {
            String outTradeNo = vo.getOut_trade_no();
            orderDao.updateOrderStatus(outTradeNo, OrderStatusEnum.PAYED.getCode());
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
        orderDao.insert(orderEntity);

        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderSn(orderTo.getOrderSn());
        orderItemEntity.setRealAmount(multiply);
        orderItemEntity.setSkuQuantity(orderTo.getNum());

        orderItemService.save(orderItemEntity);
    }

    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        orderDao.insert(orderEntity);

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
        List<OrderItemVo> userCartItems = cartFeignService.currentUserCartCheckedItems();
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
        SpuInfoVo spuInfoVo = r.getData(new TypeReference<SpuInfoVo>() {});
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

        OrderSubmitVo orderSubmitVo = orderSubmitVoThreadLocal.get();

        R r = wareFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo fareResp = r.getData(new TypeReference<FareVo>() {});

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
