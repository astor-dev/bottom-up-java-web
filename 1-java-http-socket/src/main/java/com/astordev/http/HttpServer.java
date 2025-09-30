package com.astordev.http;

import com.astordev.Server;
import com.astordev.SocketHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer implements Server {

    private final int port;
    private final SocketHandler serviceHandler;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public HttpServer(int port, SocketHandler serviceHandlers) {
        this.port = port;
        this.serviceHandler = serviceHandlers;
    }


    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("HTTP Server started on port " + port);

            while (true) {
                final Socket clientSocket = serverSocket.accept();
                executorService.submit(() -> handleClient(clientSocket));
            }
        } finally {
            executorService.shutdown();
        }
    }

    private void handleClient(Socket clientSocket) {
        try {
            serviceHandler.handle(clientSocket);
        } catch (Exception e) {
            System.err.println("Error handling client");
            e.printStackTrace();
        } finally {
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }
}
