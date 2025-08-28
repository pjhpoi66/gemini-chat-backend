package com.joongho.geminichat.core;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Component
public class LogInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        log.info("INTERCEPTOR [REQUEST]  {}", requestURI);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.info("INTERCEPTOR [POST_HANDLE]");
    }

    // 요청 처리가 완료된 후에 항상 호출됩니다. (예외 발생 여부와 무관)
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        String requestURI = request.getRequestURI();
        log.info("INTERCEPTOR [RESPONSE] {}", requestURI);
        if (ex != null) {
            log.error("afterCompletion error!!", ex);
        }
    }
}
