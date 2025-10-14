package com.astordev.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class HelloWorldServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("text/plain; charset=utf-8");
        PrintWriter writer = resp.getWriter();
        writer.println("Hello, World!");
        System.out.println("[HelloWorldServlet] Request processed");
    }
}
