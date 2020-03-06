package com.xutao.gmall.user.service.impl;

import com.xutao.gmall.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class UserServiceImpl {

    @Autowired
    UserMapper userMapper;
}
