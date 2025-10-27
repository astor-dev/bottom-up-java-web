package com.astordev.web.container.http;

import com.astordev.web.bridge.Request;
import com.astordev.web.container.connector.BridgeInputStream;
import com.astordev.web.container.connector.Connector;
import com.astordev.web.container.context.Context;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConnection;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.*;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HttpRequest implements HttpServletRequest {

    private final Request request;
    private final Connector connector;
    private final Context context;
    private HttpResponse response;
    private HttpSession session;
    private String requestedSessionId;
    private boolean requestedSessionIdFromCookie;

    private final String queryString;
    private final Map<String, String[]> parameters = new HashMap<>();
    private final Cookie[] cookies;
    private ServletInputStream servletInputStream;
    private BufferedReader reader;
    private boolean streamAccessed = false;

    public HttpRequest(Request request, Connector connector, Context context) {
        this.request = request;
        this.connector = connector;
        this.context = context;

        String uri = request.getRequestURI();
        int queryStringIndex = uri.indexOf('?');
        if (queryStringIndex != -1) {
            this.queryString = uri.substring(queryStringIndex + 1);
            parseParameters(this.queryString);
        } else {
            this.queryString = null;
        }

        if ("POST".equalsIgnoreCase(getMethod()) && "application/x-www-form-urlencoded".equalsIgnoreCase(getContentType())) {
            parseParameters(request.getBody());
        }

        this.cookies = parseCookies(request.getHeader("Cookie"));
        if (this.cookies != null) {
            for (Cookie cookie : this.cookies) {
                if ("JSESSIONID".equals(cookie.getName())) {
                    this.requestedSessionId = cookie.getValue();
                    this.requestedSessionIdFromCookie = true;
                    break;
                }
            }
        }
    }

    public void setResponse(HttpResponse response) {
        this.response = response;
    }

    private void parseParameters(String data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        String[] pairs = data.split("&");
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
        return this.request.getMethod();
    }

    @Override
    public String getRequestURI() {
        String uri = this.request.getRequestURI();
        int queryStringIndex = uri.indexOf('?');
        if (queryStringIndex != -1) {
            return uri.substring(0, queryStringIndex);
        }
        return uri;
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
        return request.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        String header = request.getHeader(name);
        return Collections.enumeration(header != null ? Collections.singletonList(header) : Collections.emptyList());
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(request.getHeaders().keySet());
    }

    @Override
    public int getIntHeader(String name) {
        String value = getHeader(name);
        return value != null ? Integer.parseInt(value) : -1;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (reader != null) {
            throw new IllegalStateException("getReader() has already been called for this request");
        }
        if (servletInputStream == null) {
            String body = request.getBody();
            if (body == null) {
                body = "";
            }
            servletInputStream = new BridgeInputStream(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
        }
        streamAccessed = true;
        return this.servletInputStream;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (streamAccessed && reader == null) {
            throw new IllegalStateException("getInputStream() has already been called for this request");
        }
        if (reader != null) {
            return reader;
        }

        String encoding = getCharacterEncoding();
        if (encoding == null) {
            encoding = StandardCharsets.UTF_8.name();
        }
        this.reader = new BufferedReader(new InputStreamReader(getInputStream(), encoding));
        streamAccessed = true;
        return this.reader;
    }

    @Override
    public String getContentType() {
        return getHeader("Content-Type");
    }

    @Override
    public String getPathInfo() { return null; }
    @Override
    public String getPathTranslated() { return null; }
    @Override
    public String getContextPath() { return ""; }
    @Override
    public String getRemoteUser() { return null; }
    @Override
    public boolean isUserInRole(String role) { return false; }
    @Override
    public java.security.Principal getUserPrincipal() { return null; }
    @Override
    public String getRequestedSessionId() { return this.requestedSessionId; }
    @Override
    public HttpSession getSession(boolean create) {
        if (session != null && ((com.astordev.web.container.session.CustomHttpSession) session).isValid()) {
            return session;
        }

        session = context.getSessionManager().findSession(requestedSessionId);

        if (session == null && create) {
            session = context.getSessionManager().createSession(this.response);
        }

        return session;
    }
    @Override
    public HttpSession getSession() { return getSession(true); }
    @Override
    public String changeSessionId() { return null; }
    @Override
    public boolean isRequestedSessionIdValid() {
        return context.getSessionManager().findSession(requestedSessionId) != null;
    }
    @Override
    public boolean isRequestedSessionIdFromCookie() { return this.requestedSessionIdFromCookie; }
    @Override
    public boolean isRequestedSessionIdFromURL() { return false; }
    @Override
    public Object getAttribute(String name) { return null; }
    @Override
    public Enumeration<String> getAttributeNames() { return Collections.emptyEnumeration(); }
    @Override
    public String getCharacterEncoding() { return "UTF-8"; }
    @Override
    public void setCharacterEncoding(String env) { }
    @Override
    public int getContentLength() {
        String contentLength = getHeader("Content-Length");
        if (contentLength == null) {
            return -1;
        }
        return Integer.parseInt(contentLength);
    }
    @Override
    public long getContentLengthLong() {
        String contentLength = getHeader("Content-Length");
        if (contentLength == null) {
            return -1L;
        }
        return Long.parseLong(contentLength);
    }
    @Override
    public String getProtocol() { return connector.protocol; }
    @Override
    public String getScheme() { return connector.scheme; }
    @Override
    public String getServerName() { return "localhost"; }
    @Override
    public int getServerPort() { return connector.port; }
    @Override
    public String getRemoteAddr() { return null; }
    @Override
    public String getRemoteHost() { return null; }
    @Override
    public void setAttribute(String name, Object o) { }
    @Override
    public void removeAttribute(String name) { }
    @Override
    public java.util.Locale getLocale() { return java.util.Locale.getDefault(); }
    @Override
    public Enumeration<java.util.Locale> getLocales() { return Collections.enumeration(Collections.singletonList(java.util.Locale.getDefault())); }
    @Override
    public boolean isSecure() { return false; }
    @Override
    public RequestDispatcher getRequestDispatcher(String path) { return null; }
    @Override
    public int getRemotePort() { return 0; }
    @Override
    public String getLocalName() { return null; }
    @Override
    public String getLocalAddr() { return null; }
    public int getLocalPort() { return 0; }
    @Override
    public ServletContext getServletContext() { return context.getServletContext(); }
    @Override
    public jakarta.servlet.AsyncContext startAsync() throws IllegalStateException { return null; }
    @Override
    public jakarta.servlet.AsyncContext startAsync(jakarta.servlet.ServletRequest servletRequest, jakarta.servlet.ServletResponse servletResponse) throws IllegalStateException { return null; }
    @Override
    public boolean isAsyncStarted() { return false; }
    @Override
    public boolean isAsyncSupported() { return false; }
    @Override
    public jakarta.servlet.AsyncContext getAsyncContext() { return null; }
    @Override
    public jakarta.servlet.DispatcherType getDispatcherType() { return jakarta.servlet.DispatcherType.REQUEST; }
    @Override
    public String getRequestId() { return ""; }
    @Override
    public String getProtocolRequestId() { return ""; }
    @Override
    public ServletConnection getServletConnection() { return null; }
    @Override
    public String getAuthType() { return null; }
    @Override
    public long getDateHeader(String name) { return -1; }
    @Override
    public StringBuffer getRequestURL() { return new StringBuffer(getScheme() + "://" + getServerName() + ":" + getServerPort() + getRequestURI()); }
    @Override
    public String getServletPath() { return ""; }
    @Override
    public jakarta.servlet.http.Part getPart(String name) { return null; }
    @Override
    public java.util.Collection<jakarta.servlet.http.Part> getParts() { return Collections.emptyList(); }
    @Override
    public boolean authenticate(HttpServletResponse response) { return false; }
    @Override
    public void login(String username, String password) { }
    @Override
    public void logout() { }
    @Override
    public <T extends jakarta.servlet.http.HttpUpgradeHandler> T upgrade(Class<T> handlerClass) { return null; }
}
