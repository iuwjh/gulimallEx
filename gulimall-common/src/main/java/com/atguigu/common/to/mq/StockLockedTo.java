package com.atguigu.common.to.mq;

import lombok.Data;

import java.util.List;

@Data
public class StockLockedTo {
    Long id;
    StockDetailTo detail;
}
