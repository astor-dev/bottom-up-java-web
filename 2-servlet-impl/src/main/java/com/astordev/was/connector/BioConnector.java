package com.astordev.was.connector;

import com.astordev.was.container.context.CustomServletContext;
import com.astordev.was.container.http.HttpRequest;
import com.astordev.was.container.http.HttpResponse;
import com.astordev.was.server.Connector;
import jakarta.servlet.Servlet;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BioConnector implements Connector {

    private final int port;
    private final CustomServletContext servletContext;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public BioConnector(int port, CustomServletContext servletContext) {
        this.port = port;
        this.servletContext = servletContext;
    }

    @Override
    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("BioConnector started on port " + port);

            //noinspection InfiniteLoopStatement
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

            Servlet servlet = servletContext.getServlet(request.getRequestURI());

            if (servlet != null) {
                servlet.service(request, response);
            } else {
                response.sendError(404, "Not Found");
            }
        } catch (Exception e) {
            System.err.println("Error handling client");
            e.printStackTrace();
        }
    }
}
