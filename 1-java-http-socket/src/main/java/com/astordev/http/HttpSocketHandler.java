package com.astordev.http;

import com.astordev.SocketHandler;
import com.astordev.http.presentation.HelloWorldRequestProcessor;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpSocketHandler implements SocketHandler {

    private final Map<HandlerKey, RequestProcessor> requestProcessorRegistry = new ConcurrentHashMap<>();
    private static final int TIMEOUT_MS = 10 * 1000;
    public HttpSocketHandler() {
        requestProcessorRegistry.put(new HandlerKey(HttpMethod.GET, "/hello"), new HelloWorldRequestProcessor());
    }

    @Override
    public void handle(Socket socket) throws IOException {
        try {
            socket.setSoTimeout(TIMEOUT_MS);
            System.out.println("TCP Connection created with "
                    + socket.getInetAddress().getHostAddress() + "[" + socket.getPort() + "]");
            while (!socket.isClosed()) {
                HttpRequest request;
                HttpResponse response = new HttpResponse(socket.getOutputStream());
                try {
                    request = new HttpRequest(socket.getInputStream());
                } catch (SocketTimeoutException e) {
                    System.out.println("Connection timed out. Closing socket.");
                    break;
                } catch (IOException e) {
                    response.sendError(400, "Malformed request");
                    System.out.println(e.getMessage());
                    break;
                }

                boolean keepAlive = false;
                try {
                    String connectionHeader = request.getHeader("Connection");
                    keepAlive = !"close".equalsIgnoreCase(connectionHeader);

                    HttpMethod method = HttpMethod.fromString(request.getMethod());
                    if (method == null) {
                        response.sendError(400, "Unsupported HTTP Method");
                    } else {
                        HandlerKey requestKey = new HandlerKey(method, request.getRequestURI());
                        RequestProcessor processor = requestProcessorRegistry.get(requestKey);
                        if (processor != null) {
                            processor.process(request, response);
                        } else {
                            response.sendError(404, "Not Found");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error processing request: " + e.getMessage());
                    e.printStackTrace();
                    response.sendError(500, "Internal Server Error");
                }

                if (keepAlive) {
                    response.addHeader("Connection", "keep-alive");
                    socket.setSoTimeout(TIMEOUT_MS);
                } else {
                    response.addHeader("Connection", "close");
                }

                response.flushBuffer();

                if (!keepAlive) {
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error in HttpSocketHandler: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (!socket.isClosed()) {
                socket.close();
            }
        }
    }
}