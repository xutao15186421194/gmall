package com.xutao.gmall.manage.mapper;

import com.xutao.gmall.bean.PmsProductSaleAttr;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface PmsProductSaleAttrMapper extends Mapper<PmsProductSaleAttr> {

    /**
     * 自定义sql查询销售属性
     * @param productId
     * @param skuId
     */
    List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(@Param("productId") String productId,@Param("skuId") String skuId);
}
