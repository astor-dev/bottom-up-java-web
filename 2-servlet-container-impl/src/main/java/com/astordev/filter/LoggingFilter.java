package com.astordev.filter;

import jakarta.servlet.*;
import java.io.IOException;

public class LoggingFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("LoggingFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        System.out.println("LoggingFilter: before chain.doFilter");
        chain.doFilter(request, response);
        System.out.println("LoggingFilter: after chain.doFilter");
    }

    @Override
    public void destroy() {
        System.out.println("LoggingFilter destroyed");
    }
}
