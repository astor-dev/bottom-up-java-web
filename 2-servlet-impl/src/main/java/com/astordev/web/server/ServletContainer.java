package com.astordev.web.server;

import com.astordev.web.connector.BioConnector;
import com.astordev.web.container.context.CustomServletConfig;
import com.astordev.web.container.context.CustomServletContext;
import com.astordev.web.container.context.CustomServletRegistration;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletRegistration;

import java.io.IOException;

@SuppressWarnings("CallToPrintStackTrace")
public class ServletContainer {

    private final CustomServletContext servletContext;
    private final Connector connector;

    public ServletContainer(int port, Connector.Type connectorType) {
        this.servletContext = new CustomServletContext();
        switch (connectorType) {
            case BIO -> this.connector = new BioConnector(port, this.servletContext);
            // TODO: NIO 지원하도록
            default -> throw new IllegalArgumentException("Unsupported connector type: " + connectorType);        }
    }

    public void addServlet(String servletName, Class<? extends Servlet> servletClass, String... urlPatterns) {
        ServletRegistration.Dynamic registration = this.servletContext.addServlet(servletName, servletClass);
        if (registration != null) {
            registration.addMapping(urlPatterns);
        } else {
            System.err.println("Failed to register " + servletName);
        }
    }

    private void initServlets() {
        try {
            for (ServletRegistration registration : servletContext.getServletRegistrations().values()) {
                String servletName = registration.getName();

                if (registration instanceof CustomServletRegistration customRegistration) {
                    Class<? extends Servlet> servletClass = customRegistration.getServletClass();

                    Servlet servlet = servletClass.getDeclaredConstructor().newInstance();

                    servletContext.addInstantiatedServlet(servletName, servlet);

                    servlet.init(new CustomServletConfig(servletName, servletContext));
                }
            }
        } catch (Exception e) {
            System.err.println("Servlet initialization failed");
            e.printStackTrace();
        }
    }

    public void start() throws IOException {
        System.out.println("Server starting...");
        initServlets();
        connector.start();
    }
}
