package com.xutao.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.AlipayClient;
import com.xutao.gmall.annotations.LoginRequired;
import com.xutao.gmall.bean.OmsOrder;
import com.xutao.gmall.bean.PaymentInfo;
import com.xutao.gmall.payment.config.AlipayConfig;
import com.xutao.gmall.service.OrderService;
import com.xutao.gmall.service.PaymentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {

    @Autowired
    AlipayClient alipayClient;

    @Reference
    OrderService orderService;

    @Autowired
    PaymentService paymentService;

    /**
     * 进入支付页面
     * @param outTradeNo
     * @param totalAmount
     * @param request
     * @param modelMap
     * @return
     */
    @RequestMapping("index")
    @LoginRequired(logginSuccess = true)
    public String index(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap){
        String memberId = (String)request.getAttribute("memberId");
        String nickname = (String)request.getAttribute("nickname");
        modelMap.put("nickname",nickname);
        modelMap.put("outTradeNo",outTradeNo);
        modelMap.put("totalAmount",totalAmount);
        return "index";
    }

    /**
     * 微信支付
     * @param outTradeNo
     * @param totalAmount
     * @param request
     * @param modelMap
     * @return
     */
    @RequestMapping("mx/submit")
    @LoginRequired(logginSuccess = true)
    public String mx(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap){

        return null;
    }

    /**
     * 支付宝支付
     * @param outTradeNo
     * @param totalAmount
     * @param request
     * @param modelMap
     * @return
     */
    @RequestMapping("alipay/submit")
    @LoginRequired(logginSuccess = true)
    @ResponseBody
    public String alipay(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap) {

        // 获得一个支付宝请求的客户端(它并不是一个链接，而是一个封装好的http的表单请求)
        String form = "";
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        //回调参数
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//异步回调地址
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);//回调地址
        Map<String,Object> map = new HashMap<>();
        map.put("out_trade_no",outTradeNo);//订单号
        map.put("product_code","FAST_INSTANT_TRADE_PAY");//销售产品码，与支付宝签约的产品码名称，注：目前仅支持FAST_INSTANT_TRADE_PAY
        map.put("total_amount",totalAmount);//付款金额，订单总金额，单位为元
        map.put("subject","尚硅谷感光徕卡Pro300瞎命名系列手机");//订单标题
        map.put("body","订单描述");//订单描述
        String param = JSON.toJSONString(map);
        alipayRequest.setBizContent(param);
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        // 生成并且保存用户的支付信息
        OmsOrder omsOrder = orderService.getOrderByOutTradeNo(outTradeNo);//根据订单号查询订单信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());//创建时间
        paymentInfo.setOrderId(omsOrder.getId());//订单编号
        paymentInfo.setOrderSn(outTradeNo);//对外业务编号
        paymentInfo.setPaymentStatus("未付款");
        paymentInfo.setSubject("交易内容");//交易内容
        paymentInfo.setTotalAmount(totalAmount);//支付金额
        //保存支付信息
        paymentService.savePaymentInfo(paymentInfo);
        // 向消息中间件发送一个检查支付状态(支付服务消费)的延迟消息队列
        paymentService.sendDelayPaymentResultCheckQueue(outTradeNo,5);
        //提交到支付宝进行支付
        return form;
    }

    /**
     * 支付包支付成功后回调接口
     * @param request
     * @param modelMap
     * @return
     */
    @RequestMapping("alipay/callback/return")
    @LoginRequired(logginSuccess = true)
    public String aliPayCallBackReturn(HttpServletRequest request, ModelMap modelMap){

        //从回调函数中获取支付信息
        String sign = request.getParameter("sign");//签名
        String trade_no = request.getParameter("trade_no");//支付宝交易号
        String out_trade_no = request.getParameter("out_trade_no");//商户订单号
        String total_amount = request.getParameter("total_amount");//交易金额
        String trade_status = request.getParameter("trade_status");//交易状态
        String subject = request.getParameter("subject");
        String call_back_content = request.getQueryString();
        // 通过支付宝的paramsMap进行签名验证，2.0版本的接口将paramsMap参数去掉了，导致同步请求没法验签
        if(StringUtils.isNotBlank(sign)){
            //验签成功
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOrderSn(out_trade_no);
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setSubject(subject);
            paymentInfo.setAlipayTradeNo(trade_no);// 支付宝的交易凭证号
            paymentInfo.setCallbackContent(call_back_content);//回调请求字符串
            paymentInfo.setCallbackTime(new Date());//更新时间
            // 更新用户的支付状态
            paymentService.updatePayment(paymentInfo);
        }
        return "redirect:http://cart.gmall.com:8086/list.html";
    }
}
