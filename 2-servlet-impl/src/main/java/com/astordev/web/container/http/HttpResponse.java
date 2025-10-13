package com.astordev.web.container.http;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Supplier;

public class HttpResponse implements HttpServletResponse {

    private final OutputStream outputStream;
    private final Map<String, List<String>> headers = new HashMap<>();
    private PrintWriter writer;
    private int status = SC_OK;
    private String contentType = "text/html";
    private boolean headersWritten = false;

    public HttpResponse(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    private void writeHeaders() {
        if (!headersWritten) {
            PrintWriter headerWriter = new PrintWriter(outputStream);
            headerWriter.print("HTTP/1.1 " + status + " " + getStatusMessage(status) + "\r\n");
            headerWriter.print("Content-Type: " + contentType + "\r\n");
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                for (String value : entry.getValue()) {
                    headerWriter.print(entry.getKey() + ": " + value + "\r\n");
                }
            }
            headerWriter.print("\r\n");
            headerWriter.flush();
            headersWritten = true;
        }
    }

    private String getStatusMessage(int sc) {
        return switch (sc) {
            case SC_OK -> "OK";
            case SC_FOUND -> "Found";
            case SC_NOT_FOUND -> "Not Found";
            case SC_METHOD_NOT_ALLOWED -> "Method Not Allowed";
            case SC_INTERNAL_SERVER_ERROR -> "Internal Server Error";
            default -> "Unknown";
        };
    }

    @Override
    public PrintWriter getWriter() {
        if (writer == null) {
            writeHeaders();
            writer = new PrintWriter(outputStream, true);
        }
        return writer;
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        if (isCommitted()) {
            throw new IllegalStateException("Cannot send error after response is committed");
        }
        setStatus(sc);
        setContentType("text/html");
        getWriter().println("<html><body><h1>" + sc + " " + msg + "</h1></body></html>");
        flushBuffer();
    }

    @Override
    public void flushBuffer() throws IOException {
        if (writer != null) {
            writer.flush();
        }
        outputStream.flush();
    }

    @Override
    public void addCookie(Cookie cookie) {
        if (cookie == null) return;
        addHeader("Set-Cookie", formatCookie(cookie));
    }

    private String formatCookie(Cookie cookie) {
        StringBuilder sb = new StringBuilder();
        sb.append(cookie.getName()).append("=").append(cookie.getValue());
        if (cookie.getPath() != null) {
            sb.append("; Path=").append(cookie.getPath());
        }
        if (cookie.getDomain() != null) {
            sb.append("; Domain=").append(cookie.getDomain());
        }
        if (cookie.getMaxAge() >= 0) {
            sb.append("; Max-Age=").append(cookie.getMaxAge());
        }
        if (cookie.getSecure()) {
            sb.append("; Secure");
        }
        if (cookie.isHttpOnly()) {
            sb.append("; HttpOnly");
        }
        return sb.toString();
    }

    @Override
    public void sendRedirect(String location) {
        if (isCommitted()) {
            throw new IllegalStateException("Cannot send redirect after response is committed");
        }
        setStatus(SC_FOUND);
        setHeader("Location", location);
        resetBuffer();
        writeHeaders();
    }

    // --- Unimplemented methods ---
    @Override
    public boolean containsHeader(String name) {
        return headers.containsKey(name);
    }

    @Override
    public String encodeURL(String url) {
        return url;
    }

    @Override
    public String encodeRedirectURL(String url) {
        return url;
    }

    @Override
    public void sendError(int sc) throws IOException {
        sendError(sc, getStatusMessage(sc));
    }

    @Override
    public void sendRedirect(String location, boolean clearBuffer) throws IOException {
        HttpServletResponse.super.sendRedirect(location, clearBuffer);
    }

    @Override
    public void sendRedirect(String location, int sc) throws IOException {
        HttpServletResponse.super.sendRedirect(location, sc);
    }

    // --- Unimplemented methods ---

    @Override
    public void sendRedirect(String location, int sc, boolean clearBuffer) {

    }

    @Override
    public void setDateHeader(String name, long date) {
    }

    @Override
    public void addDateHeader(String name, long date) {
    }

    @Override
    public void setHeader(String name, String value) {
        List<String> values = headers.computeIfAbsent(name, k -> new ArrayList<>());
        values.clear();
        values.add(value);
    }

    @Override
    public void addHeader(String name, String value) {
        headers.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        setHeader(name, String.valueOf(value));
    }

    @Override
    public void addIntHeader(String name, int value) {
        addHeader(name, String.valueOf(value));
    }

    @Override
    public String getCharacterEncoding() {
        return "UTF-8";
    }

    @Override
    public void setCharacterEncoding(String charset) {
    }

    @Override
    public void setCharacterEncoding(Charset encoding) {
        HttpServletResponse.super.setCharacterEncoding(encoding);
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public void setContentType(String type) {
        this.contentType = type;
    }

    @Override
    public ServletOutputStream getOutputStream() {
        writeHeaders();
        return new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                outputStream.write(b);
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(jakarta.servlet.WriteListener writeListener) {
            }
        };
    }

    @Override
    public void setContentLength(int len) {
        setIntHeader("Content-Length", len);
    }

    @Override
    public void setContentLengthLong(long len) {
        setHeader("Content-Length", String.valueOf(len));
    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public void setBufferSize(int size) {
    }

    @Override
    public void reset() {
        if (isCommitted()) {
            throw new IllegalStateException("Cannot reset response after it has been committed");
        }
        headers.clear();
        status = SC_OK;
        contentType = "text/html";
    }

    @Override
    public void resetBuffer() {
        if (isCommitted()) {
            throw new IllegalStateException("Cannot reset buffer after response has been committed");
        }
    }

    @Override
    public boolean isCommitted() {
        return headersWritten;
    }

    @Override
    public java.util.Locale getLocale() {
        return null;
    }

    @Override
    public void setLocale(java.util.Locale loc) {
    }

    @Override
    public int getStatus() {
        return this.status;
    }

    @Override
    public void setStatus(int sc) {
        this.status = sc;
    }

    @Override
    public String getHeader(String name) {
        List<String> values = headers.get(name);
        return values != null && !values.isEmpty() ? values.getFirst() : null;
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return headers.getOrDefault(name, Collections.emptyList());
    }

    @Override
    public Collection<String> getHeaderNames() {
        return headers.keySet();
    }

    @Override
    public Supplier<Map<String, String>> getTrailerFields() {
        return HttpServletResponse.super.getTrailerFields();
    }

    @Override
    public void setTrailerFields(Supplier<Map<String, String>> supplier) {
        HttpServletResponse.super.setTrailerFields(supplier);
    }
}