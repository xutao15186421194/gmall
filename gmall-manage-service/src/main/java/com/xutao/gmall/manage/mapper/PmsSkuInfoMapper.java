package com.xutao.gmall.manage.mapper;

import com.xutao.gmall.bean.PmsSkuInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface PmsSkuInfoMapper extends Mapper<PmsSkuInfo> {
    /**
     * 自定义sql
     * @param productId
     * @return
     */
    List<PmsSkuInfo> selectgetSkuSaleAttrValueListBySpu(String productId);
}
