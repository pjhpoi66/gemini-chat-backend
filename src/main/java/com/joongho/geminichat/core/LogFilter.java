package com.joongho.geminichat.core;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class LogFilter implements Filter {

    private final String ec2Url;

    public LogFilter(@Value("${ec2.url}") String ec2Url) {
        this.ec2Url = ec2Url;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();

        log.info("FILTER [REQUEST]  {} ", requestURI);

        log.info("ec2URL = {}", ec2Url);

        chain.doFilter(request, response);

        log.info("FILTER [RESPONSE] {} ", requestURI);
    }
}
