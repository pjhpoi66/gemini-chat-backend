package com.joongho.geminichat.core;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LogInterceptor logInterceptor;


    public WebConfig(LogInterceptor logInterceptor) {
        this.logInterceptor = logInterceptor;
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(logInterceptor)
                .order(1) // 인터셉터 체인 순서
                .addPathPatterns("/**") // 모든 URL에 대해 인터셉터 적용
                .excludePathPatterns("/css/**", "/*.ico", "/error"); // 특정 경로는 제외
    }

}
