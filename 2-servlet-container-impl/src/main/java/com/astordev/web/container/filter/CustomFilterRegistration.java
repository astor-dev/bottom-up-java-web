package com.astordev.web.container.filter;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CustomFilterRegistration implements FilterRegistration.Dynamic {

    private final String name;
    private final Class<? extends Filter> filterClass;
    private final Map<String, String> initParameters = new ConcurrentHashMap<>();
    private final Set<String> urlPatternMappings = new HashSet<>();
    private final Set<String> servletNameMappings = new HashSet<>();

    public CustomFilterRegistration(String name, Class<? extends Filter> filterClass) {
        this.name = name;
        this.filterClass = filterClass;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getClassName() {
        return filterClass.getName();
    }

    public Class<? extends Filter> getFilterClass() {
        return filterClass;
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        return initParameters.putIfAbsent(name, value) == null;
    }

    @Override
    public String getInitParameter(String name) {
        return initParameters.get(name);
    }

    @Override
    public Set<String> setInitParameters(Map<String, String> initParameters) {
        Set<String> conflicts = new HashSet<>();
        for (Map.Entry<String, String> entry : initParameters.entrySet()) {
            if (!setInitParameter(entry.getKey(), entry.getValue())) {
                conflicts.add(entry.getKey());
            }
        }
        return conflicts;
    }

    @Override
    public Map<String, String> getInitParameters() {
        return Collections.unmodifiableMap(initParameters);
    }

    @Override
    public void addMappingForServletNames(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... servletNames) {
        servletNameMappings.addAll(Arrays.asList(servletNames));
    }

    @Override
    public Collection<String> getServletNameMappings() {
        return Collections.unmodifiableSet(servletNameMappings);
    }

    @Override
    public void addMappingForUrlPatterns(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... urlPatterns) {
        urlPatternMappings.addAll(Arrays.asList(urlPatterns));
    }

    @Override
    public Collection<String> getUrlPatternMappings() {
        return Collections.unmodifiableSet(urlPatternMappings);
    }

    @Override
    public void setAsyncSupported(boolean isAsyncSupported) {
        // Not implemented
    }
}
