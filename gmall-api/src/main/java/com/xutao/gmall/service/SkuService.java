package com.xutao.gmall.service;

import com.xutao.gmall.bean.PmsSkuInfo;

import java.math.BigDecimal;
import java.util.List;

public interface SkuService {
    /**
     * 添加sku商品
     * @param pmsSkuInfo
     */
    void saveSkuInfo(PmsSkuInfo pmsSkuInfo);

    /**
     * 查询sku商品
     * @param skuId
     * @return
     */
    PmsSkuInfo getSkuById(String skuId);

    /**
     * 查询当前sku的spu的其他sku的集合的hash表
     * @param productId
     * @return
     */
    List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId);

    List<PmsSkuInfo> getAllSku();

    /**
     *
     * @param productSkuId
     * @param price
     * @return
     */
    boolean checkPrice(String productSkuId, BigDecimal price);
}
