package com.astordev.was.container.http;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConnection;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HttpRequest implements HttpServletRequest {

    private final String method;
    private final String requestURI;
    private final String queryString;
    private final Map<String, String> headers = new HashMap<>();
    private final Map<String, String[]> parameters = new HashMap<>();
    private final Cookie[] cookies;
    private final BufferedReader reader;

    public HttpRequest(InputStream inputStream) throws IOException {
        this.reader = new BufferedReader(new InputStreamReader(inputStream));
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            throw new IOException("Invalid request line");
        }

        String[] parts = requestLine.split(" ");
        this.method = parts[0];
        String uri = parts[1];

        int queryStringIndex = uri.indexOf('?');
        if (queryStringIndex != -1) {
            this.requestURI = uri.substring(0, queryStringIndex);
            this.queryString = uri.substring(queryStringIndex + 1);
            parseParameters(this.queryString);
        } else {
            this.requestURI = uri;
            this.queryString = null;
        }

        String headerLine;
        while ((headerLine = reader.readLine()) != null && !headerLine.isEmpty()) {
            String[] headerParts = headerLine.split(": ");
            if (headerParts.length == 2) {
                headers.put(headerParts[0], headerParts[1]);
            }
        }

        this.cookies = parseCookies(getHeader("Cookie"));
    }

    private void parseParameters(String queryString) {
        if (queryString == null || queryString.isEmpty()) {
            return;
        }
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8) : pair;
            String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8) : null;
            parameters.computeIfAbsent(key, k -> new String[0]);
            if (value != null) {
                String[] values = parameters.get(key);
                String[] newValues = Arrays.copyOf(values, values.length + 1);
                newValues[values.length] = value;
                parameters.put(key, newValues);
            }
        }
    }

    private Cookie[] parseCookies(String cookieHeader) {
        if (cookieHeader == null) {
            return null;
        }
        List<Cookie> cookieList = new ArrayList<>();
        String[] cookiePairs = cookieHeader.split("; ");
        for (String cookiePair : cookiePairs) {
            int idx = cookiePair.indexOf("=");
            if (idx != -1) {
                String name = cookiePair.substring(0, idx);
                String value = cookiePair.substring(idx + 1);
                cookieList.add(new Cookie(name, value));
            }
        }
        return cookieList.toArray(new Cookie[0]);
    }


    @Override
    public String getMethod() {
        return this.method;
    }

    @Override
    public String getRequestURI() {
        return this.requestURI;
    }

    @Override
    public String getQueryString() {
        return this.queryString;
    }

    @Override
    public String getParameter(String name) {
        String[] values = parameters.get(name);
        if (values != null && values.length > 0) {
            return values[0];
        }
        return null;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(parameters.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        return parameters.get(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return Collections.unmodifiableMap(parameters);
    }

    @Override
    public Cookie[] getCookies() {
        return this.cookies;
    }


    @Override
    public String getHeader(String name) {
        return headers.get(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return Collections.enumeration(headers.containsKey(name) ? Collections.singletonList(headers.get(name)) : Collections.emptyList());
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(headers.keySet());
    }

    @Override
    public int getIntHeader(String name) {
        String value = getHeader(name);
        return value != null ? Integer.parseInt(value) : -1;
    }

    // --- Unimplemented methods ---

    @Override
    public String getPathInfo() {
        return null;
    }

    @Override
    public String getPathTranslated() {
        return null;
    }

    @Override
    public String getContextPath() {
        return "";
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    @Override
    public java.security.Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return null;
    }

    @Override
    public HttpSession getSession(boolean create) {
        return null;
    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public String changeSessionId() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public Object getAttribute(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.emptyEnumeration();
    }

    @Override
    public String getCharacterEncoding() {
        return "UTF-8";
    }

    @Override
    public void setCharacterEncoding(String env) {
    }

    @Override
    public int getContentLength() {
        return -1;
    }

    @Override
    public long getContentLengthLong() {
        return -1;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public ServletInputStream getInputStream() {
        return null;
    }

    @Override
    public String getProtocol() {
        return "HTTP/1.1";
    }

    @Override
    public String getScheme() {
        return "http";
    }

    @Override
    public String getServerName() {
        return "localhost";
    }

    @Override
    public int getServerPort() {
        return 8080;
    }

    @Override
    public BufferedReader getReader() {
        return this.reader;
    }

    @Override
    public String getRemoteAddr() {
        return null;
    }

    @Override
    public String getRemoteHost() {
        return null;
    }

    @Override
    public void setAttribute(String name, Object o) {
    }

    @Override
    public void removeAttribute(String name) {
    }

    @Override
    public java.util.Locale getLocale() {
        return java.util.Locale.getDefault();
    }

    @Override
    public Enumeration<java.util.Locale> getLocales() {
        return Collections.enumeration(Collections.singletonList(java.util.Locale.getDefault()));
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public String getLocalAddr() {
        return null;
    }

    @Override
    public int getLocalPort() {
        return 0;
    }

    @Override
    public jakarta.servlet.ServletContext getServletContext() {
        return null;
    }

    @Override
    public jakarta.servlet.AsyncContext startAsync() throws IllegalStateException {
        return null;
    }

    @Override
    public jakarta.servlet.AsyncContext startAsync(jakarta.servlet.ServletRequest servletRequest, jakarta.servlet.ServletResponse servletResponse) throws IllegalStateException {
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public jakarta.servlet.AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public jakarta.servlet.DispatcherType getDispatcherType() {
        return jakarta.servlet.DispatcherType.REQUEST;
    }

    @Override
    public String getRequestId() {
        return "";
    }

    @Override
    public String getProtocolRequestId() {
        return "";
    }

    @Override
    public ServletConnection getServletConnection() {
        return null;
    }

    @Override
    public String getAuthType() {
        return null;
    }

    @Override
    public long getDateHeader(String name) {
        return -1;
    }

    @Override
    public StringBuffer getRequestURL() {
        return new StringBuffer(getScheme() + "://" + getServerName() + ":" + getServerPort() + getRequestURI());
    }

    @Override
    public String getServletPath() {
        return "";
    }

    @Override
    public jakarta.servlet.http.Part getPart(String name) {
        return null;
    }

    @Override
    public java.util.Collection<jakarta.servlet.http.Part> getParts() {
        return Collections.emptyList();
    }

    @Override
    public boolean authenticate(HttpServletResponse response) {
        return false;
    }

    @Override
    public void login(String username, String password) {
    }

    @Override
    public void logout() {
    }

    @Override
    public <T extends jakarta.servlet.http.HttpUpgradeHandler> T upgrade(Class<T> handlerClass) {
        return null;
    }
}