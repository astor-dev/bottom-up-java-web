package com.astordev.web.container.context;

import jakarta.servlet.*;
import jakarta.servlet.descriptor.JspConfigDescriptor;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CustomServletContext implements ServletContext {

    private final Map<String, ServletRegistration> servletRegistrations = new ConcurrentHashMap<>();
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();


    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
        if (servletName == null || servletName.isEmpty()) {
            throw new IllegalArgumentException("Servlet name cannot be null or empty");
        }
        if (servletClass == null) {
            throw new IllegalArgumentException("Servlet class cannot be null");
        }
        if (servletRegistrations.containsKey(servletName)) {
            return null;
        }

        CustomServletRegistration registration = new CustomServletRegistration(servletName, servletClass);
        servletRegistrations.put(servletName, registration);
        return registration;
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    @Override
    public void setAttribute(String name, Object object) {
        if (object == null) {
            removeAttribute(name);
        } else {
            attributes.put(name, object);
        }
    }

    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    // --- Unimplemented methods ---

    @Override
    public String getContextPath() {
        return "";
    }

    @Override
    public ServletContext getContext(String uripath) {
        return null;
    }

    @Override
    public int getMajorVersion() {
        return 5;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public int getEffectiveMajorVersion() {
        return 5;
    }

    @Override
    public int getEffectiveMinorVersion() {
        return 0;
    }

    @Override
    public String getMimeType(String file) {
        return null;
    }

    @Override
    public Set<String> getResourcePaths(String path) {
        return null;
    }

    @Override
    public URL getResource(String path) throws MalformedURLException {
        return null;
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        return null;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String name) {
        return null;
    }

    @Override
    public void log(String msg) {
        System.out.println(msg);
    }

    @Override
    public void log(String message, Throwable throwable) {
        System.err.println(message);
        throwable.printStackTrace(System.err);
    }

    @Override
    public String getRealPath(String path) {
        return null;
    }

    @Override
    public String getServerInfo() {
        return "CustomServletContainer/1.0";
    }

    @Override
    public String getInitParameter(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.emptyEnumeration();
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        return false;
    }

    @Override
    public String getServletContextName() {
        return "CustomServletContext";
    }

    @Override
    public ServletRegistration getServletRegistration(String servletName) {
        return servletRegistrations.get(servletName);
    }

    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return Collections.unmodifiableMap(servletRegistrations);
    }

    // --- Methods below are for more advanced features (Filters, Listeners, etc.) ---

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, String className) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
        return null;
    }



    @Override
    public ServletRegistration.Dynamic addJspFile(String servletName, String jspFile) {
        return null;
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, String className) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FilterRegistration getFilterRegistration(String filterName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addListener(String className) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends EventListener> void addListener(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addListener(Class<? extends EventListener> listenerClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    @Override
    public void declareRoles(String... roleNames) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getVirtualServerName() {
        return null;
    }

    @Override
    public int getSessionTimeout() {
        return 0;
    }

    @Override
    public void setSessionTimeout(int sessionTimeout) {

    }

    @Override
    public String getRequestCharacterEncoding() {
        return null;
    }

    @Override
    public void setRequestCharacterEncoding(String encoding) {

    }

    @Override
    public void setRequestCharacterEncoding(Charset encoding) {
        ServletContext.super.setRequestCharacterEncoding(encoding);
    }

    @Override
    public String getResponseCharacterEncoding() {
        return null;
    }

    @Override
    public void setResponseCharacterEncoding(String encoding) {

    }

    @Override
    public void setResponseCharacterEncoding(Charset encoding) {
        ServletContext.super.setResponseCharacterEncoding(encoding);
    }
}
