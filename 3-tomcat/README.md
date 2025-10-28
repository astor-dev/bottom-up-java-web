# Embedded Tomcat을 이용한 서블릿 배포

이 프로젝트는 [2번 모듈](../2-servlet-container-impl/README.md)에서 직접 구현했던 서블릿 컨테이너를, 상용 웹 서버인 **Apache Tomcat**을 내장(Embedded) 방식으로 사용하여 동일한 웹 애플리케이션을 실행하도록 재구성한 버전입니다.

## 핵심 아이디어

-   **직접 구현 대체**: `net`, `bridge`, `container` 계층으로 나누어 직접 만들었던 복잡한 네트워크 처리 및 서블릿 생명주기 관리 로직을 모두 제거했습니다.
-   **Tomcat 활용**: 검증되고 표준화된 `tomcat-embed-core` 라이브러리를 사용하여 웹 서버의 모든 기능을 톰캣 엔진에 위임합니다.
-   **간소화된 설정**: `WebApplication.java` 클래스에서는 Tomcat 인스턴스를 프로그래밍 방식으로 생성하고, 컨텍스트 경로와 클래스 파일 위치 등 최소한의 설정만으로 서버를 구동합니다.
-   **표준 서블릿 배포**: `HelloWorldServlet`은 이제 표준 Jakarta EE 어노테이션인 `@WebServlet("/hello")`을 사용하여 URL에 매핑됩니다. 내장 톰캣은 이 어노테이션을 자동으로 감지하여 서블릿을 등록하고 실행합니다.

이 방식을 통해 우리는 저수준의 복잡한 구현에서 벗어나, 애플리케이션 로직(서블릿) 개발에만 집중할 수 있게 됩니다.

---

> ### 🔗 직접 구현한 서블릿 컨테이너 아키텍처
> 
> 이 모듈의 방식과 비교해 보려면 아래 링크를 통해 2번 모듈의 상세한 아키텍처 설명을 확인해 보세요.
> 
> -   **[../2-servlet-container-impl/README.md](../2-servlet-container-impl/README.md)**
