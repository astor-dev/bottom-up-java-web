# Spring Web MVC를 통한 웹 계층의 분리

이 프로젝트는 이전 모듈들과는 근본적으로 다른 접근 방식을 보여줍니다. 바로 **스프링 프레임워크(Spring Framework)**, 특히 Spring Web MVC를 도입하여 웹 애플리케이션을 구축하는 것입니다.

## 스프링의 철학: 분리(Separation)와 제어의 역전(IoC)

이전 모듈들에서는 개발자가 직접 서버를 띄우고(3번 모듈) 서블릿의 생명주기를 관리(2번 모듈)하는 코드를 작성했습니다. 하지만 스프링을 사용하면서 이러한 책임은 프레임워크와 서블릿 컨테이너(WAS)에게 위임됩니다. 이것이 스프링의 핵심 철학인 **제어의 역전(Inversion of Control)** 입니다.

이 모듈의 가장 중요한 특징은 **웹 애플리케이션과 서버 환경의 완전한 분리**입니다.

-   **빌드 결과물**: 이 프로젝트는 `WAR(Web Application Archive)` 파일로 빌드됩니다. 이 파일 안에는 내장 톰캣과 같은 서버가 포함되어 있지 않으며, 순수하게 애플리케이션 로직(컨트롤러, 설정 등)과 의존성 라이브러리만 담겨 있습니다.
-   **배포 방식**: 생성된 `WAR` 파일은 이미 실행 중인 **외부(External) 서블릿 컨테이너**(예: 별도로 설치된 Apache Tomcat)의 특정 폴더(e.g., `webapps`)에 배포됩니다.
-   **`providedCompile`**: `build.gradle.kts` 파일을 보면 `jakarta.servlet-api` 의존성이 `providedCompile`로 선언되어 있습니다. 이는 "서블릿 API는 컴파일 시점에만 필요하고, 실제 실행 환경(톰캣)에서 제공될 것이므로 최종 WAR 파일에는 포함하지 말라"는 의미입니다. 이는 애플리케이션과 서버 환경이 명확히 분리되었음을 보여주는 대표적인 증거입니다.

## 주요 아키텍처

### 1. `DispatcherServlet` (Front Controller Pattern)

모든 클라이언트의 요청은 이제 스프링이 제공하는 단 하나의 서블릿, `DispatcherServlet`을 통해 처리됩니다. 이 서블릿은 웹 요청을 받는 중앙 창구 역할을 하며, 들어온 요청을 분석하여 가장 적절한 애플리케이션 코드(컨트롤러)에 분배(dispatch)합니다.

-   **`WebApplication.java`**: `AbstractAnnotationConfigDispatcherServletInitializer`를 상속받아 `DispatcherServlet`을 서블릿 컨테이너에 프로그래밍 방식으로 등록하는 역할을 합니다. 서블릿 컨테이너가 시작될 때 이 클래스를 감지하여 스프링 애플리케이션을 초기화합니다.

### 2. POJO 기반의 컨트롤러 (`@RestController`)

HTTP 요청을 처리하는 로직은 더 이상 `HttpServlet`을 상속받을 필요가 없는, 평범한 자바 객체(POJO)인 `HelloWorldController` 안에 구현됩니다.

-   `@RestController`: 이 어노테이션을 통해 해당 클래스가 RESTful 요청을 처리하는 컨트롤러임을 명시합니다. 메서드의 반환 값은 뷰(View) 이름이 아닌, HTTP 응답 본문(Response Body)에 직접 작성됩니다.
-   `@RequestMapping`: 특정 URL 경로와 HTTP 메서드를 처리할 메서드에 매핑합니다. `DispatcherServlet`은 이 어노테이션 정보를 보고 요청을 해당 메서드로 전달합니다.

### 3. Java 기반 설정 (`@Configuration`)

과거 XML로 관리되던 스프링 설정을 `WebConfig.java`와 같은 자바 클래스를 통해 수행합니다. 이를 통해 타입 안정성을 확보하고, 설정을 코드로 관리할 수 있게 됩니다.

## 실행 흐름

1.  개발자는 프로젝트를 `WAR` 파일로 빌드하여 외부 톰캣 서버에 배포합니다.
2.  톰캣 서버가 시작되면서 `WAR` 파일을 로드하고, 스프링이 제공하는 `ServletContainerInitializer`를 감지합니다.
3.  `ServletContainerInitializer`는 `WebApplication` 클래스를 찾아 실행하여 `DispatcherServlet`을 톰캣(서블릿 컨테이너)에 등록합니다.
4.  클라이언트가 `GET /hello` 요청을 보냅니다.
5.  톰캣은 이 요청을 `DispatcherServlet`에게 전달합니다.
6.  `DispatcherServlet`은 `@RequestMapping` 정보를 바탕으로 `HelloWorldController`의 `helloWorld()` 메서드가 이 요청을 처리해야 함을 파악합니다.
7.  `DispatcherServlet`이 `helloWorld()` 메서드를 호출하고, 반환 값인 "Hello, World!" 문자열을 받습니다.
8.  `@RestController`의 영향으로, `DispatcherServlet`은 이 문자열을 HTTP 응답 본문에 담아 톰캣에게 돌려줍니다.
9.  톰캣이 최종 HTTP 응답을 클라이언트에게 전송합니다.
