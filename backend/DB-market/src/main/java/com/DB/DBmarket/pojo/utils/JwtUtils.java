package com.DB.DBmarket.pojo.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.Map;

public class JwtUtils {
    //JWT签名密钥
    private static String signKey="muizhizhi";
    //JWT过期时间
    private static Long expire=1800000L;//30分钟

    //构建JWT字符串
    public static String generateJwt(Map<String,Object> claims){
        String jwt= Jwts.builder()
                .signWith(SignatureAlgorithm.HS256,signKey) //使用HS256算法进行签名
                .addClaims(claims) //添加声明，（参数）
                .setExpiration(new Date(System.currentTimeMillis()+expire))  //设置过期时间
                .compact(); //构建并返回JWT字符串
        return jwt;
    }
    //解析JWT字符串
    public static Claims parseJWT(String jwt){
            Claims claims= Jwts.parser()
                    .setSigningKey(signKey)   //设置签名用于解析
                    .parseClaimsJws(jwt)// 解析
                    .getBody(); //获取包含声明的Claims对象
            return claims;
    }
}
