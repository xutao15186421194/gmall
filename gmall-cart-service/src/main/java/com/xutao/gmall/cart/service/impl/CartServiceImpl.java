package com.xutao.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.xutao.gmall.bean.OmsCartItem;
import com.xutao.gmall.cart.mapper.OmsCartItemMapper;
import com.xutao.gmall.service.CartService;
import com.xutao.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    OmsCartItemMapper omsCartItemMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public OmsCartItem ifCartExistByUser(String memberId, String skuId) {
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setProductSkuId(skuId);
        return omsCartItemMapper.selectOne(omsCartItem);
    }

    /**
     * 添加数据到购物车
     * @param omsCartItem
     */
    @Override
    public void addCart(OmsCartItem omsCartItem) {
        if(StringUtils.isNotBlank(omsCartItem.getMemberId())){
            omsCartItemMapper.insertSelective(omsCartItem);//避免空值
        }
    }

    /**
     * 修改数据
     * @param omsCartItemFromDb
     */
    @Override
    public void updateCart(OmsCartItem omsCartItemFromDb) {
        Example example = new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("id",omsCartItemFromDb.getId());
        omsCartItemMapper.updateByExampleSelective(omsCartItemFromDb,example);
    }

    @Override
    public void flushCartCache(String memberId) {
        //根据id查询该用户在购物车中的所有数据
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        List<OmsCartItem> omsCartItems = omsCartItemMapper.select(omsCartItem);
        //同步到redis中
        Jedis jedis = redisUtil.getJedis();
        Map<String,String> map = new HashMap<>();
        if(omsCartItems.size() > 0  && omsCartItems != null) {
            for (OmsCartItem cartItem : omsCartItems) {
                cartItem.setTotalPrice(cartItem.getPrice().multiply(cartItem.getQuantity()));
                map.put(cartItem.getProductId(), JSON.toJSONString(cartItem));
            }
            jedis.del("user:" + memberId + ":cart");
            jedis.hmset("user:" + memberId + ":cart", map);
        }else {
            //如果没有在数据库中查询到数据
            //防止缓存击穿，将null或空值设置给redist
            jedis.setex("sku:"+UUID.randomUUID().toString()+":info",60*3,JSON.toJSONString(""));
        }
        jedis.close();
    }

    @Override
    public List<OmsCartItem> cartList(String memberId) {
        Jedis jedis = null;
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        try {
            jedis = redisUtil.getJedis();
            List<String> hvals = jedis.hvals("user:" + memberId + ":cart");
            //如果缓存中没有数据，则在从数据库中查询
            if(hvals.size() > 0){
                for (String hval : hvals) {
                    OmsCartItem omsCartItem = JSON.parseObject(hval,OmsCartItem.class);
                    omsCartItems.add(omsCartItem);
                }
            }else{
                //设置分布式锁
                String token = UUID.randomUUID().toString();//密码,钥匙
                String OK = jedis.set("user:" + memberId + ":lock", token, "nx", "px", 10*1000);
                if(StringUtils.isNotBlank(OK) && OK.equals("OK")){
                    //设置成功后，赋予10秒的权限访问数据库
                    flushCartCache(memberId);
                    String lockToken = jedis.get("user:" + memberId + ":lock");
                    if(StringUtils.isNotBlank(lockToken) && lockToken.equals(token)){
                        //jedis.eval("lua");可与用lua脚本，在查询到key的同时删除该key，防止高并发下的意外的发生
                        jedis.del("user:" + memberId + ":lock");//用token确认删除的是自己的锁
                    }
                }else{
                    //如果分布式锁设置失败，让其自旋，（有点相似于递归调用，让该线程睡上几秒，在重新调用该方法）
                    Thread.sleep(3000);
                    return cartList(memberId);
                }
            }
        }catch (Exception e){
            return null;
        }finally {
            jedis.close();
        }
        return omsCartItems;
    }

    @Override
    public void checkCart(OmsCartItem omsCartItem) {
        Example example = new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("memberId",omsCartItem.getMemberId()).andEqualTo("productSkuId",omsCartItem.getProductSkuId());
        omsCartItemMapper.updateByExampleSelective(omsCartItem,example);
        flushCartCache(omsCartItem.getMemberId());
    }

    @Override
    public void delCart(String productSkuId, String memberId) {
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setProductSkuId(productSkuId);
        omsCartItem.setMemberId(memberId);
        omsCartItemMapper.delete(omsCartItem);
        //重新同步数据到redis
        flushCartCache(memberId);
    }
}
