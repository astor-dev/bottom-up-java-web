plugins {
    id("java")
}

group = "bottom-up-java-web"
version = "1.0-SNAPSHOT"

subprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
    }
}
