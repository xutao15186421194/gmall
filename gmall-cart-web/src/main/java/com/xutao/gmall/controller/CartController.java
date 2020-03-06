package com.xutao.gmall.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.xutao.gmall.annotations.LoginRequired;
import com.xutao.gmall.bean.OmsCartItem;
import com.xutao.gmall.bean.PmsSkuInfo;
import com.xutao.gmall.service.CartService;
import com.xutao.gmall.service.SkuService;
import com.xutao.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class CartController {

    @Reference
    SkuService skuService;

    @Reference
    CartService cartService;

    /**
     * 选中时统计总价
     * @param isChecked
     * @param skuId
     * @param request
     * @param response
     * @param session
     * @param modelMap
     * @return
     */
    @RequestMapping("checkCart")
    @LoginRequired(logginSuccess = false)
    public String checkCart(String isChecked,String skuId,HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap) {

        String memberId = null;//模拟用户登录
        memberId = (String)request.getAttribute("memberId");
        //调用服务，修改状态
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setIsChecked(isChecked);
        omsCartItem.setProductSkuId(skuId);
        cartService.checkCart(omsCartItem);
        //将最新的数据从缓存中查询出来，渲染给内嵌页面
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
        modelMap.put("cartList",omsCartItems);
        //计算被勾选的商品的总价格
        BigDecimal totalAmount = getTotalAmount(omsCartItems);
        modelMap.put("totalAmount",totalAmount);
        return "cartListInner";
    }
    /**
     * 计算选中商品的总价
     * @param omsCartItems
     * @return
     */
    private BigDecimal getTotalAmount(List<OmsCartItem> omsCartItems) {
        BigDecimal totalAmount = new BigDecimal("0");
        for (OmsCartItem omsCartItem : omsCartItems) {
            BigDecimal totalPrice = omsCartItem.getTotalPrice();
            if(omsCartItem.getIsChecked()!=null && omsCartItem.getIsChecked().equals("1")){
                totalAmount = totalAmount.add(totalPrice);
            }
        }
        return totalAmount;
    }

    /**
     * 根据用户id查询该用户的购物车信息，并显示到页面上
     * @param
     * @param request
     * @param
     * @param modelMap
     * @return
     */
    @RequestMapping("cartList")
    @LoginRequired(logginSuccess = false)
    public String cartList(HttpServletRequest request,
                           ModelMap modelMap){
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        String memberId = null;//模拟用户登录
        memberId = (String)request.getAttribute("memberId");
        if(StringUtils.isNotBlank(memberId)){
            //如果用户已经登录，则查询数据库或缓存中的数据
            System.out.println("mysql中查询的数据");
            omsCartItems =cartService.cartList(memberId);

        }else{
            //如果用户没有登录，则查询cookie中的数据
            System.out.println("cookie中查询的数据");
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if(StringUtils.isNotBlank(cartListCookie)){
                omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);
            }
        }
        if(omsCartItems!=null && omsCartItems.size()>0){
            for (OmsCartItem omsCartItem : omsCartItems) {
                //计算总价
                omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
            }
            modelMap.put("cartList",omsCartItems);
        }
        // 被勾选商品的总额
        if(omsCartItems !=null && omsCartItems.size()>0){
            BigDecimal totalAmount =getTotalAmount(omsCartItems);
            modelMap.put("totalAmount",totalAmount);
        }
        return "cartList";
    }
    /**
     * 将商品添加到购物车
     * @param skuId
     * @param quantity
     * @param request
     * @param response
     * @param
     * @return
     */
    @RequestMapping("addToCart")
    @LoginRequired(logginSuccess = false)
    public String addToCart(String skuId, String quantity,
                            HttpServletRequest request,
                            HttpServletResponse response){
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        //调用商品服务查询商品信息
        PmsSkuInfo skuInfo = skuService.getSkuById(skuId);
        //将商品信息封装成购物车信息
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setCreateDate(new Date());
        omsCartItem.setDeleteStatus(0);
        omsCartItem.setModifyDate(new Date());
        omsCartItem.setPrice(skuInfo.getPrice());
        omsCartItem.setProductAttr("");
        omsCartItem.setProductBrand("");
        omsCartItem.setProductCategoryId(skuInfo.getCatalog3Id());
        omsCartItem.setProductId(skuInfo.getProductId());
        omsCartItem.setProductName(skuInfo.getSkuName());
        omsCartItem.setProductPic(skuInfo.getSkuDefaultImg());
        omsCartItem.setProductSkuCode("11111111111");
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setIsChecked("1");
        omsCartItem.setQuantity(new BigDecimal(quantity));
        //判断用户是否登录
        String memberId = null;
        memberId = (String)request.getAttribute("memberId");
        if(StringUtils.isBlank(memberId)){
            //用户没有登录
            System.out.println("添加数据到cookie");
            //cookic里面原有的购物车数据
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if(StringUtils.isBlank(cartListCookie)){
                //cookie为空，添加数据到购物车
                omsCartItems.add(omsCartItem);
            }else {
                //不为空，把cookie中的数据转为List<OmsCartItem>数据类型，相当于向omsCartItems中添加数据
                omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);
                //判断添加的数据在购物车中是否存在，返回为true，者表示存在，修改数量即可
                boolean exist = if_cart_exist(omsCartItems, omsCartItem);
                if(exist){
                    //返回为true，说明本次添加的数据已经存在购物车中了，只需要修改数量价格即可
                    for (OmsCartItem cartItem : omsCartItems) {
                        //判断两者的id相等才进行数据更新。
                        if(cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())){
                            cartItem.setQuantity(cartItem.getQuantity().add(omsCartItem.getQuantity()));
                        }
                    }
                }else {
                    //返回为false时，购物车中没有存在本条数据，需要添加该数据到购物车
                    omsCartItems.add(omsCartItem);
                }
            }
            //更新cookie
            CookieUtil.setCookie(request,response,"cartListCookie",JSON.toJSONString(omsCartItems),60*60*72,true);
        }else {
            //用户登录的时候
            System.out.println("添加数据到mysql");
            //从数据库查询出购物车中的数据
            OmsCartItem omsCartItemFromDb = cartService.ifCartExistByUser(memberId,skuId);
            if(omsCartItemFromDb == null){//判断数据库中返回的值是否为空
                //如果为空，说明该用户没有添加过该商品
                omsCartItem.setMemberId(memberId);
                omsCartItem.setMemberNickname("秀秀");
                omsCartItem.setQuantity(new BigDecimal(quantity));
                cartService.addCart(omsCartItem);
            }else{
                //如果该用户添加过该商品,修改该商品的数量即可
                omsCartItemFromDb.setQuantity(omsCartItemFromDb.getQuantity().add(omsCartItem.getQuantity()));
                cartService.updateCart(omsCartItemFromDb);
            }
            //同步缓存
            cartService.flushCartCache(memberId);
        }
        return "redirect:/success.html";
    }

    /**
     * 判断购物车原有数据的id是否等于用户本次添加的数据的id
     * @param omsCartItems
     * @param omsCartItem
     * @return
     */
    private boolean if_cart_exist(List<OmsCartItem> omsCartItems, OmsCartItem omsCartItem) {

        boolean b = false;
        for (OmsCartItem cartItem : omsCartItems) {

            String productSkuId = cartItem.getProductSkuId();
            //判断购物车原有数据的id是否等于用户本次添加的数据的id
            //如果相等返回true，否则返回false；
            if(productSkuId.equals(omsCartItem.getProductSkuId())){
                b = true;
            }
        }
        return b;
    }
}
