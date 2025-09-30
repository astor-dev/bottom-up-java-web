package com.astordev.http.presentation;

import com.astordev.http.HttpRequest;
import com.astordev.http.HttpResponse;
import com.astordev.http.RequestProcessor;

import java.io.IOException;

public class HelloWorldRequestProcessor implements RequestProcessor {
    @Override
    public void process(HttpRequest request, HttpResponse response) throws IOException {
        response.setStatus(200);
        response.addHeader("Content-Type", "text/plain; charset=utf-8");
        response.getWriter().println("Hello, World!");
    }
}
