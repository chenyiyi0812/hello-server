package com.cyz.task2.interceptor;



import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 尝试从HTTP请求头中获取名为"Authorization"的令牌信息
        String token = request.getHeader("Authorization");

        // 2. 如果没有携带Token，直接拦截，不放行到Controller
        if (token == null || token.isEmpty()) {
            // 设置响应内容类型为JSON
            response.setContentType("application/json;charset=UTF-8");
            // 构造401报错的JSON字符串返回给前端
            String errorJson = "{\"code\": 401,\"msg\":\"登录凭证已缺失,请重新登录\"}";
            response.getWriter().write(errorJson);
            return false; // 返回 false 表示拦截打回
        }

        return true; // 令牌存在，返回 true 予以放行
    }
}