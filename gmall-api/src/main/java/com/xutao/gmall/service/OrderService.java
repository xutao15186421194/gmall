package com.xutao.gmall.service;

import com.xutao.gmall.bean.OmsOrder;

import java.util.List;

public interface OrderService {
    /**
     * 生成交易码，为了在提交订单时做交易码的校验
     * @param memberId
     * @return
     */
    String genTradeCode(String memberId);

    /**
     * 检查交易码
     * @param memberId
     * @param tradeCode
     * @return
     */
    String checkTradeCode(String memberId, String tradeCode);

    /**
     * 生成订单
     * @param omsOrder
     */
    void saveOrder(OmsOrder omsOrder,String memberId);

    /**
     * 根据订单号查询订单信息
     * @param outTradeNo
     * @return
     */
    OmsOrder getOrderByOutTradeNo(String outTradeNo);

    /**
     * 支付成功后通过消息队列更新订单
     * @param omsOrder
     */
    void updateOrder(OmsOrder omsOrder);

    /**
     *
     * @param memberId
     */
    List<OmsOrder> getMemberIdQueryAll(String memberId);
}
