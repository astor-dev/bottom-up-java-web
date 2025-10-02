package com.astordev.was.server;

import java.io.IOException;

public interface Connector {

    enum Type {
        BIO, NIO
    }

    void start() throws IOException;
}
