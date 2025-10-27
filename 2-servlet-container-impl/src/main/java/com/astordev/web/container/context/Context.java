package com.astordev.web.container.context;

import com.astordev.web.container.ServletMapper;
import com.astordev.web.container.session.SessionManager;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;

import java.util.EventListener;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Context {
    private final CustomServletContext servletContext;
    private final SessionManager sessionManager;
    private ServletMapper servletMapper;
    private final Map<String, Servlet> instantiatedServlets = new ConcurrentHashMap<>();

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

    public void addServlet(String servletName, Class<? extends Servlet> servletClass, String... urlPatterns) {
        ServletRegistration.Dynamic registration = this.servletContext.addServlet(servletName, servletClass);
        if (registration != null) {
            registration.addMapping(urlPatterns);
        } else {
            System.err.println("Failed to register " + servletName);
        }
    }

    public void addListener(Class<? extends EventListener> listenerClass) {
        servletContext.addListener(listenerClass);
    }

    public CustomServletContext getServletContext() {
        return servletContext;
    }

    public void initServlets() {
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

    public void destroyServlets() {
        for (Servlet servlet : instantiatedServlets.values()) {
            try {
                servlet.destroy();
            } catch (Exception e) {
                System.err.println("Servlet destruction failed for " + servlet.getClass().getName());
                e.printStackTrace();
            }
        }
    }
}
