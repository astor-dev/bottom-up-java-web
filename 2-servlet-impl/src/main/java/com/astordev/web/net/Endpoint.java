package com.astordev.web.net;

import java.io.IOException;

public interface Endpoint {
    enum Type {
        BIO, NIO
    }

    void start() throws IOException;

    void stop() throws IOException;

    void bind(int port);

    void setHandler(Handler protocolHandler);
}