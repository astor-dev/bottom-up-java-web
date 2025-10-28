# 원시 소켓으로 구현한 TCP & HTTP 서버

이 프로젝트는 Java의 기본 `java.net.Socket` API만을 사용하여 처음부터 직접 TCP 서버와 HTTP 서버를 구현하는 과정을 담고 있습니다. 외부 라이브러리 없이 네트워크 통신의 가장 기본적인 원리를 이해하고, 저수준(low-level)에서 프로토콜을 어떻게 처리하는지 학습하는 것을 목표로 합니다.

프로젝트의 메인 진입점은 `com.astordev.WebApplication` 클래스입니다. 이 클래스 내부의 `Mode` 열거형 변수를 수정하여 TCP 서버 또는 HTTP 서버를 선택적으로 실행할 수 있습니다.

```java
class WebApplication {
    // 이 부분을 Mode.TCP 또는 Mode.HTTP로 변경하여 실행
    private final static Mode mode = Mode.HTTP;

    public static void main(String[] args) {
        Server server = null;

        switch (mode) {
            case HTTP -> { ... }
            case TCP -> { ... }
        }
        
        server.start();
    }
}
```

## 아키텍처 개요

프로젝트는 서버의 공통 인터페이스, TCP 서버 구현, HTTP 서버 구현 세 부분으로 나뉩니다. `Server`와 `SocketHandler`라는 공통 인터페이스를 통해 서버의 종류와 관계없이 일관된 구조로 클라이언트 연결을 처리하도록 설계되었습니다.

---

## `com.astordev` : 공통 인터페이스

최상위 패키지는 서버와 소켓 핸들러의 역할을 정의하는 핵심 추상화를 제공합니다.

-   **`Server`**: 서버 애플리케이션의 추상화를 위한 인터페이스입니다. `start()` 메서드를 통해 서버를 시작하는 기능을 정의합니다. `TcpServer`와 `HttpServer`가 이 인터페이스를 구현합니다.
-   **`SocketHandler`**: 클라이언트 `Socket` 연결 하나를 처리하는 로직을 정의하는 인터페이스입니다. `handle(Socket socket)` 메서드는 특정 클라이언트와의 전체 통신 주기를 담당합니다.

---

## `com.astordev.tcp` : TCP 서버 구현

이 패키지는 간단한 TCP 에코(echo) 서버와 클라이언트를 구현합니다. 클라이언트가 보낸 메시지를 콘솔에 출력하고, 미리 정해진 메시지를 클라이언트에게 다시 보내는 기본적인 소켓 통신을 보여줍니다.

#### 주요 클래스

-   **`TcpServer`**: `Server` 인터페이스의 구현체입니다. 지정된 포트에서 `ServerSocket`을 열고, 무한 루프를 돌며 클라이언트의 연결을 기다립니다. 새로운 연결이 수립될 때마다 해당 `Socket`을 스레드 풀(`ExecutorService`)에 전달하여 `TcpSocketHandler`가 병렬로 처리하도록 합니다.
-   **`TcpSocketHandler`**: `SocketHandler` 인터페이스의 구현체입니다. 클라이언트 소켓으로부터 데이터를 읽어 콘솔에 출력하고, "Hello, World!" 메시지를 클라이언트에게 전송하는 역할을 합니다. 클라이언트와의 연결이 끊어질 때까지 이 작업을 반복합니다.
-   **`TcpClientApplication`**: `TcpServer`를 테스트하기 위한 간단한 콘솔 기반 TCP 클라이언트입니다. 서버에 연결한 후, 사용자가 콘솔에 입력한 메시지를 서버로 보내고 서버로부터 받은 응답을 화면에 출력합니다.

---

## `com.astordev.http` : HTTP 서버 구현

이 패키지는 기본적인 HTTP/1.1 프로토콜을 해석하고 응답할 수 있는 원시적인 웹 서버를 구현합니다. 요청 라인, 헤더, 바디를 파싱하고, 특정 URL 경로에 따라 다른 응답을 보내는 기능을 포함합니다.

#### 주요 클래스

-   **`HttpServer`**: `TcpServer`와 유사하게 `Server` 인터페이스를 구현합니다. `ServerSocket`을 통해 클라이언트 연결을 수락하고, 각 연결을 스레드 풀에 위임하여 `HttpSocketHandler`가 처리하도록 합니다.
-   **`HttpSocketHandler`**: HTTP 통신을 총괄하는 핵심 클래스입니다. `Socket`을 받아 `keep-alive` 연결을 관리하며, 타임아웃을 설정합니다. 소켓의 `InputStream`을 `HttpRequest` 객체로 변환하고, `OutputStream`을 사용하여 `HttpResponse` 객체를 통해 응답을 보냅니다. 요청된 `HttpMethod`와 `URI`에 따라 등록된 `RequestProcessor`를 찾아 실제 비즈니스 로직을 위임합니다.
-   **`HttpRequest`**: 소켓의 `InputStream`을 읽어 HTTP 요청을 파싱하는 클래스입니다. 요청 라인(e.g., `GET /hello HTTP/1.1`), 헤더, 그리고 `Content-Length` 헤더가 있는 경우 요청 바디(body)를 읽어 객체 형태로 저장합니다.
-   **`HttpResponse`**: HTTP 응답을 생성하는 클래스입니다. 상태 코드(e.g., 200, 404), 헤더, 응답 바디를 설정할 수 있습니다. `flushBuffer()` 메서드가 호출되면, 설정된 정보들을 바탕으로 완전한 HTTP 응답 메시지를 포맷에 맞게 `OutputStream`에 씁니다.
-   **`RequestProcessor`**: 특정 HTTP 요청을 처리하는 비즈니스 로직(컨트롤러 역할)을 정의하는 인터페이스입니다. `process(request, response)` 메서드를 통해 실제 작업을 수행합니다. (Strategy Pattern)
-   **`HandlerKey`**: `HttpMethod`와 `URI` 경로를 조합하여 `RequestProcessor`를 찾기 위한 맵(Map)의 키로 사용되는 객체입니다.
-   **`HttpMethod`**: `GET`, `POST` 등 표준 HTTP 메서드를 정의한 열거형(Enum)입니다.

#### 하위 패키지

-   **`http.presentation`**: 실제 요청을 처리하는 `RequestProcessor` 구현체들을 포함합니다.
    -   `HelloWorldRequestProcessor`: `GET /hello` 요청을 처리하는 예제 구현체입니다. 응답 상태를 200으로 설정하고, "Hello, World!" 문자열을 응답 바디에 씁니다.

## HTTP 서버 요청 처리 흐름

1.  **[HttpServer]** 클라이언트가 서버 포트로 TCP 연결을 시도하면, `ServerSocket`이 연결을 수락하여 새로운 `Socket`을 생성합니다.
2.  **[HttpServer -> HttpSocketHandler]** `HttpServer`는 생성된 `Socket`을 스레드 풀의 스레드에게 할당하고, `HttpSocketHandler`의 `handle()` 메서드를 호출합니다.
3.  **[HttpSocketHandler -> HttpRequest]** `HttpSocketHandler`는 소켓의 `InputStream`을 `HttpRequest` 생성자에 전달하여, 원시 바이트 스트림을 파싱하고 `HttpRequest` 객체를 만듭니다.
4.  **[HttpSocketHandler]** `HttpRequest` 객체에서 `HttpMethod`와 `URI`를 가져와 `HandlerKey`를 생성합니다.
5.  **[HttpSocketHandler -> RequestProcessor]** `HandlerKey`를 사용하여 미리 등록된 `RequestProcessor` 맵에서 적절한 핸들러(e.g., `HelloWorldRequestProcessor`)를 찾습니다.
6.  **[RequestProcessor]** 찾은 핸들러의 `process()` 메서드를 호출하며, `HttpRequest`와 `HttpResponse` 객체를 인자로 전달합니다. 핸들러는 `HttpResponse` 객체에 상태 코드, 헤더, 응답 바디를 설정합니다.
7.  **[HttpSocketHandler -> HttpResponse]** 핸들러의 실행이 끝나면, `HttpSocketHandler`는 `HttpResponse`의 `flushBuffer()` 메서드를 호출합니다.
8.  **[HttpResponse]** `flushBuffer()` 메서드는 설정된 상태, 헤더, 바디 정보를 바탕으로 완전한 HTTP 응답 메시지를 만들어 소켓의 `OutputStream`으로 전송합니다.
9.  **[HttpSocketHandler]** `Connection: keep-alive` 헤더 여부에 따라 연결을 유지하거나 닫고, 스레드 처리를 종료합니다.
