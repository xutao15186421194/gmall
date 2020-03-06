package com.xutao.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.xutao.gmall.bean.UmsMember;
import com.xutao.gmall.bean.UmsMemberReceiveAddress;
import com.xutao.gmall.service.UserService;
import com.xutao.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import com.xutao.gmall.user.mapper.UserMapper;
import com.xutao.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public List<UmsMember> list() {
        return userMapper.selectAll();
    }

    @Override
    public UmsMember login(UmsMember umsMember) {
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            if(jedis != null){
                String umsMenberStr = jedis.get("user:" + umsMember.getPassword() + ":info");
                if(StringUtils.isNotBlank(umsMenberStr)){
                    //密码正确
                    UmsMember umsMemberCache = JSON.parseObject(umsMenberStr, UmsMember.class);
                    return umsMemberCache;
                }
            }
            //连接redis失败,开启数据库查询
            UmsMember umsMemberFromDb = loginFromDb(umsMember);
            if(umsMemberFromDb!=null){
                jedis.setex("user:"+umsMemberFromDb.getPassword()+":info",60*60*24,JSON.toJSONString(umsMemberFromDb));
            }
            return umsMemberFromDb;
        }finally {
            jedis.close();
        }
    }
    private UmsMember loginFromDb(UmsMember umsMember) {
        List<UmsMember> umsMembers = userMapper.select(umsMember);
        if(umsMembers != null){
            return umsMembers.get(0);
        }
        return null;
    }
    @Override
    public void addUserToken(String token, String memberId) {
        Jedis jedis = redisUtil.getJedis();

        jedis.setex("user:"+memberId+":token",60*60*2,token);

        jedis.close();
    }

    @Override
    public UmsMember checkOauthUser(UmsMember umsCheck) {
        return userMapper.selectOne(umsCheck);
    }

    @Override
    public void addOauthUser(UmsMember umsMember) {

        userMapper.insertSelective(umsMember);
    }

    @Override
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId) {

        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setMemberId(memberId);
        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = umsMemberReceiveAddressMapper.select(umsMemberReceiveAddress);
        return umsMemberReceiveAddresses;
    }

    @Override
    public UmsMemberReceiveAddress getReceiveAddressById(String receiveAddressId) {
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setId(receiveAddressId);
        UmsMemberReceiveAddress umsMemberReceiveAddress1 = umsMemberReceiveAddressMapper.selectOne(umsMemberReceiveAddress);
        return umsMemberReceiveAddress1;
    }
}
