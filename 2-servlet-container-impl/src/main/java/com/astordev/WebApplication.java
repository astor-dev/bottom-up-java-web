package com.astordev;

import com.astordev.filter.LoggingFilter;
import com.astordev.listener.ContextListener;
import com.astordev.web.bridge.Protocol;
import com.astordev.web.net.Endpoint;
import com.astordev.web.container.ServletContainer;
import com.astordev.servlet.HelloWorldServlet;

public class WebApplication {
    public static void main(String[] args) {
        try (ServletContainer servletContainer = new ServletContainer()) {
            servletContainer.addListener(ContextListener.class);
            servletContainer.addFilter("LoggingFilter", LoggingFilter.class, "/*");
            servletContainer.addServlet("HelloWorldServlet", HelloWorldServlet.class, "/hello");
            servletContainer.addConnector(8080, Endpoint.Type.BIO, Protocol.HTTP11);
            servletContainer.addConnector(8081, Endpoint.Type.NIO, Protocol.HTTP11);
            servletContainer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}