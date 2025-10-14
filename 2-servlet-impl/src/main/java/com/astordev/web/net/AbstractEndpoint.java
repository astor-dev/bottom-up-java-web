package com.astordev.web.net;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class AbstractEndpoint implements Endpoint {
    protected int port;
    protected ExecutorService executor;
    protected Handler handler;
    protected volatile boolean stopped = false;

    public AbstractEndpoint() {
        this(8080, Executors.newFixedThreadPool(200));
    }

    public AbstractEndpoint(int port, ExecutorService executor) {
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Invalid port number: " + port);
        }
        if (executor == null) {
            throw new IllegalArgumentException("ExecutorService cannot be null");
        }
        this.port = port;
        this.executor = executor;
    }

    @Override
    public void bind(int port) {
        this.port = port;
    }

    @Override
    public void setHandler(Handler handler) {
        this.handler = handler;
    }


    @Override
    public void stop() throws IOException {
        stopped = true;
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // 강제 종료
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}