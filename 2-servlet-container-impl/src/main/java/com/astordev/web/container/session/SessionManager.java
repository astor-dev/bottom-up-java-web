package com.astordev.web.container.session;

import com.astordev.web.container.context.Context;
import com.astordev.web.container.http.HttpResponse;
import jakarta.servlet.http.*;

import java.util.EventListener;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private final Map<String, HttpSession> sessions = new ConcurrentHashMap<>();
    private final Context context;

    public SessionManager(Context context) {
        this.context = context;
    }

    public HttpSession createSession(HttpResponse response) {
        String sessionId = UUID.randomUUID().toString();
        CustomHttpSession session = new CustomHttpSession(sessionId, context, this);
        sessions.put(sessionId, session);

        Cookie cookie = new Cookie("JSESSIONID", sessionId);
        cookie.setPath("/");
        response.addCookie(cookie);

        fireSessionCreatedEvent(session);
        return session;
    }

    public HttpSession findSession(String id) {
        if (id == null) {
            return null;
        }
        CustomHttpSession session = (CustomHttpSession) sessions.get(id);
        if (session != null && session.isValid()) {
            session.access();
            return session;
        }
        return null;
    }

    public void removeSession(String id) {
        sessions.remove(id);
    }

    private void fireSessionCreatedEvent(HttpSession session) {
        HttpSessionEvent event = new HttpSessionEvent(session);
        for (EventListener listener : context.getServletContext().getListeners()) {
            if (listener instanceof HttpSessionListener) {
                ((HttpSessionListener) listener).sessionCreated(event);
            }
        }
    }
}
