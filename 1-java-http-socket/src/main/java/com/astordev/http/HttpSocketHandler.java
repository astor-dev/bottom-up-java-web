package com.astordev.http;

import com.astordev.SocketHandler;
import com.astordev.http.presentation.HelloWorldRequestProcessor;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpSocketHandler implements SocketHandler {

    private final Map<HandlerKey, RequestProcessor> requestProcessorRegistry = new ConcurrentHashMap<>();

    public HttpSocketHandler() {
        requestProcessorRegistry.put(new HandlerKey(HttpMethod.GET, "/hello"), new HelloWorldRequestProcessor());
    }

    @Override
    public void handle(Socket socket) throws IOException {
        try {
            while (true) {
                HttpRequest request;
                try {
                    request = new HttpRequest(socket.getInputStream());
                } catch (IOException e) {
                    break;
                }

                HttpResponse response = new HttpResponse(socket.getOutputStream());

                HttpMethod method = HttpMethod.fromString(request.getMethod());
                if (method == null) {
                    response.setStatus(400);
                    response.addHeader("Content-Type", "text/plain; charset=utf-8");
                    response.getWriter().println("Bad Request: Unsupported HTTP Method");
                    socket.getOutputStream().flush();
                    break;
                }
                HandlerKey requestKey = new HandlerKey(method, request.getRequestURI());
                RequestProcessor servlet = requestProcessorRegistry.get(requestKey);

                if (servlet != null) {
                    servlet.process(request, response);
                } else {
                    response.setStatus(404);
                    response.addHeader("Content-Type", "text/plain; charset=utf-8");
                    response.getWriter().println("Not Found");
                }

                String connectionHeader = request.getHeader("Connection");
                boolean keepAlive = !"close".equalsIgnoreCase(connectionHeader);

                if (keepAlive) {
                    response.addHeader("Connection", "Keep-Alive");
                    socket.getOutputStream().flush();
                } else {
                    response.addHeader("Connection", "close");
                    socket.getOutputStream().flush();
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error in HttpSocketHandler: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
