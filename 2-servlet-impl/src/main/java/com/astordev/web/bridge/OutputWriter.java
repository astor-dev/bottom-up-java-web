package com.astordev.web.bridge;

import com.astordev.web.net.SocketWrapperBase;
import com.astordev.web.net.nio.NioSocketWrapper;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class OutputWriter {
    private final SocketWrapperBase socketWrapper;
    private final boolean isNio;
    private final OutputStream bioStream;

    public OutputWriter(SocketWrapperBase socketWrapper) throws IOException {
        this.socketWrapper = socketWrapper;
        this.isNio = socketWrapper instanceof NioSocketWrapper;
        if (isNio) {
            this.bioStream = null;
        } else {
            this.bioStream = socketWrapper.getOutputStream();
        }
    }

    public void write(byte[] bytes) throws IOException {
        if (isNio) {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            socketWrapper.write(buffer);
        } else {
            bioStream.write(bytes);
        }
    }

    public void flush() throws IOException {
        if (!isNio) {
            bioStream.flush();
        }
    }
}
