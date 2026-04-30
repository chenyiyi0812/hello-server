package com.example.demo.config;

import com.example.demo.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(AbstractHttpConfigurer::disable) // 开启全局CORS配置
                .csrf(AbstractHttpConfigurer::disable) // 关闭 CSRF 防护，前后端分离场景下建议关闭
                .sessionManagement(session -> session // 配置 Session 管理策略，设置无状态
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth // 重点：配置接口访问规则
                        .antMatchers("POST", "/api/users").permitAll() // 放行注册接口
                        .antMatchers("POST", "/api/users/login").permitAll() // 放行登录接口
                        .anyRequest().authenticated() // 其他所有请求都必须先认证
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(AbstractHttpConfigurer::disable) // 关闭 Spring Security 自带的表单登录
                .httpBasic(AbstractHttpConfigurer::disable); // 关闭 httpBasic 登录

        return http.build();
    }
}
