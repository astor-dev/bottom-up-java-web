package com.astordev;

import com.astordev.http.HttpServer;
import com.astordev.http.HelloWorldHttpServiceHandler;
import com.astordev.tcp.HelloWorldTcpServiceHandler;
import com.astordev.tcp.TcpServer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class WebApplication {
    enum Mode {
        HTTP, TCP
    }
    private final static Mode mode = Mode.TCP;

    public static void main(String[] args) {
        Server server = null;

        switch (mode) {
            case HTTP -> {
                Map<String, ServiceHandler> handlers = new HashMap<>();
                handlers.put("/hello", new HelloWorldHttpServiceHandler());
                server = new HttpServer(8080, handlers);
            }
            case TCP -> {
                ServiceHandler handler = new HelloWorldTcpServiceHandler();
                server = new TcpServer(8080, handler);
            }
        }


        try {
            server.start();
        } catch (IOException e) {
            System.err.println("Failed to start server");
            e.printStackTrace();
        }
    }
}
