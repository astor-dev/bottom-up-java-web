package com.astordev.server;

import com.astordev.server.handler.ServiceHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer implements Server {
    private final int port;
    private final WorkerPool workerPool;
    private final ServiceHandler handler;
    private volatile boolean running = true;

    public TcpServer(int port, int workerThreads, ServiceHandler handler) {
        this.port = port;
        this.workerPool = new WorkerPool(workerThreads);
        this.handler = handler;
    }

    public void run() {
        Thread acceptor = new Thread(this::acceptLoop, "acceptor-thread");
        acceptor.start();
        System.out.println("TCP com.astordev.server.Server listening on port " + port);
    }

    private void acceptLoop() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (running) {
                Socket client = serverSocket.accept(); // 블로킹 accept
                workerPool.submit(() -> {
                    try {
                        handler.handle(client);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try { client.close(); } catch (IOException ignored) {}
                    }
                });
            }
        } catch (IOException e) {
            if (running) e.printStackTrace();
        }
    }

    public void close() {
        running = false;
        workerPool.shutdown();
        System.out.println("TCP com.astordev.server.Server stopped");
    }
}