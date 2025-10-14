package com.astordev.web.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public abstract class SocketWrapperBase implements AutoCloseable {
    public abstract InputStream getInputStream() throws IOException;
    public abstract OutputStream getOutputStream() throws IOException;
    public abstract void close() throws IOException;
    public boolean isReady() throws IOException {
        return true;
    }
    public int read(ByteBuffer buffer) throws IOException {
        throw new UnsupportedOperationException("Only for NIO");
    }
}