package com.astordev.web.bridge;

import com.astordev.web.net.SocketWrapperBase;
import com.astordev.web.net.nio.NioSocketWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class InputReader {
    private final SocketWrapperBase socketWrapper;
    private final boolean isNio;
    private final BufferedReader bioReader; // For BIO mode

    private final ByteBuffer nioBuffer; // For NIO mode

    public InputReader(SocketWrapperBase socketWrapper) throws IOException {
        this.socketWrapper = socketWrapper;
        this.isNio = socketWrapper instanceof NioSocketWrapper;
        if (isNio) {
            this.bioReader = null;
            this.nioBuffer = ByteBuffer.allocate(8192);
            this.nioBuffer.limit(0); // Initially empty
        } else {
            this.bioReader = new BufferedReader(new InputStreamReader(socketWrapper.getInputStream(), StandardCharsets.UTF_8));
            this.nioBuffer = null;
        }
    }

    private boolean fillBuffer() throws IOException {
        if (nioBuffer.hasRemaining()) {
            return true;
        }
        nioBuffer.clear();
        int bytesRead = socketWrapper.read(nioBuffer);
        while (bytesRead == 0) {
            try { Thread.sleep(1); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            bytesRead = socketWrapper.read(nioBuffer);
        }
        nioBuffer.flip();
        return bytesRead != -1;
    }

    public String readLine() throws IOException {
        if (!isNio) {
            return bioReader.readLine();
        }

        StringBuilder sb = new StringBuilder();
        while (fillBuffer()) {
            while (nioBuffer.hasRemaining()) {
                char c = (char) nioBuffer.get(); // Note: This is a simplification and assumes single-byte characters.
                if (c == '\n') {
                    if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\r') {
                        sb.setLength(sb.length() - 1);
                    }
                    return sb.toString();
                }
                sb.append(c);
            }
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    public int read(char[] cbuf, int off, int len) throws IOException {
        if (!isNio) {
            return bioReader.read(cbuf, off, len);
        }

        if (!fillBuffer()) {
            return -1; // End of stream
        }

        int count = Math.min(len, nioBuffer.remaining());
        for (int i = 0; i < count; i++) {
            cbuf[off + i] = (char) nioBuffer.get(); // Note: This is a simplification.
        }
        return count;
    }
}