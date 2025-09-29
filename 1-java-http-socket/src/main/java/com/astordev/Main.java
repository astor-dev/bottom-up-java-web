package com.astordev;

import com.astordev.server.Server;
import com.astordev.server.TcpServer;
import com.astordev.server.handler.EchoServiceHandler;
import com.astordev.server.handler.ServiceHandler;

/**
 * 이 클래스는 전체 애플리케이션을 초기화하고 실행하는 부트스트랩(bootstrap) 역할을 담당합니다.
 * 구체적인 {@link Server} 구현체와 비즈니스 로직을 처리하는 {@link ServiceHandler} 구현체를
 * 주입하여 서버 인스턴스를 생성하고 실행하는 책임을 가집니다.
 *
 * @see Server
 * @see ServiceHandler
 */
class Main {
    public static void main(String[] args) {
        ServiceHandler handler = new EchoServiceHandler();
        Server server = new TcpServer(9000, 4, handler);
        server.run();
    }
}