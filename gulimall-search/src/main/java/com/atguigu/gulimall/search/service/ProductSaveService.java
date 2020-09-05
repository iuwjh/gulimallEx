package com.atguigu.gulimall.search.service;

import com.atguigu.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

public interface ProductSaveService {
    boolean productStartUp(List<SkuEsModel> skuEsModels) throws IOException;
}
