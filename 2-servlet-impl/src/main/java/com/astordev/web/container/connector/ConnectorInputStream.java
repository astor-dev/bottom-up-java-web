package com.astordev.web.container.connector;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

import java.io.IOException;
import java.io.InputStream;

public class ConnectorInputStream extends ServletInputStream {

    private final InputStream source;

    public ConnectorInputStream(InputStream source) {
        this.source = source;
    }

    @Override
    public int read() throws IOException {
        return source.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return source.read(b, off, len);
    }

    @Override
    public boolean isFinished() {
        try {
            return source.available() == 0;
        } catch (IOException e) {
            return true;
        }
    }

    @Override
    public boolean isReady() {
        // For blocking I/O, it's always ready to attempt a read.
        return true;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        // Non-blocking I/O is not supported in this simple implementation.
        throw new UnsupportedOperationException("ReadListener is not supported for blocking I/O.");
    }

    @Override
    public void close() throws IOException {
        source.close();
    }
}