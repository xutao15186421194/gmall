package com.xutao.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.xutao.gmall.annotations.LoginRequired;
import com.xutao.gmall.bean.OmsCartItem;
import com.xutao.gmall.bean.OmsOrder;
import com.xutao.gmall.bean.OmsOrderItem;
import com.xutao.gmall.bean.UmsMemberReceiveAddress;
import com.xutao.gmall.service.CartService;
import com.xutao.gmall.service.OrderService;
import com.xutao.gmall.service.SkuService;
import com.xutao.gmall.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
@Controller
public class OrderController {

    @Reference
    UserService userService;

    @Reference
    CartService cartService;

    @Reference
    OrderService orderService;

    @Reference
    SkuService skuService;


    /**
     * 生成订单
     * @param receiveAddressId
     * @param totalAmount
     * @param tradeCode
     * @param request
     * @param response
     * @param session
     * @param modelMap
     * @return
     */
    @RequestMapping("submitOrder")
    @LoginRequired(logginSuccess = true)
    public ModelAndView submitOrder(String receiveAddressId,
                                    BigDecimal totalAmount, String tradeCode,
                                    HttpServletRequest request,
                                    HttpServletResponse response,
                                    HttpSession session, ModelMap modelMap){
        String memberId = (String)request.getAttribute("memberId");
        String nickname = (String)request.getAttribute("nickname");
        //检查交易码
        String success = orderService.checkTradeCode(memberId,tradeCode);
        if(success.equals("success")) {
            List<OmsOrderItem> omsOrderItems = new ArrayList<>();
            //订单对象
            OmsOrder omsOrder = new OmsOrder();
            omsOrder.setAutoConfirmDay("7");//自动确认时间（天）
            omsOrder.setCreateTime(new Date());//提交时间
            omsOrder.setMemberId(memberId);
            omsOrder.setMemberUsername(nickname);
            omsOrder.setNote("急用，快速发货");//订单备注
            String outTradeNo = "gmall";
            outTradeNo = outTradeNo + System.currentTimeMillis();//将毫秒时间戳拼接到外部订单号
            SimpleDateFormat fmt = new SimpleDateFormat("YYYYMMDDHHmmss");
            outTradeNo = outTradeNo + fmt.format(new Date());// 将时间字符串拼接到外部订单号
            omsOrder.setOrderSn(outTradeNo);//订单号
            omsOrder.setPayAmount(totalAmount);//折扣后金额
            omsOrder.setOrderType("0");//订单类型：0正常订单；1秒杀订单
            //配送地址
            UmsMemberReceiveAddress umsMemberReceiveAddresses = userService.getReceiveAddressById(receiveAddressId);
            omsOrder.setReceiverName(umsMemberReceiveAddresses.getName());//收货人姓名
            omsOrder.setReceiverPhone(umsMemberReceiveAddresses.getPhoneNumber());//收货人电话
            omsOrder.setReceiverProvince(umsMemberReceiveAddresses.getProvince());//省份/直辖市
            omsOrder.setReceiverPostCode(umsMemberReceiveAddresses.getPostCode());//收货人邮编
            omsOrder.setReceiverCity(umsMemberReceiveAddresses.getCity());//城市
            omsOrder.setReceiverRegion(umsMemberReceiveAddresses.getRegion());//区
            omsOrder.setReceiverDetailAddress(umsMemberReceiveAddresses.getDetailAddress());//详细地址
            //选择配送时间，隔一天配送
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, 1);
            Date time = calendar.getTime();
            omsOrder.setReceiveTime(time);//配送时间
            omsOrder.setSourceType("0");//订单来源：0PC订单；1app订单
            omsOrder.setStatus("0");//订单状态：0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭；5->无效订单
            omsOrder.setTotalAmount(totalAmount);//总金额
            // 根据用户id获得要购买的商品列表(购物车)，和总价格
            List<OmsCartItem> cartItems = cartService.cartList(memberId);
            for (OmsCartItem cartItem : cartItems) {
                if (cartItem.getIsChecked().equals("1")) {
                    //获得订单详情列表
                    OmsOrderItem omsOrderItem = new OmsOrderItem();
                    //检验库存数量
                    boolean b = skuService.checkPrice(cartItem.getProductSkuId(), cartItem.getPrice());
                    if (b == false) {//检验不通过，去到错误页面
                        ModelAndView mv = new ModelAndView("tradeFail");
                        return mv;
                    }
                    //验库存，远程调用库存系统
                    omsOrderItem.setProductPic(cartItem.getProductPic());//
                    omsOrderItem.setProductName(cartItem.getProductName());
                    omsOrderItem.setOrderSn(outTradeNo);// 外部订单号，用来和其他系统进行交互，防止重复
                    omsOrderItem.setProductCategoryId(cartItem.getProductCategoryId());
                    omsOrderItem.setProductPrice(cartItem.getPrice());
                    omsOrderItem.setProductQuantity(cartItem.getQuantity());
                    omsOrderItem.setProductSkuCode("12323232121");
                    omsOrderItem.setProductSkuId(cartItem.getProductSkuId());
                    omsOrderItem.setProductId(cartItem.getProductId());
                    omsOrderItem.setProductSn("仓库对应的商品编号");// 在仓库中的skuId
                    omsOrderItems.add(omsOrderItem);
                }
            }
            omsOrder.setOmsOrderItems(omsOrderItems);
            // 将订单和订单详情写入数据库
            // 删除购物车的对应商品
            orderService.saveOrder(omsOrder,memberId);
            // 重定向到支付系统
            ModelAndView mv = new ModelAndView("redirect:http://payment.gmall.com:8087/index");
            mv.addObject("outTradeNo",outTradeNo);
            mv.addObject("totalAmount",totalAmount);
            return mv;
        }else {
            ModelAndView mv = new ModelAndView("tradeFail");
            return mv;
        }
    }
    /**
     * 订单页面
     * @param request
     * @param response
     * @param session
     * @param modelMap
     * @return
     */
    @RequestMapping("toTrade")
    @LoginRequired(logginSuccess = true)
    public String toTrade(HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap){

        String memberId = (String)request.getAttribute("memberId");
        String nickname = (String)request.getAttribute("nickname");
        System.out.println(memberId);
        // 收件人地址列表
        if(StringUtils.isNotBlank(memberId)){
            List<UmsMemberReceiveAddress> UmsMemberReceiveAddresses = userService.getReceiveAddressByMemberId(memberId);
            //将购物车集合转化为页面计算清单集合
            List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
            List<OmsOrderItem> omsOrderItems = new ArrayList<>();
            for (OmsCartItem omsCartItem : omsCartItems) {
                // 每循环一个购物车对象，就封装一个商品的详情到OmsOrderItem
                if(omsCartItem.getIsChecked().equals("1")){
                    OmsOrderItem omsOrderItem = new OmsOrderItem();
                    omsOrderItem.setProductName(omsCartItem.getProductName());
                    omsOrderItem.setProductPic(omsCartItem.getProductPic());
                    omsOrderItem.setProductPrice(omsCartItem.getPrice());
                    omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                    omsOrderItems.add(omsOrderItem);
                }
            }
            modelMap.put("userAddressList",UmsMemberReceiveAddresses);
            modelMap.put("omsOrderItems",omsOrderItems);
            modelMap.put("totalAmount",getTotalAmount(omsCartItems));
            // 生成交易码，为了在提交订单时做交易码的校验
            String tradeCode = orderService.genTradeCode(memberId);
            modelMap.put("tradeCode",tradeCode);
        }
        return "trade";
    }
    /**
     * 计算总价
     * @param omsCartItems
     * @return
     */
    public BigDecimal getTotalAmount(List<OmsCartItem> omsCartItems){

        BigDecimal totalAmount = new BigDecimal("0");
        for (OmsCartItem omsCartItem : omsCartItems) {
            BigDecimal totalPrice = omsCartItem.getTotalPrice();
            if( omsCartItem.getIsChecked().equals("1")){
                totalAmount = totalAmount.add(totalPrice);
            }
        }
        return totalAmount;
    }

    @RequestMapping("list.html")
    @LoginRequired(logginSuccess = true)
    public String queryAll(HttpServletRequest request,ModelMap map){
        //获取登录者的用户id以及用户名
        String memberId = (String)request.getAttribute("memberId");
        String nickname = (String)request.getAttribute("nickname");
        //查询订单
        List<OmsOrder> orderSubList = orderService.getMemberIdQueryAll(memberId);
        for (OmsOrder omsOrder : orderSubList) {
            List<OmsOrderItem> omsOrderItems = omsOrder.getOmsOrderItems();
            for (OmsOrderItem omsOrderItem : omsOrderItems) {
                omsOrderItem.setTotalAmount(omsOrderItem.getProductPrice().multiply(omsOrderItem.getProductQuantity()));
            }
        }
        map.put("orderSubList",orderSubList);
        return "list";
    }

}
