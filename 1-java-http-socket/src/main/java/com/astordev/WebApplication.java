package com.astordev;

import com.astordev.http.HttpServer;
import com.astordev.http.handler.HelloWorldServiceHandler;
import com.astordev.http.handler.ServiceHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class WebApplication {
    public static void main(String[] args) {
        Map<String, ServiceHandler> handlers = new HashMap<>();
        handlers.put("/hello", new HelloWorldServiceHandler());

        try {
            HttpServer server = new HttpServer(8080, handlers);
            server.start();
        } catch (IOException e) {
            System.err.println("Failed to start server");
            e.printStackTrace();
        }
    }
}
