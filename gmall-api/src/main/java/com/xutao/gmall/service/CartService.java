package com.xutao.gmall.service;

import com.xutao.gmall.bean.OmsCartItem;

import java.util.List;

public interface CartService {

    /**
     * 查询数据库中购物车中的数据
     * @param memberId
     * @param skuId
     * @return
     */
    OmsCartItem ifCartExistByUser(String memberId, String skuId);

    /**
     * 添加数据到购物车
     * @param omsCartItem
     */
    void addCart(OmsCartItem omsCartItem);

    /**
     * 修改购物车数据
     * @param omsCartItemFromDb
     */
    void updateCart(OmsCartItem omsCartItemFromDb);

    /**
     * 同步缓存数据
     * @param memberId
     */
    void flushCartCache(String memberId);

    /**
     * 根据用户id查询购物车的所有数据
     * @param memberId
     * @return
     */
    List<OmsCartItem> cartList(String memberId);

    /**
     * 修改商品被选中的状态
     * @param omsCartItem
     */
    void checkCart(OmsCartItem omsCartItem);

    /**
     * 订单生成成功后删除购物车中的数据
     * @param productSkuId
     * @param memberId
     */
    void delCart(String productSkuId, String memberId);
}
