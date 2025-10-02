pluginManagement {
    plugins {
        kotlin("jvm") version "2.2.0"
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "bottom-up-java-web"
include("1-java-http-socket")
include("2-servlet-impl")
include("3-tomcat")
include("4-spring-mvc")