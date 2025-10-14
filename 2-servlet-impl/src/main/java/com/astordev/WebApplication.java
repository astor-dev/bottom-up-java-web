package com.astordev;

import com.astordev.web.bridge.Protocol;
import com.astordev.web.net.Endpoint;
import com.astordev.web.bridge.ProtocolHandler;
import com.astordev.web.server.ServletContainer;
import com.astordev.servlet.HelloWorldServlet;

import java.io.IOException;

public class WebApplication {
    public static void main(String[] args) {
        try (ServletContainer servletContainer = new ServletContainer()) {
            servletContainer.addServlet("HelloWorldServlet", HelloWorldServlet.class, "/hello");
            servletContainer.addConnector(8080, Endpoint.Type.BIO, Protocol.HTTP11);
            servletContainer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}