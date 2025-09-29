package com.astordev;

import com.astordev.server.Server;
import com.astordev.server.TcpServer;
import com.astordev.server.handler.EchoServiceHandler;
import com.astordev.server.handler.ServiceHandler;

class WebApplication {
    public static void main(String[] args) {
        ServiceHandler handler = new EchoServiceHandler();
        Server server = new TcpServer(9000, 4, handler);
        server.run();
    }
}