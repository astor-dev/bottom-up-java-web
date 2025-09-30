package com.astordev;

import java.io.IOException;
import java.net.Socket;


public interface ServiceHandler {
    void handle(Socket socket) throws IOException;
}
