package com.astordev.web.container.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FilterMapper {

    private final Map<String, ? extends FilterRegistration> filterRegistrations;
    private final Map<String, Filter> instantiatedFilters;

    public FilterMapper(ServletContext servletContext, Map<String, Filter> instantiatedFilters) {
        this.filterRegistrations = servletContext.getFilterRegistrations();
        this.instantiatedFilters = instantiatedFilters;
    }

    public List<Filter> getMatchingFilters(String requestURI) {
        List<Filter> matchingFilters = new ArrayList<>();
        for (FilterRegistration registration : filterRegistrations.values()) {
            // For now, only exact URL pattern matching is supported.
            if (registration.getUrlPatternMappings().contains(requestURI)) {
                Filter filter = instantiatedFilters.get(registration.getName());
                if (filter != null) {
                    matchingFilters.add(filter);
                }
            }
        }
        return matchingFilters;
    }
}
