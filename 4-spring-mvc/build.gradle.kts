plugins {
    id("org.springframework.boot") version "3.5.5"
}

dependencies {
    implementation(platform(project.dependencies.create("org.springframework.boot:spring-boot-dependencies:3.5.5")))
    annotationProcessor(platform(project.dependencies.create("org.springframework.boot:spring-boot-dependencies:3.5.5")))
    implementation("org.springframework.boot:spring-boot-starter-web")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}
