package com.astordev.web.bridge.http11;

import com.astordev.web.bridge.Gateway;
import com.astordev.web.bridge.ProtocolHandler;
import com.astordev.web.net.Endpoint;
import com.astordev.web.net.SocketWrapperBase;
import com.astordev.web.net.bio.BioEndpoint;
import com.astordev.web.net.nio.NioEndpoint;

import java.io.IOException;

public class Http11Protocol extends ProtocolHandler {

    private final Gateway gateway;
    private final int port;

    public Http11Protocol(Gateway gateway, Endpoint.Type type, int port) {
        this.gateway = gateway;
        switch (type) {
            case NIO -> this.endpoint = new NioEndpoint();
            case BIO -> this.endpoint = new BioEndpoint();
        }
        this.port = port;
        init();
    }

    @Override
    public void process(SocketWrapperBase socketWrapper) {
        Http11Processor processor = new Http11Processor(socketWrapper, gateway);
        processor.process();
    }

    private void init() {
        this.endpoint.setHandler(this);
        this.endpoint.bind(port);
        try {
            this.endpoint.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
