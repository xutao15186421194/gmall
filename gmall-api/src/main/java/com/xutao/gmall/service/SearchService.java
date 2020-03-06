package com.xutao.gmall.service;

import com.xutao.gmall.bean.PmsSearchParam;
import com.xutao.gmall.bean.PmsSearchSkuInfo;

import java.util.List;

/**
 * 全文解锁，搜索
 */
public interface SearchService {

    /**
     * 调用搜索功能，返回搜索结果
     * @param pmsSearchParam
     * @return
     */
    List<PmsSearchSkuInfo> list(PmsSearchParam pmsSearchParam);
}
