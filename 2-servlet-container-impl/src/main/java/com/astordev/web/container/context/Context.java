package com.astordev.web.container.context;

import com.astordev.web.container.ServletMapper;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Context {
    private final ServletContext servletContext;
    private ServletMapper servletMapper;

    public Context() {
        this.servletContext = new CustomServletContext();
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

    public void initServlets() {
        try {
            Map<String, Servlet> instantiatedServlets = new ConcurrentHashMap<>();
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
}
