package com.gzu.bigdata.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class JwtUtil {
    // 密钥（防伪水印，千万不能告诉别人），你可以随便改
    private static final String SECRET_KEY = "BigDataGzuSecretKey2025";
    // 过期时间：24小时 (毫秒)
    private static final long EXPIRE_TIME = 24 * 60 * 60 * 1000;

    /**
     * 生成 Token (发工牌)
     * 把 userId 和 role 存进 Token 里
     */
    public static String createToken(Long userId, Integer role) {
        return Jwts.builder()
                .setSubject(userId.toString()) // 存用户ID
                .claim("role", role)           // 存角色信息
                .setIssuedAt(new Date())       // 发证时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_TIME)) // 过期时间
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY) // 盖章（签名）
                .compact();
    }

    /**
     * 解析 Token 获取 UserID (查工牌)
     */
    public static Long getUserId(String token) {
        if (token == null || token.isEmpty()) return null;
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            return null; // 假工牌或过期的工牌
        }
    }
}