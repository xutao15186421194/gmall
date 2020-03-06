package com.xutao.gmall.service;

import com.xutao.gmall.bean.PmsBaseSaleAttr;
import com.xutao.gmall.bean.PmsProductImage;
import com.xutao.gmall.bean.PmsProductInfo;
import com.xutao.gmall.bean.PmsProductSaleAttr;

import java.util.List;

public interface SpuService {
    /**
     * 查询商品spu
     * @param catalog3Id
     * @return
     */
    List<PmsProductInfo> spuList(String catalog3Id);

    /**
     * 查询销售属性名称
     * @return
     */
    List<PmsBaseSaleAttr> baseSaleAttrList();

    /**
     * 添加商品以及商品属性值
     * @param pmsProductInfo
     * @return
     */
    String saveSpuInfo(PmsProductInfo pmsProductInfo);

    /**
     * 查询属性值
     * @param spuId
     * @return
     */
    List<PmsProductSaleAttr> spuSaleAttrList(String spuId);

    /**
     * 查询图片
     * @param spuId
     * @return
     */
    List<PmsProductImage> spuImageList(String spuId);

    /**
     * 查询销售属性
     * @param productId
     * @param skuId
     * @return
     */
    List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String productId, String skuId);
}
