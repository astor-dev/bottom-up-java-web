package com.astordev.web;

import com.astordev.web.bridge.Protocol;
import com.astordev.web.container.connector.Connector;
import com.astordev.web.container.context.Context;
import com.astordev.web.net.Endpoint;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ServletContainer implements AutoCloseable {

    private final Context context;
    private final List<Connector> connectors = new ArrayList<>();
    private final List<ConnectorConfig> connectorConfigs = new ArrayList<>();
    private CountDownLatch shutdownLatch;

    public ServletContainer() {
        this.context = new Context();
    }

    public void addConnector(int port, Endpoint.Type endpointType, Protocol protocol) {
        connectorConfigs.add(new ConnectorConfig(port, endpointType, protocol));
    }

    public void addListener(Class<? extends EventListener> listenerClass) {
        this.context.addListener(listenerClass);
    }

    public void addServlet(String servletName, Class<? extends Servlet> servletClass, String... urlPatterns) {
        this.context.addServlet(servletName, servletClass, urlPatterns);
    }

    public void start() {
        context.initServlets();
        fireContextInitializedEvent();
        shutdownLatch = new CountDownLatch(1);
        for (ConnectorConfig config : connectorConfigs) {
            Connector connector = new Connector(config.protocol, this, context, config.port, config.endpointType);
            connectors.add(connector);
        }
        try {
            shutdownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Server interrupted");
        }
    }

    private void fireContextInitializedEvent() {
        ServletContextEvent event = new ServletContextEvent(context.getServletContext());
        for (EventListener listener : context.getServletContext().getListeners()) {
            if (listener instanceof ServletContextListener) {
                ((ServletContextListener) listener).contextInitialized(event);
            }
        }
    }

    private void fireContextDestroyedEvent() {
        ServletContextEvent event = new ServletContextEvent(context.getServletContext());
        for (EventListener listener : context.getServletContext().getListeners()) {
            if (listener instanceof ServletContextListener) {
                ((ServletContextListener) listener).contextDestroyed(event);
            }
        }
    }

    @Override
    public void close() throws Exception {
        fireContextDestroyedEvent();
        context.destroyServlets();
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