plugins {
    application
}

application {
    mainClass.set("com.astordev.WebApplication")
}

dependencies {
    implementation("org.apache.tomcat.embed:tomcat-embed-core:11.0.11")
    implementation("org.apache.tomcat.embed:tomcat-embed-el:11.0.11")
}