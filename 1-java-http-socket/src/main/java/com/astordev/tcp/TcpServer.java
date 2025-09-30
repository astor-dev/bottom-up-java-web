package com.astordev.tcp;

import com.astordev.Server;
import com.astordev.ServiceHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpServer implements Server {
    private final int port;
    private final ServiceHandler serviceHandler;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    public TcpServer(int port, ServiceHandler serviceHandler) {
        this.port = port;
        this.serviceHandler = serviceHandler;
    }

    @Override
    public void start() throws IOException {
        try(ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("TCP Server started on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                serviceHandler.handle(clientSocket);
            }
        } finally {
            executorService.shutdown();
        }
    }
}
