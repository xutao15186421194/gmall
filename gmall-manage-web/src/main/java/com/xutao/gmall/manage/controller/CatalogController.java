package com.xutao.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.xutao.gmall.bean.PmsBaseCatalog1;
import com.xutao.gmall.bean.PmsBaseCatalog2;
import com.xutao.gmall.bean.PmsBaseCatalog3;
import com.xutao.gmall.service.CatalogService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@CrossOrigin//允许跨域请求
public class CatalogController {

    @Reference
    CatalogService catalogServiceImpl;

    /**
     * 查询一级分类
     * @return
     */
    @RequestMapping("getCatalog1")
    @ResponseBody
    public List<PmsBaseCatalog1> getCatalog1(){

        List<PmsBaseCatalog1> catalog1 = catalogServiceImpl.getCatalog1();
        return catalog1;
    }

    /**
     * 根据一级分类查询二级分类
     * @return
     */
    @RequestMapping("getCatalog2")
    @ResponseBody
    public List<PmsBaseCatalog2> getCatalog2(String catalog1Id){

        return catalogServiceImpl.getCatalog2(catalog1Id);
    }

    /**
     *根据二级分类查询三级分类
     * @param catalog2Id
     * @return
     */
    @RequestMapping("getCatalog3")
    @ResponseBody
    public List<PmsBaseCatalog3> getCatalog3(String catalog2Id){
        return catalogServiceImpl.getCatalog3(catalog2Id);
    }
}
