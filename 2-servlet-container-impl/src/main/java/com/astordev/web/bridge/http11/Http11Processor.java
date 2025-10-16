package com.astordev.web.bridge.http11;

import com.astordev.web.bridge.Gateway;
import com.astordev.web.bridge.InputReader;
import com.astordev.web.bridge.OutputWriter;
import com.astordev.web.bridge.Request;
import com.astordev.web.bridge.Response;
import com.astordev.web.net.SocketWrapperBase;

import java.io.BufferedReader;
import java.io.IOException;

public class Http11Processor {

    private final SocketWrapperBase socketWrapper;
    private final Gateway gateway;

        public Http11Processor(SocketWrapperBase socketWrapper, Gateway gateway) {

            this.socketWrapper = socketWrapper;

            this.gateway = gateway;

        }

    

        public boolean process() {

            Request request = new Request();

            Response response = new Response();

    

            try {

                parseRequest(request);

                System.out.println("[" + request.getMethod() + " " + request.getRequestURI() + "]");

                gateway.service(request, response);

                sendResponse(response, request.isKeepAlive());

            } catch (Exception e) {

                response.setStatus(500);

                response.setBody("Internal Server Error");

                try {

                    sendResponse(response, false); // 에러 발생 시 커넥션은 닫음

                } catch (IOException ignored) {}

                e.printStackTrace();

                request.setKeepAlive(false); // 에러 발생 시 커넥션은 닫음

            } finally {

                if (!request.isKeepAlive()) {

                    try {

                        socketWrapper.close();

                    } catch (IOException e) {

                        // ignore

                    }

                }

            }

            return request.isKeepAlive();

        }

    

        private void parseRequest(Request request) throws IOException {

            InputReader reader = new InputReader(socketWrapper);

    

            String requestLine = reader.readLine();

            if (requestLine == null || requestLine.isEmpty()) {

                throw new IOException("Invalid request line");

            }

            String[] parts = requestLine.split("\\s+", 3);

            if (parts.length != 3) {

                throw new IOException("Malformed Request-Line: " + requestLine);

            }

            request.setMethod(parts[0]);

            request.setRequestURI(parts[1]);

    

            String headerLine;

            while ((headerLine = reader.readLine()) != null && !headerLine.isEmpty()) {

                final int separatorIndex = headerLine.indexOf(":");

                if (separatorIndex < 1) {

                    continue;

                }

                String key = headerLine.substring(0, separatorIndex).trim();

                String value = headerLine.substring(separatorIndex + 1).trim();

                if (!key.isEmpty()) {

                    request.addHeader(key, value);

                }

    

                if ("connection".equalsIgnoreCase(key) && "close".equalsIgnoreCase(value)) {

                    request.setKeepAlive(false);

                }

            }

    

            int contentLength = 0;

            String contentLengthHeader = request.getHeader("Content-Length");

            if (contentLengthHeader != null) {

                contentLength = Integer.parseInt(contentLengthHeader);

            }

    

            if (contentLength > 0) {

                char[] bodyChars = new char[contentLength];

                int totalRead = 0;

                int bytesRead = 0;

                while (totalRead < contentLength && bytesRead != -1) {

                    bytesRead = reader.read(bodyChars, totalRead, contentLength - totalRead);

                    if (bytesRead != -1) {

                        totalRead += bytesRead;

                    }

                }

    

                if (totalRead < contentLength) {

                    throw new IOException("Unexpected end of stream while reading request body");

                }

                request.setBody(new String(bodyChars));

            }

        }

    

        private void sendResponse(Response response, boolean keepAlive) throws IOException {

            OutputWriter outputWriter = new OutputWriter(socketWrapper);

            StringBuilder sb = new StringBuilder();

            sb.append("HTTP/1.1 ").append(response.getStatus()).append(" \r\n");

            response.getHeaders().forEach((key, values) -> {

                for (String value : values) {

                    sb.append(key).append(": ").append(value).append("\r\n");

                }

            });

            sb.append("Connection: ").append(keepAlive ? "keep-alive" : "close").append("\r\n");

            sb.append("Content-Length: ").append(response.getBody().getBytes().length).append("\r\n");

            sb.append("\r\n");

            sb.append(response.getBody());

    

            outputWriter.write(sb.toString().getBytes());

            outputWriter.flush();

        }

    }

    