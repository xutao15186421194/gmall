package com.xutao.gmall.service;

import com.xutao.gmall.bean.PaymentInfo;

import java.util.Map;

public interface PaymentService {
    /*
    保存支付信息
     */
    void savePaymentInfo(PaymentInfo paymentInfo);

    /**
     * 更新用户支付信息
     * @param paymentInfo
     */
    void updatePayment(PaymentInfo paymentInfo);

    /**
     * 向消息中间件发送一个检查支付状态(支付服务消费)的延迟消息队列
     * @param outTradeNo
     * @param i
     */
    void sendDelayPaymentResultCheckQueue(String outTradeNo, int i);

    Map<String, Object> checkAlipayPayment(String out_trade_no);
}
