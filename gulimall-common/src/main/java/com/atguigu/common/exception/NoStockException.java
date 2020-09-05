package com.atguigu.common.exception;

public class NoStockException extends RuntimeException {
    private Long skuId = null;


    public NoStockException(Long skuId) {
        super("商品id:"+skuId+"； 没有足够库存");
        this.skuId = skuId;
    }

    public NoStockException(String msg) {
        super(msg);
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
}
