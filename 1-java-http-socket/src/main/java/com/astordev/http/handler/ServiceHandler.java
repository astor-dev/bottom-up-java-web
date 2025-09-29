package com.astordev.http.handler;

import com.astordev.http.HttpRequest;
import com.astordev.http.HttpResponse;

import java.io.IOException;

public interface ServiceHandler {
    void handle(HttpRequest request, HttpResponse response) throws IOException;
}
