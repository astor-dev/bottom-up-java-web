package com.astordev;

import com.astordev.http.HttpSocketHandler;
import com.astordev.http.HttpServer;
import com.astordev.tcp.TcpSocketHandler;
import com.astordev.tcp.TcpServer;

import java.io.IOException;

class WebApplication {
    private final static Mode mode = Mode.HTTP;

    public static void main(String[] args) {
        Server server = null;

        switch (mode) {
            case HTTP -> {
                SocketHandler handler = new HttpSocketHandler();
                server = new HttpServer(8080, handler);
            }
            case TCP -> {
                SocketHandler handler = new TcpSocketHandler();
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

    enum Mode {
        HTTP, TCP
    }
}
