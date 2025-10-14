package com.astordev.web.net;

public interface Handler {
    boolean process(SocketWrapperBase socketWrapper);
}