package com.xutao.gmall.user.controller;

import com.xutao.gmall.bean.UmsMember;
import com.xutao.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserController {

    @Autowired
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
