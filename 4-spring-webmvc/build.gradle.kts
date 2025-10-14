plugins {
    java
    war
}

repositories {
    mavenCentral()
}

val springVersion = "6.2.10"

dependencies {
    implementation("org.springframework:spring-webmvc:$springVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")

    providedCompile("jakarta.servlet:jakarta.servlet-api:6.1.0")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
