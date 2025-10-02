package com.astordev.tcp;

import java.io.*;
import java.net.Socket;

public class TcpClientApplication {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 8080)) {
            System.out.println("TCP Connection created");
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            BufferedReader commandLineReader = new BufferedReader(new InputStreamReader(System.in));


            Thread serverReaderThread = new Thread(() -> {
                try {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        System.out.println("[Server] " + new String(buffer, 0, bytesRead));
                    }
                    System.out.println("\nServer has closed the connection.");
                } catch (IOException e) {
                    System.err.println("Server disconnected: " + e.getMessage());
                } finally {
                    try {
                        System.in.close();
                        if (!socket.isClosed()) {
                            socket.close();
                        }
                    } catch (IOException e) {
                        // Ignore
                    }
                }
            });

            Thread commandLineWriterThead = new Thread(() -> {
                try (BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
                     PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

                    String line;
                    while ((line = consoleReader.readLine()) != null) {
                        writer.println(line);
                        if (writer.checkError()) {
                            System.err.println("Could not send data. Socket might be closed.");
                            break;
                        }
                    }
                    System.out.println("Input stream closed. Writer thread is stopping.");
                } catch (IOException e) {
                    System.err.println("Writer thread error: " + e.getMessage());
                } finally {
                    try {
                        if (!socket.isClosed()) socket.close();
                    } catch (IOException e) { /* 무시 */ }
                }
            });
            serverReaderThread.start();
            commandLineWriterThead.start();

            serverReaderThread.join();
            commandLineWriterThead.join();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
