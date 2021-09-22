package com.atguigu.gmall0218.passport.config;


import io.jsonwebtoken.*;

import java.util.Map;

/*
    1.生成token
    2.@param key 公共部分
    3.@param param 私有部分
    4.@param salt 签名部分
 */

public class JwtUtil {

    public static String encode(String key,Map<String,Object> param,String salt) {

        if (salt !=null){
            key+=salt;
        }
        JwtBuilder jwtBuilder = Jwts.builder().signWith(SignatureAlgorithm.HS256, key);
        // 将用户信息放到jwtBuild
         jwtBuilder = jwtBuilder.setClaims(param);

         // 生成token
        String token = jwtBuilder.compact();
        return token;


    }

    /**
     * 解析token
     * @param token
     * @param key
     * @param salt
     * @return
     */

    public  static Map<String,Object> decode(String token , String key, String salt){
        Claims claims=null;
        if (salt!=null){
            key+=salt;
        }
        try {
            claims= Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
        } catch ( JwtException e) {
            return null;
        }
        return  claims;
    }

}
