package com.astordev.web.container.connector;

import com.astordev.web.bridge.*;
import com.astordev.web.bridge.http11.Http11Protocol;
import com.astordev.web.container.context.Context;
import com.astordev.web.container.http.HttpRequest;
import com.astordev.web.container.http.HttpResponse;
import com.astordev.web.net.Endpoint;

public class Connector implements AutoCloseable{
    private final Gateway gateway;
    private final ProtocolHandler protocolHandler;
    public final int port;
    public final String protocol;
    public final String scheme;

    public Connector(Protocol protocol, Context context, int port, Endpoint.Type type) {
        this.gateway = new BridgeGateway(context, this);
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

    public HttpRequest createRequest(Request request, Context context) {
        return new HttpRequest(request, this, context);
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
