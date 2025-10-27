package com.astordev.web.container.filter;

import jakarta.servlet.*;

import java.io.IOException;
import java.util.List;

public class ApplicationFilterChain implements FilterChain {

    private final List<Filter> filters;
    private final Servlet servlet;
    private int currentFilterIndex = 0;

    public ApplicationFilterChain(List<Filter> filters, Servlet servlet) {
        this.filters = filters;
        this.servlet = servlet;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        if (currentFilterIndex < filters.size()) {
            Filter currentFilter = filters.get(currentFilterIndex++);
            currentFilter.doFilter(request, response, this);
        } else {
            if (servlet != null) {
                servlet.service(request, response);
            }
        }
    }
}
