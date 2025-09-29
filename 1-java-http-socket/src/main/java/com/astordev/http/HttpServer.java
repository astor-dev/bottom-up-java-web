package com.astordev.http;

import com.astordev.http.handler.ServiceHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {

    private final int port;
    private final Map<String, ServiceHandler> serviceHandlers;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public HttpServer(int port, Map<String, ServiceHandler> serviceHandlers) {
        this.port = port;
        this.serviceHandlers = serviceHandlers;
    }


    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);

            while (true) {
                final Socket clientSocket = serverSocket.accept();
                executorService.submit(() -> handleClient(clientSocket));
            }
        } finally {
            executorService.shutdown();
        }
    }

    private void handleClient(Socket clientSocket) {
        try (clientSocket) {
            HttpRequest request = new HttpRequest(clientSocket.getInputStream());
            HttpResponse response = new HttpResponse(clientSocket.getOutputStream());

            ServiceHandler handler = serviceHandlers.get(request.getRequestURI());

            if (handler != null) {
                handler.handle(request, response);
            } else {
                response.sendError(404, "Not Found");
            }
        } catch (Exception e) {
            System.err.println("Error handling client");
            e.printStackTrace();
        }
    }
}
