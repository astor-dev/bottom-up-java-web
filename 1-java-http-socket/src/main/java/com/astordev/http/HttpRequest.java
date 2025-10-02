package com.astordev.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    private final String method;
    private final String requestURI;
    private final Map<String, String> headers = new HashMap<>();
    private final String body;

    public HttpRequest(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            throw new IOException("Invalid request line");
        }
        String[] parts = requestLine.split("\\s+", 3);
        if (parts.length != 3) {
            throw new IOException("Malformed Request-Line: " + requestLine);
        }
        this.method = parts[0];
        this.requestURI = parts[1];

        String headerLine;
        while ((headerLine = reader.readLine()) != null && !headerLine.isEmpty()) {
            final int separatorIndex = headerLine.indexOf(":");
            if (separatorIndex < 1) {
                continue;
            }
            String key = headerLine.substring(0, separatorIndex).trim();
            String value = headerLine.substring(separatorIndex + 1).trim();
            if (!key.isEmpty()) {
                headers.put(key, value);
            }
        }

        int contentLength = 0;
        String contentLengthHeader = getHeader("Content-Length");
        if (contentLengthHeader != null) {
            contentLength = Integer.parseInt(contentLengthHeader);
        }

        if (contentLength > 0) {
            char[] bodyChars = new char[contentLength];
            int bytesRead = reader.read(bodyChars, 0, contentLength);
            if (bytesRead < contentLength) {
                throw new IOException("Unexpected end of stream while reading request body");
            }
            this.body = new String(bodyChars);
        } else {
            this.body = null;
        }
    }

    public String getMethod() {
        return this.method;
    }

    public String getRequestURI() {
        return this.requestURI;
    }

    public String getHeader(String name) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }
}