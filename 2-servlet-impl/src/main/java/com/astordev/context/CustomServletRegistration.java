package com.astordev.context;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.ServletSecurityElement;

import java.util.*;

public class CustomServletRegistration implements ServletRegistration.Dynamic {

    private final String name;
    private final CustomServletContext servletContext;
    private final Class<? extends Servlet> servletClass;
    private final Set<String> mappings = new HashSet<>();

    public CustomServletRegistration(String name, CustomServletContext servletContext, Class<? extends Servlet> servletClass) {
        this.name = name;
        this.servletContext = servletContext;
        this.servletClass = servletClass;
    }

    @Override
    public Set<String> addMapping(String... urlPatterns) {
        Set<String> conflicts = new HashSet<>();
        for (String urlPattern : urlPatterns) {
            servletContext.addServletMapping(name, urlPattern);
            mappings.add(urlPattern);
        }
        // In a real implementation, you'd check for conflicts
        return conflicts;
    }

    @Override
    public Collection<String> getMappings() {
        return Collections.unmodifiableSet(mappings);
    }

    @Override
    public String getRunAsRole() {
        return null;
    }

    @Override
    public void setRunAsRole(String roleName) {
        // Not implemented
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getClassName() {
        return servletClass.getName();
    }

    public Class<? extends Servlet> getServletClass() {
        return servletClass;
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        return false;
    }

    @Override
    public String getInitParameter(String name) {
        return null;
    }

    @Override
    public Set<String> setInitParameters(Map<String, String> initParameters) {
        return Collections.emptySet();
    }

    // --- Dynamic methods ---

    @Override
    public Map<String, String> getInitParameters() {
        return Collections.emptyMap();
    }

    @Override
    public void setLoadOnStartup(int loadOnStartup) {
        // Not implemented
    }

    @Override
    public Set<String> setServletSecurity(ServletSecurityElement constraint) {
        // Not implemented
        return null;
    }

    @Override
    public void setMultipartConfig(jakarta.servlet.MultipartConfigElement multipartConfig) {
        // Not implemented
    }

    @Override
    public void setAsyncSupported(boolean isAsyncSupported) {
        // Not implemented
    }
}
