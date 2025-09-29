package com.astordev.http.handler;

import com.astordev.http.HttpRequest;
import com.astordev.http.HttpResponse;

import java.io.IOException;

public class HelloWorldServiceHandler implements ServiceHandler {
    @Override
    public void handle(HttpRequest request, HttpResponse response) throws IOException {
        response.setStatus(200);
        response.addHeader("Content-Type", "text/plain; charset=utf-8");
        response.getWriter().println("hello world");
    }
}
