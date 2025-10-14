package com.astordev.web.container;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServletMapper {
    private final Map<String, Servlet> exactMappings;

    public ServletMapper(ServletContext servletContext, Map<String, Servlet> instantiatedServlets) {
        this.exactMappings = new ConcurrentHashMap<>();

        for (ServletRegistration registration : servletContext.getServletRegistrations().values()) {
            String servletName = registration.getName();
            Servlet servlet = instantiatedServlets.get(servletName);
            if (servlet != null) {
                for (String pattern : registration.getMappings()) {
                    exactMappings.put(pattern, servlet);
                }
            }
        }
    }

    public Servlet map(String requestURI) {
        return exactMappings.get(requestURI);
    }
}