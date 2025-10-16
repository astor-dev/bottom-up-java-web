package com.astordev.web.container.http;

import com.astordev.web.bridge.Response;
import com.astordev.web.container.connector.BridgeOutputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class HttpResponse implements HttpServletResponse {

    private final Response response;
    private boolean committed = false;
    private PrintWriter writer;
    private final ServletOutputStream outputStream;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private String characterEncoding = StandardCharsets.UTF_8.name();

    public HttpResponse(Response response) {
        this.response = response;
        this.outputStream = new BridgeOutputStream(buffer);
    }

    @Override
    public void setStatus(int sc) {
        if (isCommitted()) return;
        response.setStatus(sc);
    }

    @Override
    public int getStatus() {
        return response.getStatus();
    }

    @Override
    public void addHeader(String name, String value) {
        if (isCommitted()) return;
        response.addHeader(name, value);
    }

    @Override
    public void setHeader(String name, String value) {
        if (isCommitted()) return;
        response.setHeader(name, value);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (writer != null) {
            throw new IllegalStateException("getWriter() has already been called for this request");
        }
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            writer = new PrintWriter(new java.io.OutputStreamWriter(buffer, characterEncoding));
        }
        return writer;
    }

    @Override
    public void flushBuffer() throws IOException {
        if (writer != null) {
            writer.flush();
        }
        outputStream.flush();

        if (!committed) {
            byte[] bytes = buffer.toByteArray();
            response.setBody(new String(bytes, Charset.forName(characterEncoding)));
            if (!response.getHeaders().containsKey("Content-Length")) {
                setContentLength(bytes.length);
            }
            committed = true;
        }
    }

    @Override
    public boolean isCommitted() {
        return committed;
    }

    @Override
    public void setContentLength(int len) {
        if (isCommitted()) return;
        setHeader("Content-Length", String.valueOf(len));
    }

    @Override
    public void setContentLengthLong(long len) {
        if (isCommitted()) return;
        setHeader("Content-Length", String.valueOf(len));
    }

    @Override
    public void setContentType(String type) {
        if (isCommitted()) return;
        setHeader("Content-Type", type);
    }

    @Override
    public String getContentType() {
        return getHeader("Content-Type");
    }

    @Override
    public String getCharacterEncoding() {
        return characterEncoding;
    }

    @Override
    public void setCharacterEncoding(String charset) {
        if (isCommitted()) return;
        this.characterEncoding = charset;
    }

    @Override
    public void reset() {
        if (isCommitted()) {
            throw new IllegalStateException("Cannot reset response after it has been committed");
        }
        response.getHeaders().clear();
        response.setStatus(200);
        resetBuffer();
    }

    @Override
    public void resetBuffer() {
        if (isCommitted()) {
            throw new IllegalStateException("Cannot reset buffer after response has been committed");
        }
        buffer.reset();
    }

    @Override
    public void addCookie(Cookie cookie) {
        if (cookie == null || isCommitted()) return;
        addHeader("Set-Cookie", formatCookie(cookie));
    }

    private String formatCookie(Cookie cookie) {
        StringBuilder sb = new StringBuilder();
        sb.append(cookie.getName()).append("=").append(cookie.getValue());
        if (cookie.getPath() != null) sb.append("; Path=").append(cookie.getPath());
        if (cookie.getDomain() != null) sb.append("; Domain=").append(cookie.getDomain());
        if (cookie.getMaxAge() >= 0) sb.append("; Max-Age=").append(cookie.getMaxAge());
        if (cookie.getSecure()) sb.append("; Secure");
        if (cookie.isHttpOnly()) sb.append("; HttpOnly");
        return sb.toString();
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        if (isCommitted()) {
            throw new IllegalStateException("Cannot send redirect after response is committed");
        }
        setStatus(SC_FOUND);
        setHeader("Location", location);
        resetBuffer();
        flushBuffer();
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        if (isCommitted()) {
            throw new IllegalStateException("Cannot send error after response is committed");
        }
        setStatus(sc);
        setContentType("text/html");
        resetBuffer();
        getWriter().println("<html><body><h1>" + sc + " " + msg + "</h1></body></html>");
        flushBuffer();
    }

    @Override
    public void sendError(int sc) throws IOException {
        sendError(sc, "Error");
    }

    @Override
    public boolean containsHeader(String name) { return response.getHeaders().containsKey(name); }
    @Override
    public String encodeURL(String url) { return url; }
    @Override
    public String encodeRedirectURL(String url) { return url; }
    @Override
    public void setDateHeader(String name, long date) { setHeader(name, String.valueOf(date)); }
    @Override
    public void addDateHeader(String name, long date) { addHeader(name, String.valueOf(date)); }
    @Override
    public void setIntHeader(String name, int value) { setHeader(name, String.valueOf(value)); }
    @Override
    public void addIntHeader(String name, int value) { addHeader(name, String.valueOf(value)); }
    @Override
    public int getBufferSize() { return buffer.size(); }
    @Override
    public void setBufferSize(int size) { /* no-op */ }
    @Override
    public java.util.Locale getLocale() { return null; }
    @Override
    public void setLocale(java.util.Locale loc) { /* no-op */ }
    @Override
    public String getHeader(String name) {
        List<String> values = response.getHeaders().get(name);
        return values != null && !values.isEmpty() ? values.get(0) : null;
    }
    @Override
    public Collection<String> getHeaders(String name) {
        return response.getHeaders().getOrDefault(name, List.of());
    }
    @Override
    public Collection<String> getHeaderNames() {
        return response.getHeaders().keySet();
    }

    @Override
    public void sendRedirect(String location, boolean clearBuffer) throws IOException {
        HttpServletResponse.super.sendRedirect(location, clearBuffer);
    }

    @Override
    public void sendRedirect(String location, int sc) throws IOException {
        HttpServletResponse.super.sendRedirect(location, sc);
    }

    @Override
    public void sendRedirect(String location, int sc, boolean clearBuffer) {

    }

    @Override
    public void setCharacterEncoding(Charset encoding) {
        HttpServletResponse.super.setCharacterEncoding(encoding);
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
