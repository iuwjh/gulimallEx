package com.atguigu.gulimall.order.service;

import com.atguigu.common.to.SeckillOrderTo;
import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.dao.OrderItemDao;
import com.atguigu.gulimall.order.dao.OrderRedisDao;
import com.atguigu.gulimall.order.dao.amqp.OrderRabbitSender;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.enume.OrderStatusEnum;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.ProductFeignService;
import com.atguigu.gulimall.order.feign.WareFeignService;
import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.order.service.impl.OrderServiceImpl;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private PaymentInfoService paymentInfoService;

    @Mock
    private OrderItemService orderItemService;

    @Mock
    private OrderItemDao orderItemDao;

    @Mock
    private MemberFeignService memberFeignService;

    @Mock
    private CartFeignService cartFeignService;

    @Spy
    private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);

    @Mock
    private WareFeignService wareFeignService;

    @Mock
    private ProductFeignService productFeignService;

    @Mock
    private OrderDao orderDao;

    @Mock
    private OrderRedisDao orderRedisDao;

    @Mock
    private OrderRabbitSender orderRabbitSender;

    @InjectMocks
    private OrderServiceImpl orderService;

    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static final OrderSubmitVo ORDER_SUBMIT_VO = new OrderSubmitVo().setAddrId(11L).setPayPrice(BigDecimal.valueOf(900));

    public static final MemberRespVo MEMBER_RESP_VO = new MemberRespVo().setId(9L).setIntegration(3000);

    @BeforeEach
    void setup() {
        // orderService = Mockito.spy(new OrderServiceImpl());
        LoginUserInterceptor.loginUserThreadLocal.set(MEMBER_RESP_VO);
        ((ThreadLocal<OrderSubmitVo>) ReflectionTestUtils.getField(OrderServiceImpl.class, "orderSubmitVoThreadLocal"))
            .set(ORDER_SUBMIT_VO);
    }

    @Test
    void confirmOrder() throws Exception {
        final ValueOperations valueOperations = Mockito.mock(ValueOperations.class);
        final MemberAddressVo memberAddressVo = new MemberAddressVo().setId(33L);
        final OrderItemVo orderItemVo = new OrderItemVo().setTitle("item1");
        Mockito.when(wareFeignService.getSkusHasStock(any())).thenReturn(
            R.ok().setData(singletonList(new SkuStockVo().setSkuId(3L).setHasStock(true))));
        Mockito.when(memberFeignService.getAddress(MEMBER_RESP_VO.getId())).thenReturn(singletonList(memberAddressVo));
        Mockito.when(cartFeignService.currentUserCartCheckedItems()).thenReturn(singletonList(orderItemVo));

        final OrderConfirmVo result = orderService.confirmOrder();
        assertThat(result).isNotNull();
        assertThat(result.getIntegration()).isNotNull().isEqualTo(MEMBER_RESP_VO.getIntegration());
        assertThat(result.getItems()).hasSize(1).isEqualTo(singletonList(orderItemVo));
        assertThat(result.getAddress()).hasSize(1).isEqualTo(singletonList(memberAddressVo));
    }

    @Test
    void submitOrder() throws Exception {
        final OrderItemVo orderItemVo = new OrderItemVo().setSkuId(4L).setPrice(BigDecimal.valueOf(300)).setCount(3);
        Mockito.when(wareFeignService.getFare(ORDER_SUBMIT_VO.getAddrId()))
            .thenReturn(R.ok().setData(new FareVo().setAddress(new MemberAddressVo()).setFare(BigDecimal.valueOf(0))));
        Mockito.when(cartFeignService.currentUserCartCheckedItems()).thenReturn(singletonList(orderItemVo));
        Mockito.when(productFeignService.getSpuInfoBySkuId(orderItemVo.getSkuId()))
            .thenReturn(R.ok().setData(new SpuInfoVo().setBrandId(5L)));
        Mockito.when(wareFeignService.orderLockStock(any())).thenReturn(R.ok());
        Mockito.when(orderRedisDao.removeOrderTokenIfExist(any(), any())).thenReturn(true);

        final SubmitOrderResponseVo result = orderService.submitOrder(ORDER_SUBMIT_VO);
        assertThat(result).isNotNull();
    }

    @Test
    void getOrderByOrderSn() throws Exception {
        final String orderSn = "abc123";
        final OrderEntity orderEntity = new OrderEntity().setOrderSn(orderSn);
        Mockito.when(orderDao.selectOne(any())).thenReturn(orderEntity);

        final OrderEntity result = orderService.getOrderByOrderSn(orderSn);
        assertThat(result).isNotNull().isEqualTo(orderEntity);
    }

    @Test
    void closeOrder() throws Exception {
        final OrderEntity orderEntity = new OrderEntity().setId(7L).setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        Mockito.when(orderDao.selectById(orderEntity.getId())).thenReturn(orderEntity);

        orderService.closeOrder(orderEntity);
        Mockito.verify(orderRabbitSender, Mockito.times(1))
            .closeOrder(objectMapper.convertValue(orderEntity, OrderTo.class));
    }

    @Test
    void getOrderPay() throws Exception {
        final String orderSn = "bbb333";
        Mockito.when(orderDao.selectOne(any())).thenReturn(new OrderEntity().setPayAmount(BigDecimal.valueOf(400)));
        Mockito.when(orderItemDao.selectList(any())).thenReturn(singletonList(new OrderItemEntity()));

        orderService.getOrderPay(orderSn);
    }

    @Test
    void queryPageWithItem() throws Exception {
        Mockito.when(orderDao.selectPage(any(), any())).thenReturn(new Page<OrderEntity>().setRecords(singletonList(new OrderEntity())));

        final HashMap<String, Object> params = new HashMap<>();
        orderService.queryPageWithItem(params);
    }

    @Test
    void handlePayResult() throws Exception {

        final PayAsyncVo payAsyncVo = new PayAsyncVo().setTrade_status("TRADE_SUCCESS");
        orderService.handlePayResult(payAsyncVo);
    }

    @Test
    void createSeckillOrder() throws Exception {

        final SeckillOrderTo seckillOrderTo = new SeckillOrderTo().setSeckillPrice(BigDecimal.valueOf(300)).setNum(5);
        orderService.createSeckillOrder(seckillOrderTo);
    }
}
