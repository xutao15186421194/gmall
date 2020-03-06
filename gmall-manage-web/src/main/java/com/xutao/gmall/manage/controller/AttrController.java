package com.xutao.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.xutao.gmall.bean.PmsBaseAttrInfo;
import com.xutao.gmall.bean.PmsBaseAttrValue;
import com.xutao.gmall.service.AttrService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@CrossOrigin
public class AttrController {

    @Reference
    AttrService atterServiceImpl;

    /**
     * 根据三级分类查询查询商品属性
     * @param catalog3Id
     * @return
     */
    @RequestMapping("attrInfoList")
    @ResponseBody
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id){
        return atterServiceImpl.attrInfoList(catalog3Id);
    }

    /**
     * 添加和修改属性和属性值
     * @param pmsBaseAttrInfo
     * @return
      */
    @RequestMapping("saveAttrInfo")
    @ResponseBody
    public String saveAttrInfo(@RequestBody PmsBaseAttrInfo pmsBaseAttrInfo){

        String success = atterServiceImpl.saveAttrInfo(pmsBaseAttrInfo);
        return "success";
    }

    @RequestMapping("getAttrValueList")
    @ResponseBody
    public List<PmsBaseAttrValue> getAttrValueList(String attrId){

        List<PmsBaseAttrValue> pmsBaseAttrValues = atterServiceImpl.getAttrValueList(attrId);
        return pmsBaseAttrValues;
    }
}
