package com.atguigu.gulimall.order.constant;

public enum OrderSubmitStatus {
    OK(0,"订单提交成功"),
    INFO_OUTDATED(1,"订单信息过期，请刷新后再提交"),
    PRICE_CHANGED(2,"订单商品价格发生变化，请确认后再提交"),
    INSUFFICIENT_STOCK(3,"锁库存失败，商品库存不足");

    private final int code;
    private final String msg;

    OrderSubmitStatus(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
