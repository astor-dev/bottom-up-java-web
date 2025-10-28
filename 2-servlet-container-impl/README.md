# 직접 구현해보는 서블릿 컨테이너

이 프로젝트는 Tomcat, Jetty와 같은 Java 서블릿 컨테이너의 핵심 구조를 학습하기 위해 직접 구현한 간소화된 버전입니다. HTTP 요청 처리, 서블릿 생명주기 관리, 그리고 확장성 있는 서버를 만드는 계층형 아키텍처의 핵심 개념을 다룹니다.

서버를 시작하는 기본 진입점은 `com.astordev.WebApplication` 클래스입니다. 이 클래스의 `main` 메서드에서 서블릿을 등록하고, 커넥터를 설정하여 웹 애플리케이션을 실행할 수 있습니다.

```java
public class WebApplication {
    public static void main(String[] args) {
        try (ServletContainer servletContainer = new ServletContainer()) {
            // 서블릿 등록
            servletContainer.addServlet("HelloWorldServlet", HelloWorldServlet.class, "/hello");

            // 커넥터 설정 (예: 8080 포트 BIO, 8081 포트 NIO)
            servletContainer.addConnector(8080, Endpoint.Type.BIO, Protocol.HTTP11);
            servletContainer.addConnector(8081, Endpoint.Type.NIO, Protocol.HTTP11);

            // 컨테이너 시작
            servletContainer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

## 아키텍처 개요

컨테이너의 핵심 로직은 `com.astordev.web` 패키지에 있으며, 세 개의 명확한 계층으로 나뉩니다. 각 계층은 **`container` -> `bridge` -> `net`** 순서의 의존성을 가집니다. 이러한 구조는 저수준(low-level) 네트워크 I/O부터 고수준(high-level) 서블릿 관리에 이르기까지 각 계층이 자신의 역할에만 집중하도록 만듭니다.

---

## `com.astordev.web.net` : 네트워크 계층 (Network Layer)

가장 낮은 수준의 계층으로, 원시(raw) 네트워크 통신을 담당합니다. 이 계층의 핵심 역할은 BIO(Blocking I/O)와 NIO(Non-Blocking I/O)의 구현 차이를 추상화하여 상위 계층에 일관된 인터페이스를 제공하는 것입니다.

#### 주요 클래스 및 인터페이스

-   **`Endpoint`**: 네트워크 연결을 수신하는 엔드포인트에 대한 인터페이스입니다. 구현체는 특정 I/O 모델(BIO/NIO)을 사용하여 서버 소켓을 열고 클라이언트 연결을 수락합니다.
-   **`AbstractEndpoint`**: `Endpoint` 인터페이스의 공통 로직(스레드 풀 관리, 포트 바인딩 등)을 구현한 추상 클래스입니다.
-   **`Handler`**: `Endpoint`가 수락한 연결을 처리할 핸들러에 대한 인터페이스입니다. `bridge` 계층의 `ProtocolHandler`가 이 인터페이스를 구현하여 실제 프로토콜 처리 로직을 수행합니다.
-   **`SocketWrapperBase`**: `java.net.Socket`(BIO)과 `java.nio.channels.SocketChannel`(NIO)을 감싸는 추상 클래스입니다. I/O 모델에 관계없이 상위 계층에서 소켓을 동일한 방식으로 다룰 수 있도록 입출력 작업을 추상화합니다.

#### 하위 패키지

-   **`net.bio`**: 블로킹 I/O(BIO) 관련 구현을 포함합니다.
    -   `BioEndpoint`: `java.net.ServerSocket`을 사용하여 동기 방식으로 클라이언트의 연결을 수락합니다. 각 연결은 스레드 풀의 별도 스레드에서 처리됩니다.
    -   `BioSocketWrapper`: `java.net.Socket`을 감싸 `SocketWrapperBase` 인터페이스를 구현합니다.
-   **`net.nio`**: 논블로킹 I/O(NIO) 관련 구현을 포함합니다.
    -   `NioEndpoint`: `java.nio.channels.Selector`와 `ServerSocketChannel`을 사용하여 비동기 방식으로 이벤트를 감지합니다. 단일 스레드(이벤트 루프)에서 여러 연결을 효율적으로 관리할 수 있습니다.
    -   `NioSocketWrapper`: `java.nio.channels.SocketChannel`을 감싸 `SocketWrapperBase` 인터페이스를 구현합니다.

---

## `com.astordev.web.bridge` : 프로토콜 계층 (Protocol Layer)

이름처럼 `net` 계층과 `container` 계층을 연결하는 다리 역할을 합니다. `net` 계층으로부터 받은 원시 바이트 스트림을 해석하여 의미 있는 애플리케이션 프로토콜(여기서는 HTTP/1.1) 단위로 변환하고, 그 결과를 `container` 계층으로 전달합니다.

#### 주요 클래스 및 인터페이스

-   **`ProtocolHandler`**: `net.Handler`를 구현하는 추상 클래스로, 특정 프로토콜 처리의 생명주기를 관리합니다. `Endpoint`와 연결되어 프로토콜 처리 로직을 총괄합니다.
-   **`Gateway`**: 프로토콜 처리기(`Http11Processor`)와 서블릿 컨테이너(`container` 계층)를 분리하는 역할을 하는 인터페이스입니다. `container` 계층의 `BridgeGateway`가 이를 구현하여 요청을 서블릿 컨테이너 내부로 전달합니다.
-   **`Request` / `Response`**: HTTP 요청과 응답의 핵심 정보(메서드, URI, 헤더, 바디 등)만을 담는 간단한 데이터 객체(POJO)입니다. 이는 서블릿 API의 `HttpServletRequest`, `HttpServletResponse`와는 다른, 프로토콜 계층 내부에서만 사용되는 객체입니다.
-   **`InputReader` / `OutputWriter`**: `SocketWrapperBase`를 사용하여 BIO와 NIO 스트림의 차이를 다시 한번 추상화합니다. 이를 통해 `Http11Processor`는 I/O 모델에 관계없이 라인 단위 읽기와 같은 작업을 동일한 코드로 수행할 수 있습니다.
-   **`Protocol`**: 지원하는 프로토콜의 종류(`HTTP11`, `HTTP2` 등)를 정의한 열거형(Enum)입니다.

#### 하위 패키지

-   **`bridge.http11`**: HTTP/1.1 프로토콜 관련 구현을 포함합니다.
    -   `Http11Protocol`: `ProtocolHandler`를 상속받아 HTTP/1.1 처리 로직을 시작합니다. `Endpoint`로부터 `SocketWrapperBase`를 전달받아 `Http11Processor`를 생성하고 실행합니다.
    -   `Http11Processor`: HTTP/1.1 명세에 따라 소켓 스트림을 파싱하여 `bridge.Request` 객체를 생성하고, `Gateway`를 통해 컨테이너 처리가 완료된 `bridge.Response` 객체를 다시 HTTP 응답 포맷에 맞춰 직렬화한 후 소켓에 씁니다.

---

## `com.astordev.web.container` : 서블릿 컨테이너 계층 (Servlet Container Layer)

가장 상위 계층으로, Jakarta Servlet 명세를 구현합니다. 서블릿의 생명주기 관리, 요청-서블릿 매핑, 필터 체인 적용, 세션 관리 등 웹 애플리케이션의 핵심 로직을 담당합니다.

#### 하위 패키지 및 주요 클래스

-   **`container.connector`**: `bridge` 계층과 `container` 계층을 최종적으로 연결합니다.
    -   `Connector`: 모든 계층을 아우르는 핵심 조립 클래스입니다. 특정 포트, 프로토콜(`Http11Protocol`), I/O 모델(`Endpoint`) 설정을 바탕으로 서버의 한쪽 끝점을 구성합니다. `ServletContainer`는 여러 `Connector`를 가질 수 있어, 여러 포트에서 동시에 요청을 수신할 수 있습니다.
    -   `BridgeGateway`: `bridge.Gateway` 인터페이스의 구현체로, 컨테이너의 실질적인 진입점입니다. `bridge` 계층에서 파싱된 `bridge.Request`와 `bridge.Response`를 받아, 이를 서블릿 API 표준인 `HttpServletRequest`와 `HttpServletResponse`로 변환한 후, 적절한 서블릿과 필터 체인을 찾아 호출합니다.
    -   `BridgeInputStream` / `BridgeOutputStream`: `ServletInputStream`과 `ServletOutputStream`의 구현체로, 서블릿 API 스펙을 준수하는 스트림을 제공합니다.

-   **`container.context`**: 하나의 웹 애플리케이션 컨텍스트를 구성하는 요소들을 포함합니다.
    -   `Context`: 하나의 웹 애플리케이션 그 자체를 표현하는 객체입니다. 내부에 서블릿, 필터, 리스너의 등록 정보를 모두 보관하며, 컨테이너가 시작되고 종료될 때 이들의 생명주기(`init`, `destroy`)를 관리합니다.
    -   `CustomServletContext`: `jakarta.servlet.ServletContext` 인터페이스의 구현체입니다.
    -   `CustomServletConfig` / `CustomServletRegistration`: 서블릿 설정 및 등록 정보를 관리하는 클래스입니다.

-   **`container.filter`**: 서블릿 필터 관련 기능을 포함합니다.
    -   `FilterMapper`: 요청 URI를 분석하여 해당 요청에 적용되어야 할 필터 목록을 찾아냅니다.
    -   `ApplicationFilterChain`: `FilterMapper`가 찾아낸 필터들을 순서대로 실행(`doFilter`)하고, 체인의 마지막에는 실제 목적지인 서블릿을 호출하는 역할을 합니다.
    -   `CustomFilterConfig` / `CustomFilterRegistration`: 필터 설정 및 등록 정보를 관리하는 클래스입니다.

-   **`container.http`**: 서블릿 HTTP API의 구현체를 포함합니다.
    -   `HttpRequest`: `jakarta.servlet.http.HttpServletRequest` 인터페이스의 구현체입니다. `bridge.Request`를 감싸 파라미터 파싱, 쿠키 처리, 세션 관리 등 서블릿 API의 모든 기능을 제공합니다.
    -   `HttpResponse`: `jakarta.servlet.http.HttpServletResponse` 인터페이스의 구현체입니다. `bridge.Response`를 감싸 헤더 설정, 리다이렉트, 버퍼링 등 복잡한 응답 관련 기능을 제공합니다.

-   **`container.session`**: HTTP 세션 관리 기능을 포함합니다.
    -   `SessionManager`: `HttpSession`의 생명주기(생성, 조회, 소멸)를 총괄합니다. 세션 ID를 생성하고 쿠키를 통해 클라이언트에 전달하는 역할도 수행합니다.
    -   `CustomHttpSession`: `jakarta.servlet.http.HttpSession` 인터페이스의 구현체로, 세션 속성(attribute) 저장, 타임아웃 관리 등의 기능을 제공합니다.

## 전체 요청 처리 흐름

1.  **[net]** `Endpoint`(`BioEndpoint` 또는 `NioEndpoint`)가 클라이언트의 TCP 연결을 수락합니다.
2.  **[net -> bridge]** `Endpoint`는 연결된 소켓을 `SocketWrapperBase`로 감싸 `Http11Protocol`(`Handler`)에게 처리를 위임합니다.
3.  **[bridge]** `Http11Protocol`은 `Http11Processor`를 사용하여 소켓의 입력 스트림을 파싱하고, `bridge.Request` 객체를 생성합니다.
4.  **[bridge -> container]** `Http11Processor`는 `BridgeGateway`의 `service` 메서드를 호출하며, 파싱된 `bridge.Request`와 비어있는 `bridge.Response` 객체를 전달합니다.
5.  **[container]** `BridgeGateway`는 전달받은 `bridge` 객체들을 감싸 `HttpRequest`와 `HttpResponse` 객체를 생성합니다.
6.  **[container]** `ServletMapper`와 `FilterMapper`를 사용하여 현재 요청 URI에 해당하는 서블릿과 필터 체인을 찾습니다.
7.  **[container]** `ApplicationFilterChain`을 통해 필터들이 순서대로 실행되고, 마지막으로 대상 서블릿의 `service()` 메서드가 호출됩니다.
8.  **[container]** 서블릿은 `HttpRequest`에서 요청 정보를 읽고, `HttpResponse`에 응답 데이터를 기록합니다. 데이터는 `HttpResponse` 내부 버퍼에 저장됩니다.
9.  **[container -> bridge]** 서블릿 처리가 끝나고 응답이 커밋(commit)되면, `HttpResponse`의 버퍼에 담긴 내용이 `bridge.Response` 객체로 복사됩니다.
10. **[bridge -> net]** `Http11Processor`는 완성된 `bridge.Response` 객체를 HTTP/1.1 응답 포맷에 맞춰 직렬화한 후, `SocketWrapperBase`를 통해 클라이언트 소켓으로 전송합니다.