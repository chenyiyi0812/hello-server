package com.example.demo.utils;

import java.util.UUID;

public class TokenUtils {

    /**
     * 生成Token
     * @return 生成的Token字符串
     */
    public static String generateToken() {
        return "Bearer " + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 验证Token是否有效
     * @param token Token字符串
     * @return 是否有效
     */
    public static boolean validateToken(String token) {
        return token != null && token.startsWith("Bearer ") && token.length() > 7;
    }
}
