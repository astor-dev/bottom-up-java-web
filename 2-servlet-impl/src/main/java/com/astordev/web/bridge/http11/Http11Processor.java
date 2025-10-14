package com.astordev.web.bridge.http11;

import com.astordev.web.bridge.Gateway;
import com.astordev.web.bridge.Request;
import com.astordev.web.bridge.Response;
import com.astordev.web.net.SocketWrapperBase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class Http11Processor {

    private final SocketWrapperBase socketWrapper;
    private final Gateway gateway;

    public Http11Processor(SocketWrapperBase socketWrapper, Gateway gateway) {
        this.socketWrapper = socketWrapper;
        this.gateway = gateway;
    }

    public void process() {
        Request request = new Request();
        Response response = new Response();

        try(socketWrapper) {
            parseRequest(request);
            System.out.println("[" + request.getMethod() + " " + request.getRequestURI() + "]");
            gateway.service(request, response);
            sendResponse(response);
        } catch (Exception e) {
            response.setStatus(500);
            response.setBody("Internal Server Error");
            try {
                sendResponse(response);
            } catch (IOException ignored) {}
            e.printStackTrace();
        }
    }

    private void parseRequest(Request request) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(socketWrapper.getInputStream()));

        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            throw new IOException("Invalid request line");
        }
        String[] parts = requestLine.split("\\s+", 3);
        if (parts.length != 3) {
            throw new IOException("Malformed Request-Line: " + requestLine);
        }
        request.setMethod(parts[0]);
        request.setRequestURI(parts[1]);

        String headerLine;
        while ((headerLine = reader.readLine()) != null && !headerLine.isEmpty()) {
            final int separatorIndex = headerLine.indexOf(":");
            if (separatorIndex < 1) {
                continue;
            }
            String key = headerLine.substring(0, separatorIndex).trim();
            String value = headerLine.substring(separatorIndex + 1).trim();
            if (!key.isEmpty()) {
                request.addHeader(key, value);
            }
        }

        int contentLength = 0;
        String contentLengthHeader = request.getHeader("Content-Length");
        if (contentLengthHeader != null) {
            contentLength = Integer.parseInt(contentLengthHeader);
        }

        if (contentLength > 0) {
            char[] bodyChars = new char[contentLength];
            int bytesRead = reader.read(bodyChars, 0, contentLength);
            if (bytesRead < contentLength) {
                throw new IOException("Unexpected end of stream while reading request body");
            }
            request.setBody(new String(bodyChars));
        }
    }

    private void sendResponse(Response response) throws IOException {
        OutputStream outputStream = socketWrapper.getOutputStream();
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(response.getStatus()).append(" \r\n");
        response.getHeaders().forEach((key, values) -> {
            for (String value : values) {
                sb.append(key).append(": ").append(value).append("\r\n");
            }
        });
        sb.append("Content-Length: ").append(response.getBody().getBytes().length).append("\r\n");
        sb.append("\r\n");
        sb.append(response.getBody());

        outputStream.write(sb.toString().getBytes());
        outputStream.flush();
    }
}
