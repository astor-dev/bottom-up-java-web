package com.astordev;

import com.astordev.context.CustomServletConfig;
import com.astordev.context.CustomServletContext;
import com.astordev.context.CustomServletRegistration;
import com.astordev.http.HttpRequest;
import com.astordev.http.HttpResponse;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletRegistration;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("CallToPrintStackTrace")
public class ServletContainer {

    private final int port;
    private final CustomServletContext servletContext;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public ServletContainer(int port) {
        this.port = port;
        this.servletContext = new CustomServletContext();
        runInitializers();
        initServlets();
    }

    private void runInitializers() {
        try {
            ServiceLoader<ServletContainerInitializer> initializers = ServiceLoader.load(ServletContainerInitializer.class);
            for (ServletContainerInitializer initializer : initializers) {
                initializer.onStartup(null, servletContext); // Passing null for Set<Class<?>> as we don't support it yet
            }
        } catch (Exception e) {
            System.err.println("Error running presentation container initializers");
            e.printStackTrace();
        }
    }

    private void initServlets() {
        try {
            for (ServletRegistration registration : servletContext.getServletRegistrations().values()) {
                String servletName = registration.getName();

                if (registration instanceof CustomServletRegistration customRegistration) {
                    Class<? extends Servlet> servletClass = customRegistration.getServletClass();

                    // Instantiate the presentation using reflection
                    Servlet servlet = servletClass.getDeclaredConstructor().newInstance();

                    // Store the instantiated presentation in the context
                    servletContext.addInstantiatedServlet(servletName, servlet);

                    // Initialize the presentation
                    servlet.init(new CustomServletConfig(servletName, servletContext));
                }
            }
        } catch (Exception e) {
            System.err.println("Servlet initialization failed");
            e.printStackTrace();
        }
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
