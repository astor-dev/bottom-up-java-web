package com.astordev.web.net.nio;

import com.astordev.web.net.SocketWrapperBase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class NioSocketWrapper extends SocketWrapperBase {
    private final SocketChannel channel;
    private final SelectionKey key; // Selector와 연결된 키
    private final ByteBuffer readBuffer;

    public NioSocketWrapper(SocketChannel channel, SelectionKey key) {
        this.channel = channel;
        this.key = key;
        this.readBuffer = ByteBuffer.allocate(8192);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return Channels.newInputStream(channel);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return Channels.newOutputStream(channel);
    }

    @Override
    public void close() throws IOException {
        if (key != null) {
            key.cancel(); // Selector에서 등록 해제
        }
        channel.close();
    }

    @Override
    public boolean isReady() throws IOException {
        return channel.isOpen() && key != null && key.isReadable();
    }

    @Override
    public int read(ByteBuffer buffer) throws IOException {
        return channel.read(buffer);
    }
}