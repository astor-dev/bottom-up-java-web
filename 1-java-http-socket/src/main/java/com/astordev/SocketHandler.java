package com.astordev;

import java.io.IOException;
import java.net.Socket;


public interface SocketHandler {
    void handle(Socket socket) throws IOException;
}
