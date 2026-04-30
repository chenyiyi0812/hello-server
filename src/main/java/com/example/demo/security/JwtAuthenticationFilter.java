package com.example.demo.security;

import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 1. 读取请求头中的 Authorization
        String authHeader = request.getHeader("Authorization");

        // 2. 如果没有 Authorization，或者不是 Bearer 开头，直接放行给后续过滤器
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 截取真正的 JWT 字符串
        String jwt = authHeader.substring(7);

        String username;

        try {
            // 4. 从 JWT 中解析用户名
            username = jwtUtil.extractUsername(jwt);
        } catch (Exception e) {
            // token 解析失败，直接继续后续过滤器
            filterChain.doFilter(request, response);
            return;
        }

        // 5. 如果解析到了用户名，并且当前还没有认证信息
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 6. 根据用户名查数据库，确认用户仍然存在
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User> queryWrapper = 
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            queryWrapper.eq(User::getUsername, username);
            User dbUser = userMapper.selectOne(queryWrapper);

            if (dbUser != null && jwtUtil.isTokenValid(jwt, dbUser.getUsername())) {
                // 7. 构造一个 Spring Security 认可的 UserDetails 对象
                UserDetails userDetails = org.springframework.security.core.userdetails.User
                        .withUsername(dbUser.getUsername())
                        .password(dbUser.getPassword())
                        .authorities(Collections.emptyList())
                        .build();

                // 8. 创建认证对象
                UsernamePasswordAuthenticationToken authenticationToken = 
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 9. 将认证信息放入 SecurityContextHolder
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        // 10. 继续后续过滤器
        filterChain.doFilter(request, response);
    }
}
