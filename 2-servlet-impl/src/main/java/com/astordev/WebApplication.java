package com.astordev;

import java.io.IOException;

public class WebApplication {
    public static void main(String[] args) {
        try {
            ServletContainer servletContainer = new ServletContainer(8080);
            servletContainer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}