package com.astordev.web.net.nio;

import com.astordev.web.net.SocketWrapperBase;
import com.astordev.web.net.AbstractEndpoint;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NioEndpoint extends AbstractEndpoint {
    private ServerSocketChannel serverChannel;
    private Selector selector;

    @Override
    public void start() throws IOException {
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(port));
        selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        executor.submit(() -> {
            System.out.println("started Nio Server Socket Channel at port: " + port);
            while (!stopped) {
                try {
                    selector.select(1000);
                    Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                    while (keys.hasNext()) {
                        SelectionKey key = keys.next();
                        keys.remove();
                        if (key.isAcceptable()) {
                            SocketChannel channel = serverChannel.accept();
                            if (channel != null) {
                                channel.configureBlocking(false);
                                SelectionKey readKey = channel.register(selector, SelectionKey.OP_READ);
                                // SelectionKey를 NioSocketWrapper에 전달
                                SocketWrapperBase wrapper = new NioSocketWrapper(channel, readKey);
                                executor.submit(() -> handler.process(wrapper));
                            }
                        } else if (key.isReadable()) {
                            // Prevent the selector from firing again for this key while it's being processed.
                            key.interestOps(0);
                            SocketChannel channel = (SocketChannel) key.channel();
                            SocketWrapperBase wrapper = new NioSocketWrapper(channel, key);
                            executor.submit(() -> {
                                boolean keepAlive = handler.process(wrapper);
                                if (keepAlive) {
                                    key.interestOps(SelectionKey.OP_READ);
                                    selector.wakeup();
                                }
                            });
                        }
                    }
                } catch (IOException e) {
                    if (!stopped) {
                        System.err.println("Error in selector loop: " + e.getMessage());
                    }
                }
            }
        });
    }

    @Override
    public void stop() throws IOException {
        super.stop();
        if (selector != null && selector.isOpen()) {
            for (SelectionKey key : selector.keys()) {
                if (key.channel() instanceof SocketChannel) {
                    (key.channel()).close();
                }
                key.cancel();
            }
            selector.close();
        }
        if (serverChannel != null && serverChannel.isOpen()) {
            serverChannel.close();
        }
    }
}