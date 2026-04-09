package com.cyz.task2.config;


import com.cyz.task2.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration  // 核心配置注解，表示这是一个配置类
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor())
                // 拦截 /api 下的所有请求路径
                .addPathPatterns("/api/**")
                // 排除不需要拦截的路径
                .excludePathPatterns(
                        "/api/users/login" // 保留原本放行的登录接口

                );
    }
}