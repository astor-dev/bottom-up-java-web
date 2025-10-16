package com.astordev.web.container.connector;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;

import java.io.IOException;
import java.io.OutputStream;

public class BridgeOutputStream extends ServletOutputStream {

    private final OutputStream source;

    public BridgeOutputStream(OutputStream source) {
        this.source = source;
    }

    @Override
    public void write(int b) throws IOException {
        source.write(b);
    }

    @Override
    public void flush() throws IOException {
        source.flush();
    }

    @Override
    public void close() throws IOException {
        source.close();
    }

    @Override
    public boolean isReady() {
        // For blocking I/O, it's always ready to write.
        return true;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
        // Non-blocking I/O is not supported in this simple implementation.
        throw new UnsupportedOperationException("WriteListener is not supported for blocking I/O.");
    }
}
