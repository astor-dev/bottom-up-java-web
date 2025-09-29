package com.astordev.http;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {

    private final OutputStream outputStream;
    private final Map<String, String> headers = new HashMap<>();
    private PrintWriter writer;
    private int status = 200;
    private String statusMessage = "OK";
    private boolean headersSent = false;

    public HttpResponse(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    private void sendHeaders() {
        if (!headersSent) {
            PrintWriter headerWriter = new PrintWriter(outputStream);
            headerWriter.println("HTTP/1.1 " + status + " " + statusMessage);
            if (!headers.containsKey("Content-Type")) {
                headers.put("Content-Type", "text/plain");
            }
            headers.forEach((key, value) -> headerWriter.println(key + ": " + value));
            headerWriter.println();
            headerWriter.flush();
            headersSent = true;
        }
    }

    public PrintWriter getWriter() {
        if (writer == null) {
            sendHeaders();
            this.writer = new PrintWriter(outputStream, true);
        }
        return writer;
    }

    public void setStatus(int status) {
        this.status = status;
        this.statusMessage = getStatusMessage(status);
    }

    private String getStatusMessage(int sc) {
        return switch (sc) {
            case 200 -> "OK";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            default -> "Unknown";
        };
    }

    public void addHeader(String name, String value) {
        if (headersSent) {
            return; // Or throw exception
        }
        headers.put(name, value);
    }

    public void sendError(int sc, String msg) {
        if (headersSent) {
            return;
        }
        setStatus(sc);
        addHeader("Content-Type", "text/html");
        getWriter().println("<html><body><h1>" + sc + " " + msg + "</h1></body></html>");
    }
}
