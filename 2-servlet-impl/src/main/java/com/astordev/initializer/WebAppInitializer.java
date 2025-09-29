package com.astordev.initializer;

import com.astordev.servlet.HelloWorldServlet;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;

import java.util.Set;

public class WebAppInitializer implements ServletContainerInitializer {
    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
        System.out.println("WebAppInitializer.onStartup() called");
        ServletRegistration.Dynamic registration = ctx.addServlet("HelloWorldServlet", HelloWorldServlet.class);
        if (registration != null) {
            registration.addMapping("/hello");
        } else {
            System.err.println("Failed to register HelloWorldServlet");
        }
    }
}
