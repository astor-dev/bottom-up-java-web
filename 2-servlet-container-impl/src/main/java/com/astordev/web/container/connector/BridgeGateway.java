package com.astordev.web.container.connector;

import com.astordev.web.bridge.Gateway;
import com.astordev.web.bridge.Request;
import com.astordev.web.bridge.Response;
import com.astordev.web.container.ServletMapper;
import com.astordev.web.container.http.HttpRequest;
import com.astordev.web.container.http.HttpResponse;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class BridgeGateway implements Gateway {
    private final ServletMapper mapper;
    private final Connector connector;


    public BridgeGateway(ServletMapper mapper, Connector connector) {
        this.mapper = mapper;
        this.connector = connector;
    }

    @Override
    public void service(Request request, Response response)  {
        String requestURI = request.getRequestURI();
        Servlet servlet = mapper.map(requestURI);
        HttpRequest httpRequest = connector.createRequest(request);
        HttpResponse httpResponse = connector.createResponse(response);
        try {
            if (servlet != null) {
                servlet.service(httpRequest, httpResponse);
                httpResponse.flushBuffer();
            } else {
                httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "Not Found");
            }
        } catch (ServletException | IOException e) {
            throw new RuntimeException(e);
        }
    }

}