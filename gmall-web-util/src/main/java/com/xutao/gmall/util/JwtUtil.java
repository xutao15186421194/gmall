package com.xutao.gmall.util;

import io.jsonwebtoken.*;

import java.util.Map;

/**
 * token加密与减密
 */
public class JwtUtil {

    /**
     * 加密
     * @param key
     * @param param
     * @param salt
     * @return
     */
    public static String encode(String key,
                                Map<String,Object> param,
                                String salt//盐值
    ){
        if(salt != null){
            key += salt;
        }
        JwtBuilder builder = Jwts.builder().signWith(SignatureAlgorithm.HS256, key);
        builder = builder.setClaims(param);
        String token = builder.compact();
        return token;
    }

    /**
     * 解密
     * @param token
     * @param key
     * @param salt
     * @return
     */
    public static Map<String,Object> decode(String token,String key,String salt){
        Claims claims=null;
        if(salt != null){
            key += salt;
        }
        try {
            claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
        }catch (JwtException e){
            return null;
        }
        return claims;
    }
}
