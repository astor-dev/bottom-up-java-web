package com.astordev.tcp;

import java.io.*;
import java.net.Socket;

public class TcpClientApplication {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 8080)) {
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
                } catch (IOException e) {
                    System.err.println("Server disconnected: " + e.getMessage());
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // Ignore
                    }
                }
            });
            serverReaderThread.start();

            String line;
            // NOTE: 숫자를 입력하면 해당 숫자에 해당하는 KB 사이즈 데이터를 전송, 그 외에는 그대로 씀
            while ((line = commandLineReader.readLine()) != null) {
                try {
                    int kb = Integer.parseInt(line.trim());
                    byte[] buffer = new byte[kb * 1024]; // KB 단위 버퍼 생성
                    out.write(buffer);
                } catch (NumberFormatException e) {
                    out.write(line.getBytes());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
