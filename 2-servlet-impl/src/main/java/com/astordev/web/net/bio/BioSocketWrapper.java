package com.astordev.web.net.bio;

import com.astordev.web.net.SocketWrapperBase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class BioSocketWrapper extends SocketWrapperBase {
    private final Socket socket;

    public BioSocketWrapper(Socket socket) {
        this.socket = socket;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}