package com.astordev.web.server;

import com.astordev.web.bridge.Protocol;
import com.astordev.web.container.ServletMapper;
import com.astordev.web.container.connector.Connector;
import com.astordev.web.net.Endpoint;
import com.astordev.web.container.context.CustomServletConfig;
import com.astordev.web.container.context.CustomServletContext;
import com.astordev.web.container.context.CustomServletRegistration;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class ServletContainer implements AutoCloseable {

    private final CustomServletContext servletContext;
    private List<Connector> connectors = new ArrayList<>();
    private List<ConnectorConfig> connectorConfigs = new ArrayList<>();
    private ServletMapper servletMapper;
    private CountDownLatch shutdownLatch;

    public ServletContainer() {
        this.servletContext = new CustomServletContext();
    }

    public void addServlet(String servletName, Class<? extends Servlet> servletClass, String... urlPatterns) {
        ServletRegistration.Dynamic registration = this.servletContext.addServlet(servletName, servletClass);
        if (registration != null) {
            registration.addMapping(urlPatterns);
        } else {
            System.err.println("Failed to register " + servletName);
        }
    }

    public void addConnector(int port, Endpoint.Type endpointType, Protocol protocol) {
        connectorConfigs.add(new ConnectorConfig(port, endpointType, protocol));
    }

    private void initServlets() {
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

    public void start() {
        initServlets();
        shutdownLatch = new CountDownLatch(1);
        for (ConnectorConfig config : connectorConfigs) {
            Connector connector = new Connector(config.protocol, this, servletMapper, config.port, config.endpointType);
            connectors.add(connector);
        }
        try {
            shutdownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Server interrupted");
        }
    }

    @Override
    public void close() throws Exception {
        for (Connector connector : connectors) {
            connector.close();
        }
        if (shutdownLatch != null) {
            shutdownLatch.countDown();
        }
    }

    private static class ConnectorConfig {
        private final int port;
        private final Endpoint.Type endpointType;
        private final Protocol protocol;

        public ConnectorConfig(int port, Endpoint.Type endpointType, Protocol protocol) {
            this.port = port;
            this.endpointType = endpointType;
            this.protocol = protocol;
        }
    }
}