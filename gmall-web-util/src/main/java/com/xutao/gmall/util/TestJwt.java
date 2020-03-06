package com.xutao.gmall.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * token加密
 */
public class TestJwt {


    public static void main(String[] args) {

        Map<String,Object> param = new HashMap<>();
        param.put("memberId","1");
        param.put("nickname","张三");
        String ip = "127.0.0.1";
        String time = new SimpleDateFormat("yyMMdd HHmmss").format(new Date());
        String encode = JwtUtil.encode("gmall", param, ip + time);
        System.err.println(encode);
        Map<String, Object> decode = JwtUtil.decode(encode, "gmall", ip + time);
        System.out.println(decode);
    }
}
