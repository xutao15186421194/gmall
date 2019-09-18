package com.xutao.gmall.user.service.impl;

import com.xutao.gmall.bean.UmsMember;
import com.xutao.gmall.service.UserService;
import com.xutao.gmall.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;

    @Override
    public List<UmsMember> list() {
        return userMapper.selectAll();
    }
}
