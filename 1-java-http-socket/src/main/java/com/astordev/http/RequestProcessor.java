package com.astordev.http;

import java.io.IOException;

public interface RequestProcessor {
    void process(HttpRequest request, HttpResponse response) throws IOException;
}
