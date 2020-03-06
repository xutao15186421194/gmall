package com.xutao.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.xutao.gmall.annotations.LoginRequired;
import com.xutao.gmall.bean.*;
import com.xutao.gmall.service.AttrService;
import com.xutao.gmall.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
public class SearchController {

    @Reference
    SearchService searchService;

    @Reference
    AttrService attrService;

    @RequestMapping("list.html")
    @LoginRequired(logginSuccess = false)
    public String list(PmsSearchParam pmsSearchParam, ModelMap modelMap, HttpServletRequest request, HttpSession session) {//三级分类，关键字搜索
        System.out.println((String)request.getAttribute("nickname"));
        System.out.println(session.getAttribute("nickname"));
        //调用搜索服务，返回搜索结果
        if(StringUtils.isBlank(pmsSearchParam.getKeyword())&&StringUtils.isBlank(pmsSearchParam.getCatalog3Id())){
            pmsSearchParam.setKeyword("无");
        }
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = searchService.list(pmsSearchParam);
        modelMap.put("skuLsInfoList", pmsSearchSkuInfos);
        // 抽取检索结果锁包含的平台属性集合
        Set<String> valueIdSet = new HashSet<>();
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
            List<PmsSkuAttrValue> pmsSkuAttrValues = pmsSearchSkuInfo.getSkuAttrValueList();
            for (PmsSkuAttrValue pmsSkuAttrValue : pmsSkuAttrValues) {
                String valueId = pmsSkuAttrValue.getValueId();
                valueIdSet.add(valueId);
            }
        }
        //根据属性valueId将属性列表查询出来
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = new ArrayList<>();
        if(valueIdSet != null && valueIdSet.size()>0){
            pmsBaseAttrInfos = attrService.getAttrValueListByValueId(valueIdSet);
        }

        modelMap.put("attrList", pmsBaseAttrInfos);
        //对平台属性进一步处理，去掉当前条件中的valueId所有在属性组
        String[] delValueIds = pmsSearchParam.getValueId();
        if(delValueIds != null){
            //面包屑
            List<PmsSearchCrumb> pmsSearchCrumbs = new ArrayList<>();
            for (String delValueId : delValueIds) {
                Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfos.iterator();
                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
                //生成面包屑的参数
                pmsSearchCrumb.setValueId(delValueId);
                pmsSearchCrumb.setUrlParam(getUrlParamForCrumb(pmsSearchParam, delValueId));
                while(iterator.hasNext()){
                    PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();
                    List<PmsBaseAttrValue> pmsBaseAttrValues = pmsBaseAttrInfo.getAttrValueList();
                    for (PmsBaseAttrValue pmsBaseAttrValue : pmsBaseAttrValues) {
                        String valueId = pmsBaseAttrValue.getId();
                        if(delValueId.equals(valueId)){
                            //查找面面包屑的属性名称
                            pmsSearchCrumb.setValueName(pmsBaseAttrValue.getValueName());
                            //删除该属性值所在的属组
                            iterator.remove();
                        }
                    }
                }
                pmsSearchCrumbs.add(pmsSearchCrumb);
            }
            modelMap.put("attrValueSelectedList",pmsSearchCrumbs);
        }
        String urlParam = getUrlParam(pmsSearchParam);
        modelMap.put("urlParam",urlParam);
        String keyword = pmsSearchParam.getKeyword();
        if(StringUtils.isNotBlank(keyword)){
            modelMap.put("keyword",keyword);
        }
        return "list";
    }

    private String getUrlParam(PmsSearchParam pmsSearchParam) {
        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String[] skuAttrValueList = pmsSearchParam.getValueId();

        String urlParam = "";

        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
        }

        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }

        if (skuAttrValueList != null) {

            for (String pmsSkuAttrValue : skuAttrValueList) {
                urlParam = urlParam + "&valueId=" + pmsSkuAttrValue;
            }
        }

        return urlParam;
    }

    private String getUrlParamForCrumb(PmsSearchParam pmsSearchParam, String delValueId) {
        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String[] skuAttrValueList = pmsSearchParam.getValueId();
        String urlParam = "";
        if(StringUtils.isNotBlank(keyword)){
            if(StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
        }
        if(StringUtils.isNotBlank(catalog3Id)){
            if(StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }
        if(skuAttrValueList != null){
            for(String skuAttrValue : skuAttrValueList){
                if(!skuAttrValue.equals(delValueId)){
                    urlParam = urlParam + "&valueId=" + skuAttrValue;
                }
            }
        }

        return urlParam;
    }

    /**
     * 访问index页面
     *
     * @return
     */
    @RequestMapping("index")
    @LoginRequired(logginSuccess = false)
    public String index(HttpServletRequest request,HttpSession session) {
        System.out.println((String)request.getAttribute("nickname"));
        System.out.println(session.getAttribute("nickname"));
        return "index";
    }
}
