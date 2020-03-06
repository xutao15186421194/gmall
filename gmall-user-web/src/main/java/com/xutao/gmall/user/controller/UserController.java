package com.xutao.gmall.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.xutao.gmall.bean.UmsMember;
import com.xutao.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserController {

    @Reference
    UserService userServiceImpl;

    @RequestMapping("index")
    @ResponseBody
    public String index(){
        return "hello index";
    }

    @RequestMapping("list")
    @ResponseBody
    public List<UmsMember> list(){
        List<UmsMember> umsMember = userServiceImpl.list();
        return umsMember;
    }
}
