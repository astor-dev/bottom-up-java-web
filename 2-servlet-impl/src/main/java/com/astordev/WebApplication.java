package com.astordev;

import com.astordev.was.server.Connector;
import com.astordev.was.server.ServletContainer;
import com.astordev.servlet.HelloWorldServlet;

import java.io.IOException;

public class WebApplication {
    public static void main(String[] args) {
        try {
            ServletContainer servletContainer = new ServletContainer(8080, Connector.Type.BIO);

            servletContainer.addServlet("HelloWorldServlet", HelloWorldServlet.class, "/hello");

            servletContainer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}