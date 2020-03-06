package com.xutao.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.xutao.gmall.bean.PmsBaseSaleAttr;
import com.xutao.gmall.bean.PmsProductImage;
import com.xutao.gmall.bean.PmsProductInfo;
import com.xutao.gmall.bean.PmsProductSaleAttr;
import com.xutao.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@CrossOrigin
public class SpuController {

    @Reference
    SpuService spuServiceImpl;

    /**
     * 商品查询，spu查询
     * @param catalog3Id
     * @return
     */
    @RequestMapping("spuList")
    @ResponseBody
    public List<PmsProductInfo> spuList(String catalog3Id){

        return spuServiceImpl.spuList(catalog3Id);
    }

    /**
     * 查询销售属性名称
     * @return
     */
    @RequestMapping("baseSaleAttrList")
    @ResponseBody
    public List<PmsBaseSaleAttr> baseSaleAttrList(){
        return spuServiceImpl.baseSaleAttrList();
    }

    /**
     * 添加商品以及商品属性及属性值
     * @param pmsProductInfo
     * @return
     */
    @RequestMapping("saveSpuInfo")
    @ResponseBody
    public String saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo){
        return spuServiceImpl.saveSpuInfo(pmsProductInfo);
    }


    /**
     * 查询商品自定义属性值
     * @param spuId
     * @return
     */
    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId){

        return spuServiceImpl.spuSaleAttrList(spuId);
    }

    @RequestMapping("spuImageList")
    @ResponseBody
    public List<PmsProductImage> spuImageList(String spuId){
        return spuServiceImpl.spuImageList(spuId);
    }
}
