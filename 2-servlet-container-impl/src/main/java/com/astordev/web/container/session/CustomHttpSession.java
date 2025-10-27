package com.astordev.web.container.session;

import com.astordev.web.container.context.Context;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.*;

import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CustomHttpSession implements HttpSession {

    private final String id;
    private final long creationTime;
    private long lastAccessedTime;
    private final Context context;
    private final SessionManager sessionManager;
    private boolean isValid = true;
    private boolean isNew = true;
    private int maxInactiveInterval = 1800; // 30 minutes

    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    public CustomHttpSession(String id, Context context, SessionManager sessionManager) {
        this.id = id;
        this.context = context;
        this.sessionManager = sessionManager;
        this.creationTime = System.currentTimeMillis();
        this.lastAccessedTime = this.creationTime;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public long getLastAccessedTime() {
        return lastAccessedTime;
    }

    public void access() {
        this.lastAccessedTime = System.currentTimeMillis();
        this.isNew = false;
    }

    @Override
    public ServletContext getServletContext() {
        return context.getServletContext();
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        this.maxInactiveInterval = interval;
    }

    @Override
    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
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
    public void setAttribute(String name, Object value) {
        if (value == null) {
            removeAttribute(name);
            return;
        }
        Object oldValue = attributes.put(name, value);
        fireAttributeAddedEvent(name, value);
        if (oldValue != null) {
            fireAttributeReplacedEvent(name, oldValue);
        }
    }

    @Override
    public void removeAttribute(String name) {
        Object value = attributes.remove(name);
        if (value != null) {
            fireAttributeRemovedEvent(name, value);
        }
    }

    @Override
    public void invalidate() {
        if (!isValid) {
            throw new IllegalStateException("Session has already been invalidated.");
        }
        this.isValid = false;
        sessionManager.removeSession(this.id);
        fireSessionDestroyedEvent();
        attributes.clear();
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    public boolean isValid() {
        if (!isValid) {
            return false;
        }
        if (maxInactiveInterval >= 0) {
            long idleTime = System.currentTimeMillis() - lastAccessedTime;
            if (idleTime > maxInactiveInterval * 1000L) {
                invalidate();
                return false;
            }
        }
        return true;
    }

    private void fireAttributeAddedEvent(String name, Object value) {
        HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, name, value);
        for (EventListener listener : context.getServletContext().getListeners()) {
            if (listener instanceof HttpSessionAttributeListener) {
                ((HttpSessionAttributeListener) listener).attributeAdded(event);
            }
        }
    }

    private void fireAttributeRemovedEvent(String name, Object value) {
        HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, name, value);
        for (EventListener listener : context.getServletContext().getListeners()) {
            if (listener instanceof HttpSessionAttributeListener) {
                ((HttpSessionAttributeListener) listener).attributeRemoved(event);
            }
        }
    }

    private void fireAttributeReplacedEvent(String name, Object value) {
        HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, name, value);
        for (EventListener listener : context.getServletContext().getListeners()) {
            if (listener instanceof HttpSessionAttributeListener) {
                ((HttpSessionAttributeListener) listener).attributeReplaced(event);
            }
        }
    }

    private void fireSessionDestroyedEvent() {
        HttpSessionEvent event = new HttpSessionEvent(this);
        for (EventListener listener : context.getServletContext().getListeners()) {
            if (listener instanceof HttpSessionListener) {
                ((HttpSessionListener) listener).sessionDestroyed(event);
            }
        }
    }
}
