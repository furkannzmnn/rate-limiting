package com.example.ratelimiting.interceptor;

import com.example.ratelimiting.service.RateLimitingService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitingService rateLimitingService;

    public RateLimitInterceptor(RateLimitingService rateLimitingService) {
        this.rateLimitingService = rateLimitingService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (!rateLimitingService.isRateLimitExceeded(request,response,handler)) {
            // print screen
            PrintWriter writer = response.getWriter();
            writer.write("Rate limit exceeded");
            return false;
        }
        return true;
    }

    }
