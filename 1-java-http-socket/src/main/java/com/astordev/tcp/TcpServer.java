package com.astordev.tcp;

import com.astordev.Server;
import com.astordev.ServiceHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpServer implements Server {
    private final int port;
    private final ServiceHandler serviceHandler;
    private final List<Socket> clientSockets = Collections.synchronizedList(new ArrayList<>());
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private ServerSocket serverSocket;


    public TcpServer(int port, ServiceHandler serviceHandler) {
        this.port = port;
        this.serviceHandler = serviceHandler;
        addShutdownHook();
    }

    @Override
    public void start() throws IOException {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("TCP Server started on port " + port);
            while (!Thread.currentThread().isInterrupted()) {
                Socket clientSocket = serverSocket.accept();
                clientSockets.add(clientSocket);
                executorService.submit(() -> handleClient(clientSocket));
            }
        } finally {
            executorService.shutdown();
        }
    }

    private void handleClient(Socket clientSocket) {
        try (clientSocket) {
            serviceHandler.handle(clientSocket);
        } catch (Exception e) {
            System.err.println("Error handling client");
            e.printStackTrace();
        }
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("JVM 종료 감지. 모든 소켓을 닫습니다...");
            try {
                for (Socket clientSocket : clientSockets) {
                    if (!clientSocket.isClosed()) {
                        clientSocket.close();
                    }
                }
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                // 종료 중 발생한 오류는 무시
            }
            System.out.println("모든 소켓이 정상적으로 닫혔습니다.");
        }));
    }
}
