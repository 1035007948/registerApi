package com.example.registerapi.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final String START_TIME = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME, startTime);
        
        log.info("[请求开始] {} {} from {}", 
                request.getMethod(), 
                request.getRequestURI(),
                request.getRemoteAddr());
        
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 请求处理后的操作
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        long startTime = (Long) request.getAttribute(START_TIME);
        long duration = System.currentTimeMillis() - startTime;
        
        if (ex != null) {
            log.error("[请求异常] {} {} - {}ms - 异常: {}", 
                    request.getMethod(), 
                    request.getRequestURI(),
                    duration,
                    ex.getMessage());
        } else {
            log.info("[请求完成] {} {} - {}ms - 状态码: {}", 
                    request.getMethod(), 
                    request.getRequestURI(),
                    duration,
                    response.getStatus());
        }
    }
}
