package com.xutao.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.xutao.gmall.bean.OmsCartItem;
import com.xutao.gmall.bean.OmsOrder;
import com.xutao.gmall.bean.OmsOrderItem;
import com.xutao.gmall.mq.ActiveMQUtil;
import com.xutao.gmall.order.mapper.OmsOrderItemMapper;
import com.xutao.gmall.order.mapper.OmsOrderMapper;
import com.xutao.gmall.service.CartService;
import com.xutao.gmall.service.OrderService;
import com.xutao.gmall.util.RedisUtil;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import javax.jms.Queue;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    OmsOrderMapper omsOrderMapper;

    @Autowired
    OmsOrderItemMapper omsOrderItemMapper;

    @Reference
    CartService cartService;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Override
    public List<OmsOrder> getMemberIdQueryAll(String memberId) {
        //OmsOrder omsOrder = new OmsOrder();
        //omsOrder.setMemberId(memberId);
        //List<OmsOrder> omsOrders = omsOrderMapper.select(omsOrder);
        List<OmsOrder> omsOrders = new ArrayList<>();
        Jedis jedis = null;
        jedis = redisUtil.getJedis();
        String orderKey = "user:"+memberId+":order";
        List<String> hvals = jedis.hvals(orderKey);
        //如果缓存中没有数据，在从数据库中查询
        if(hvals.size()>0){
            for (String hval : hvals) {
                OmsOrder omsOrder = JSON.parseObject(hval,OmsOrder.class);
                omsOrders.add(omsOrder);
            }
        }else{
            //设置分布式锁
            String token = UUID.randomUUID().toString();
            String OK = jedis.set("lock:"+memberId+":order",token,"nx","px",10*1000);
            if(StringUtils.isNotBlank(OK)&&OK.equals("OK")){
                //设置成功后，赋予10秒钟的访问数据库时间
                Example example = new Example(OmsOrder.class);
                example.createCriteria().andEqualTo("memberId",memberId);
                example.setOrderByClause("create_time desc");
                omsOrders = omsOrderMapper.selectByExample(example);
                for (OmsOrder omsOrder : omsOrders) {
                    OmsOrderItem omsOrderItem = new OmsOrderItem();
                    omsOrderItem.setOrderId(omsOrder.getId());
                    List<OmsOrderItem> omsOrderItems = omsOrderItemMapper.select(omsOrderItem);
                    omsOrder.setOmsOrderItems(omsOrderItems);
                }
                Map<String,String> map = new HashMap<>();
                if(omsOrders.size() > 0  && omsOrders != null) {
                    for (OmsOrder omsOrder : omsOrders) {
                        map.put(omsOrder.getId(), JSON.toJSONString(omsOrder));
                    }
                    jedis.del("user:" + memberId + ":order");
                    jedis.hmset("user:" + memberId + ":order", map);
                }else{
                    //如果数据库中没有查询到数据，防止缓存击穿将null或者空值传个redis
                    jedis.setex("sku:"+UUID.randomUUID().toString()+":info",60*3,JSON.toJSONString(""));
                }
                String lockToken = jedis.get("lock:"+memberId+":order");
                if(StringUtils.isNotBlank(lockToken) && lockToken.equals(token)){
                    //用token来确认删除的是自己的锁
                    jedis.del("lock:" + memberId + ":order");
                }
            }else{
                //如果分布式锁设置失败，让其自旋，（有点相似于递归调用，让该线程睡上几秒，在重新调用该方法）
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return getMemberIdQueryAll(memberId);
            }
        }
        return omsOrders;
    }

    @Override
    public String genTradeCode(String memberId) {
        Jedis jedis = redisUtil.getJedis();
        String tradeKey = "user:"+memberId+":tradeCode";
        String tradeCode = UUID.randomUUID().toString();
        jedis.setex(tradeKey,60*15,tradeCode);
        jedis.close();
        return tradeCode;
    }

    @Override
    public String checkTradeCode(String memberId, String tradeCode) {
        Jedis jedis = null;
        try{
            jedis = redisUtil.getJedis();
            String tradeKey = "user:"+memberId+":tradeCode";
            String tradCodeFromCache = jedis.get(tradeKey);//使用lua脚本在发现key的同时将key删除，防止并发订单攻击
            //对比防重删令牌
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Long eval = (Long)jedis.eval(script, Collections.singletonList(tradeKey), Collections.singletonList(tradeCode));
            if(eval!=null && eval!=0){
                //jedis.del(tradCodeFromCache);
                return "success";
            }else{
                return "fail";
            }
        }finally {
            jedis.close();
        }
    }

    @Override
    public void saveOrder(OmsOrder omsOrder,String memberId) {
        //保存订单表
        omsOrderMapper.insertSelective(omsOrder);
        String orderId = omsOrder.getId();
        //保存订单详情表
        List<OmsOrderItem> omsOrderItems = omsOrder.getOmsOrderItems();
        for(OmsOrderItem omsOrderItem:omsOrderItems){
            omsOrderItem.setOrderId(orderId);
            omsOrderItemMapper.insertSelective(omsOrderItem);
            //删除购物车
            cartService.delCart(omsOrderItem.getProductSkuId(),memberId);
        }

    }

    @Override
    public OmsOrder getOrderByOutTradeNo(String outTradeNo) {
       OmsOrder omsOrder = new OmsOrder();
               omsOrder.setOrderSn(outTradeNo);
        OmsOrder omsOrder1 = omsOrderMapper.selectOne(omsOrder);
        return omsOrder1;
    }

    @Override
    public void updateOrder(OmsOrder omsOrder) {

        Example example = new Example(OmsOrder.class);
        example.createCriteria().andEqualTo("orderSn",omsOrder.getOrderSn());
        OmsOrder omsOrderUpdate = new OmsOrder();
        omsOrderUpdate.setStatus("1");//订单状态：0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭；5->无效订单
        // 发送一个订单已支付的队列，提供给库存消费
        Connection connection = null;
        Session session = null;
        try {
            connection = activeMQUtil.getConnectionFactory().createConnection();
            session = connection.createSession(true,Session.SESSION_TRANSACTED);
            Queue order_pay_queue = session.createQueue("ORDER_PAY_QUEUE");
            MessageProducer producer = session.createProducer(order_pay_queue);
            TextMessage textMessage=new ActiveMQTextMessage();//字符串文本
            //MapMessage mapMessage = new ActiveMQMapMessage();// hash结构

            // 查询订单的对象，转化成json字符串，存入ORDER_PAY_QUEUE的消息队列
            OmsOrder omsOrderParam = new OmsOrder();
            omsOrderParam.setOrderSn(omsOrder.getOrderSn());
            OmsOrder omsOrderResponse = omsOrderMapper.selectOne(omsOrderParam);

            OmsOrderItem omsOrderItemParam = new OmsOrderItem();
            omsOrderItemParam.setOrderSn(omsOrderParam.getOrderSn());
            List<OmsOrderItem> select = omsOrderItemMapper.select(omsOrderItemParam);
            omsOrderResponse.setOmsOrderItems(select);
            textMessage.setText(JSON.toJSONString(omsOrderResponse));
            omsOrderMapper.updateByExampleSelective(omsOrderUpdate,example);
            producer.send(textMessage);
            session.commit();
        } catch (JMSException e) {
            //消息回滚
            try {
                session.rollback();
            } catch (JMSException e1) {
                e1.printStackTrace();
            }
            //关闭连接
            try {
                connection.close();
            } catch (JMSException e1) {
                e1.printStackTrace();
            }
        }
    }

}
