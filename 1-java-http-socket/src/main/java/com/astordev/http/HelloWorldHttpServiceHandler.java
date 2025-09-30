package com.astordev.http;

import com.astordev.ServiceHandler;

import java.io.IOException;
import java.net.Socket;

public class HelloWorldHttpServiceHandler implements ServiceHandler {


    @Override
    public void handle(Socket socket) throws IOException {
        HttpRequest request = new HttpRequest(socket.getInputStream());
        HttpResponse response = new HttpResponse(socket.getOutputStream());
        response.setStatus(200);
        response.addHeader("Content-Type", "text/plain; charset=utf-8");
        response.getWriter().println("Hello, World!");
    }
}
