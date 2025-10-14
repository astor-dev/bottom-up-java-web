package com.astordev.web.bridge;

import com.astordev.web.net.Endpoint;
import com.astordev.web.net.Handler;

public abstract class ProtocolHandler implements Handler, AutoCloseable {
    protected Endpoint endpoint;

    protected Endpoint getEndpoint() {
        return this.endpoint;
    }

    @Override
    public void close() throws Exception {
        this.endpoint.stop();
    }
}
