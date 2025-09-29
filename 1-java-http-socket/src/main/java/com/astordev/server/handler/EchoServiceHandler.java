package com.astordev.server.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class EchoServiceHandler implements ServiceHandler {

    @Override
    public void handle(Socket socket) throws IOException {
        try (InputStream in = socket.getInputStream();
             OutputStream out = socket.getOutputStream()) {
            byte[] buf = new byte[1024];
            int read;
            while ((read = in.read(buf)) != -1) {
                out.write(buf, 0, read);
                out.flush();
            }
        }
    }
}