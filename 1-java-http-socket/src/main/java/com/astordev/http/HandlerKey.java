package com.astordev.http;

import java.util.Objects;

public class HandlerKey {
    private final HttpMethod method;
    private final String uri;

    public HandlerKey(HttpMethod method, String uri) {
        this.method = method;
        this.uri = uri;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HandlerKey that = (HandlerKey) o;
        return method == that.method && Objects.equals(uri, that.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, uri);
    }

    @Override
    public String toString() {
        return method + " " + uri;
    }
}
