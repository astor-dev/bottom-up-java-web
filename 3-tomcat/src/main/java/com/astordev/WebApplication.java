package com.astordev;

import org.apache.catalina.Context;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;

public class WebApplication {
    public static void main(String[] args) throws Exception {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.getConnector();

        String docBase = new File(".").getAbsolutePath();
        Context context = tomcat.addContext("", docBase);
        File compiledClassesDir = new File("build/classes/java/main");
        StandardRoot resources = new StandardRoot(context);
        DirResourceSet resourceSet = new DirResourceSet(
                resources,
                "/WEB-INF/classes",
                compiledClassesDir.getAbsolutePath(),
                "/"
        );
        resources.addPreResources(resourceSet);
        context.setResources(resources);
        context.addLifecycleListener(new ContextConfig());
        tomcat.start();
        System.out.println("Tomcat started on port 8080.");
        tomcat.getServer().await();
    }
}
