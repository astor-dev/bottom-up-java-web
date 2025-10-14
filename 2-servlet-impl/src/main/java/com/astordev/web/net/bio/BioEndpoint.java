package com.astordev.web.net.bio;

import com.astordev.web.net.SocketWrapperBase;
import com.astordev.web.net.AbstractEndpoint;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class BioEndpoint extends AbstractEndpoint {
    private ServerSocket serverSocket;

    @Override
    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        executor.submit(() -> {
            System.out.println("started Bio Server Socket at port: " + port);
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    SocketWrapperBase wrapper = new BioSocketWrapper(socket);  // 래퍼 생성
                    executor.submit(() -> handler.process(wrapper));  // 스레드 풀로 처리
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void stop() throws IOException {
        super.stop();
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }
}