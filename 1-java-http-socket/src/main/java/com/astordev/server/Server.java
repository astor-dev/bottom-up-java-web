package com.astordev.server;

import java.io.Closeable;
import java.io.IOException;

public interface Server extends Runnable, Closeable {
    @Override
    void run();

    @Override
    void close() throws IOException;
}