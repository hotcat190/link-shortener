package com.example.linkshortener.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;

public class LoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Log request details
        logger.info("Request Method: {} | URL: {}", request.getMethod(), request.getRequestURI());

        long startTime = System.currentTimeMillis();
        filterChain.doFilter(request, response);
        long duration = System.currentTimeMillis() - startTime;

        // Log response details
        logger.info("Response Status: {} | Duration: {}ms", response.getStatus(), duration);

    }
}
