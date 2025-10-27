package com.astordev.web.container.context;

import com.astordev.web.container.ServletMapper;
import com.astordev.web.container.filter.CustomFilterConfig;
import com.astordev.web.container.filter.CustomFilterRegistration;
import com.astordev.web.container.filter.FilterMapper;
import com.astordev.web.container.session.SessionManager;
import jakarta.servlet.*;

import java.util.EventListener;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Context {
    private final CustomServletContext servletContext;
    private final SessionManager sessionManager;
    private ServletMapper servletMapper;
    private FilterMapper filterMapper;
    private final Map<String, Servlet> instantiatedServlets = new ConcurrentHashMap<>();
    private final Map<String, Filter> instantiatedFilters = new ConcurrentHashMap<>();

    public Context() {
        this.servletContext = new CustomServletContext();
        this.sessionManager = new SessionManager(this);
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public ServletMapper getServletMapper() {
        return this.servletMapper;
    }

    public FilterMapper getFilterMapper() {
        return filterMapper;
    }

    public void addServlet(String servletName, Class<? extends Servlet> servletClass, String... urlPatterns) {
        ServletRegistration.Dynamic registration = this.servletContext.addServlet(servletName, servletClass);
        if (registration != null) {
            registration.addMapping(urlPatterns);
        } else {
            System.err.println("Failed to register " + servletName);
        }
    }

    public void addFilter(String filterName, Class<? extends Filter> filterClass, String... urlPatterns) {
        FilterRegistration.Dynamic registration = this.servletContext.addFilter(filterName, filterClass);
        if (registration != null) {
            registration.addMappingForUrlPatterns(null, true, urlPatterns);
        } else {
            System.err.println("Failed to register filter " + filterName);
        }
    }

    public void addListener(Class<? extends EventListener> listenerClass) {
        servletContext.addListener(listenerClass);
    }

    public CustomServletContext getServletContext() {
        return servletContext;
    }

    public void init() {
        initFilters();
        initServlets();
        this.filterMapper = new FilterMapper(this.servletContext, instantiatedFilters);
    }

    private void initServlets() {
        try {
            for (ServletRegistration registration : servletContext.getServletRegistrations().values()) {
                String servletName = registration.getName();

                if (registration instanceof CustomServletRegistration customRegistration) {
                    Class<? extends Servlet> servletClass = customRegistration.getServletClass();

                    Servlet servlet = servletClass.getDeclaredConstructor().newInstance();
                    instantiatedServlets.put(servletName, servlet);

                    servlet.init(new CustomServletConfig(servletName, servletContext));
                }
            }
            this.servletMapper = new ServletMapper(this.servletContext, instantiatedServlets);
        } catch (Exception e) {
            System.err.println("Servlet initialization failed");
            e.printStackTrace();
        }
    }

    private void initFilters() {
        try {
            for (FilterRegistration registration : servletContext.getFilterRegistrations().values()) {
                String filterName = registration.getName();
                if (registration instanceof CustomFilterRegistration customRegistration) {
                    Class<? extends Filter> filterClass = customRegistration.getFilterClass();
                    Filter filter = filterClass.getDeclaredConstructor().newInstance();
                    filter.init(new CustomFilterConfig(filterName, servletContext, registration.getInitParameters()));
                    instantiatedFilters.put(filterName, filter);
                }
            }
        } catch (Exception e) {
            System.err.println("Filter initialization failed");
            e.printStackTrace();
        }
    }

    public void destroy() {
        destroyFilters();
        destroyServlets();
    }

    private void destroyServlets() {
        for (Servlet servlet : instantiatedServlets.values()) {
            try {
                servlet.destroy();
            } catch (Exception e) {
                System.err.println("Servlet destruction failed for " + servlet.getClass().getName());
                e.printStackTrace();
            }
        }
    }

    private void destroyFilters() {
        for (Filter filter : instantiatedFilters.values()) {
            try {
                filter.destroy();
            } catch (Exception e) {
                System.err.println("Filter destruction failed for " + filter.getClass().getName());
                e.printStackTrace();
            }
        }
    }
}
