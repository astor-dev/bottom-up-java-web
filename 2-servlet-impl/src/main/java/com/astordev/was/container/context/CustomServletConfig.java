package com.astordev.was.container.context;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;

import java.util.Collections;
import java.util.Enumeration;

public class CustomServletConfig implements ServletConfig {

    private final String servletName;
    private final ServletContext servletContext;

    public CustomServletConfig(String servletName, ServletContext servletContext) {
        this.servletName = servletName;
        this.servletContext = servletContext;
    }

    @Override
    public String getServletName() {
        return servletName;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    // --- Unimplemented methods ---

    @Override
    public String getInitParameter(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.emptyEnumeration();
    }
}
