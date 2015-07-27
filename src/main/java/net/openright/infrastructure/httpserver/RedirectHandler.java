package net.openright.infrastructure.httpserver;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RedirectHandler implements HttpHandler {
    private String relativePath(HttpExchange exchange) {
        return exchange.getRequestURI().getPath().substring(exchange.getHttpContext().getPath().length());
    }

    private void verifyExactContextPath(HttpExchange exchange) throws FileNotFoundException {
        if (!relativePath(exchange).isEmpty()) {
            throw new FileNotFoundException("Context " + exchange.getHttpContext().getPath() + " doesn't serve subpaths");
        }
    }

    private final String target;

    public RedirectHandler(String target) {
        this.target = target;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        verifyExactContextPath(exchange);
        exchange.getResponseHeaders().add("Location", getRequestAuthority(exchange) + target);
        exchange.sendResponseHeaders(301, -1);
    }

    /** Authority of an URL = [protocol]://[host]:[port] */
    private String getRequestAuthority(HttpExchange exchange) {
        return "http://" + exchange.getRequestHeaders().getFirst("Host");
    }
}