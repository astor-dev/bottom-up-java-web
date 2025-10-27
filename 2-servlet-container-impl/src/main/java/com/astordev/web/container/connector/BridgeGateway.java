package com.astordev.web.container.connector;

import com.astordev.web.bridge.Gateway;
import com.astordev.web.bridge.Request;
import com.astordev.web.bridge.Response;
import com.astordev.web.container.ServletMapper;
import com.astordev.web.container.context.Context;
import com.astordev.web.container.http.HttpRequest;
import com.astordev.web.container.http.HttpResponse;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.EventListener;

public class BridgeGateway implements Gateway {
    private final Context context;
    private final ServletMapper mapper;
    private final Connector connector;


    public BridgeGateway(Context context, Connector connector) {
        this.context = context;
        this.mapper = context.getServletMapper();
        this.connector = connector;
    }

    @Override
    public void service(Request request, Response response)  {
        HttpRequest httpRequest = connector.createRequest(request);
        HttpResponse httpResponse = connector.createResponse(response);

        fireRequestInitializedEvent(httpRequest);

        try {
            String requestURI = request.getRequestURI();
            Servlet servlet = mapper.map(requestURI);
            if (servlet != null) {
                servlet.service(httpRequest, httpResponse);
                httpResponse.flushBuffer();
            } else {
                httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "Not Found");
            }
        } catch (ServletException | IOException e) {
            throw new RuntimeException(e);
        } finally {
            fireRequestDestroyedEvent(httpRequest);
        }
    }

    private void fireRequestInitializedEvent(ServletRequest request) {
        ServletRequestEvent event = new ServletRequestEvent(context.getServletContext(), request);
        for (EventListener listener : context.getServletContext().getListeners()) {
            if (listener instanceof ServletRequestListener) {
                ((ServletRequestListener) listener).requestInitialized(event);
            }
        }
    }

    private void fireRequestDestroyedEvent(ServletRequest request) {
        ServletRequestEvent event = new ServletRequestEvent(context.getServletContext(), request);
        for (EventListener listener : context.getServletContext().getListeners()) {
            if (listener instanceof ServletRequestListener) {
                ((ServletRequestListener) listener).requestDestroyed(event);
            }
        }
    }

}