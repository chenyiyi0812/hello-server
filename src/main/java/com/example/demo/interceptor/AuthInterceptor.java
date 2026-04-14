package com.example.demo.interceptor;

import com.example.demo.utils.TokenUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        // 1. 获取本次请求的 HTTP 动词和具体路径
        String method = request.getMethod();
        String uri = request.getRequestURI();

        // 2. 手写细粒度放行规则
        // 规则 A：POST /api/users/register → 允许用户注册（放行）
        boolean isCreateUser = "POST".equalsIgnoreCase(method) && "/api/users/register".equals(uri);
        // 规则 B：POST /api/users/login → 允许用户登录（放行）
        boolean isLogin = "POST".equalsIgnoreCase(method) && "/api/users/login".equals(uri);

        // 满足公开操作规则 → 直接放行，无需 Token
        if (isCreateUser || isLogin) {
            return true;
        }

        // 3. 其他操作（包括GET /api/users/{id}）必须校验 Token
        String token = request.getHeader("Authorization");
        if (token == null || !TokenUtils.validateToken(token)) {
            response.setContentType("application/json;charset=UTF-8");
            // 提示需要携带有效凭证
            String errorJson = "{\"code\": 401, \"msg\": \"非法操作：需携带有效凭证\"}";
            response.getWriter().write(errorJson);
            return false;
        }

        // Token 有效 → 放行
        return true;
    }
}