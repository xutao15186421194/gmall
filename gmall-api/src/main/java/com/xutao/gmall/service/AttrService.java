package com.xutao.gmall.service;

import com.xutao.gmall.bean.PmsBaseAttrInfo;
import com.xutao.gmall.bean.PmsBaseAttrValue;

import java.util.List;
import java.util.Set;

public interface AttrService {

    /**
     * 根据三级分类去查询属性值
     * @param catalog3Id
     * @return
     */
    List<PmsBaseAttrInfo> attrInfoList(String catalog3Id);

    /**
     * 添加以及修改属性和属性值
     * @param pmsBaseAttrInfo
     * @return
     */
    String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo);

    /**
     * 根据id查询属性值
     * @param attrId
     * @return
     */
    List<PmsBaseAttrValue> getAttrValueList(String attrId);

    /**
     * 查询属性列表
     * @param valueIdSet
     * @return
     */
    List<PmsBaseAttrInfo> getAttrValueListByValueId(Set<String> valueIdSet);
}
