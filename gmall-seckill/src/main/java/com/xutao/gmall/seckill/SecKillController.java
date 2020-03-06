package com.xutao.gmall.seckill;

import com.xutao.gmall.mq.ActiveMQUtil;
import com.xutao.gmall.util.RedisUtil;
import org.redisson.client.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

@Controller
public class SecKillController {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    ActiveMQUtil activeMQUtil;


    @RequestMapping("随机拼运气式秒杀")
    @ResponseBody
    public String seckill(){
        Jedis jedis = redisUtil.getJedis();
        //开启商品监控
        jedis.watch("106");
        int stock = Integer.parseInt(jedis.get("106"));
        if(stock>0){
            Transaction multi = jedis.multi();//开启事务
            multi.incrBy("106",-1);
            List<Object> exec = multi.exec();
            if(exec!=null&&exec.size()>0){
                System.out.println("当前库存剩余数量"+stock+",某用户抢购成功，当前抢购人数："+(1000-stock));
                System.out.println("用消息队列发出订单消息");
            }else{
                System.out.println("当前库存剩余数量"+stock+",某用户抢购失败");
            }
        }
        jedis.close();
        return "1";

    }
}
