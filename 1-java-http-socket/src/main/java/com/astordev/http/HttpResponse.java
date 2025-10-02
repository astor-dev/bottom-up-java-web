package com.astordev.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {

    private final OutputStream outputStream;
    private final Map<String, String> headers = new HashMap<>();
    private int status = 200;
    private String statusMessage = "OK";
    private boolean committed = false;

    private final ByteArrayOutputStream bodyContent = new ByteArrayOutputStream();
    private final PrintWriter writer = new PrintWriter(bodyContent, false, StandardCharsets.UTF_8);

    public HttpResponse(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public void setStatus(int status) {
        this.status = status;
        this.statusMessage = getStatusMessage(status);
    }

    private String getStatusMessage(int sc) {
        return switch (sc) {
            case 200 -> "OK";
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            default -> "Unknown";
        };
    }

    public void addHeader(String name, String value) {
        if (committed) {
            return;
        }
        headers.put(name, value);
    }

    public void sendError(int sc, String msg) {
        if (committed) {
            return;
        }
        setStatus(sc);
        addHeader("Content-Type", "text/html; charset=utf-8");
        bodyContent.reset();
        writer.println("<html><body><h1>" + sc + " " + msg + "</h1></body></html>");
    }

    public void flushBuffer() throws IOException {
        if (committed) {
            return;
        }

        writer.flush();
        byte[] bodyBytes = bodyContent.toByteArray();

        if (!headers.containsKey("Content-Length")) {
            addHeader("Content-Length", String.valueOf(bodyBytes.length));
        }

        PrintWriter headerWriter = new PrintWriter(outputStream, false, StandardCharsets.UTF_8);
        headerWriter.println("HTTP/1.1 " + status + " " + statusMessage);
        if (!headers.containsKey("Content-Type")) {
            headers.put("Content-Type", "text/plain; charset=utf-8");
        }
        headers.forEach((key, value) -> headerWriter.println(key + ": " + value));
        headerWriter.println();
        headerWriter.flush();

        outputStream.write(bodyBytes);
        outputStream.flush();

        committed = true;
    }
}