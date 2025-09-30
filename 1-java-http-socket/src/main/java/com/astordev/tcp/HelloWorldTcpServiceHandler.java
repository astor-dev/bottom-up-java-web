package com.astordev.tcp;

import com.astordev.ServiceHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class HelloWorldTcpServiceHandler implements ServiceHandler {

    @Override
    public void handle(Socket socket) throws IOException {
        try (InputStream in = socket.getInputStream()) {
            OutputStream out = socket.getOutputStream();

            out.write("Hello, World!".getBytes());
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                System.out.println("Received: " + new String(buffer, 0, bytesRead));
                out.write("Hello, World!".getBytes());
            }
            System.out.println("Connection Closed");
            socket.close();
        } catch (IOException e) {
            // 연결 끊김 등 예외 처리
            System.err.println("Communication error: " + e.getMessage());
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }
}
