package com.astordev.tcp;

import com.astordev.ServiceHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class HelloWorldTcpServiceHandler implements ServiceHandler {

    @Override
    public void handle(Socket socket) throws IOException {
        try {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            out.write("Hello, World!".getBytes());
            byte[] buffer = new byte[1024 * 128];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                int maxLength = 50;
                String content = new String(buffer, 0, bytesRead);
                if (content.length() > maxLength) {
                    content = content.substring(0, maxLength) + "...";
                }
                System.out.println("[" + socket.getInetAddress().getHostAddress() +
                        "(" + socket.getPort() + ")] " +
                        content + " (" + bytesRead + " bytes)");
                out.write("Hello, World!".getBytes());
            }
            System.out.println("Connection Closed");
            socket.close();
        } catch (IOException e) {
            System.err.println("Communication error: " + e.getMessage());
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }
}
