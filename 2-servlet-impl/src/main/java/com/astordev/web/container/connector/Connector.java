package com.astordev.web.container.connector;

import com.astordev.web.bridge.*;
import com.astordev.web.bridge.http11.Http11Protocol;
import com.astordev.web.container.ServletMapper;
import com.astordev.web.container.http.HttpRequest;
import com.astordev.web.container.http.HttpResponse;
import com.astordev.web.net.Endpoint;
import com.astordev.web.server.ServletContainer;

public class Connector implements AutoCloseable{
    private final Gateway gateway;
    private final ProtocolHandler protocolHandler;
    public final int port;
    public final String protocol;
    public final String scheme;

    public Connector(Protocol protocol, ServletContainer servletContainer, ServletMapper servletMapper, int port, Endpoint.Type type) {
        this.gateway = new BridgeGateway(servletMapper, this);
        this.port = port;
        switch (protocol) {
            case HTTP11 -> {
                this.protocolHandler = new Http11Protocol(gateway, type, port);
                this.protocol = "HTTP/1.1";
                this.scheme = "http";
            }
            default -> throw new UnsupportedOperationException();
        }
    }

    public HttpRequest createRequest(Request request) {
        return new HttpRequest(request, this);
    }

    public HttpResponse createResponse(Response response) {
        return new HttpResponse(response);
    }

    public Gateway getGateway() {
        return gateway;
    }

    @Override
    public void close() throws Exception {
        this.protocolHandler.close();
    }
}
