package net.openright.infrastructure.httpserver;

import java.io.FileNotFoundException;
import java.io.IOException;

import net.openright.infrastructure.rest.RequestException;
import net.openright.infrastructure.util.IOUtil;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class AdminControlHandler implements HttpHandler {

    private String relativePath(HttpExchange exchange) {
        return exchange.getRequestURI().getPath().substring(exchange.getHttpContext().getPath().length());
    }

    private void verifyExactContextPath(HttpExchange exchange) throws FileNotFoundException {
        if (!relativePath(exchange).isEmpty()) {
            throw new FileNotFoundException("Context " + exchange.getHttpContext().getPath() + " doesn't serve subpaths");
        }
    }

    private HttpServer server;
    private final String shutdownSecret;

    public AdminControlHandler(HttpServer server, String shutdownSecret) {
        this.server = server;
        this.shutdownSecret = shutdownSecret;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        verifyExactContextPath(exchange);
        if (!exchange.getRequestMethod().equals("POST")) {
            throw new RequestException(400, "Must be a post request");
        }
        if (exchange.getRemoteAddress().getAddress().isAnyLocalAddress()) {
            throw new RequestException(403, "Invalid client " + exchange.getRemoteAddress());
        }

        String request = IOUtil.toString(exchange.getRequestBody());
        if (request.startsWith("SHUTDOWN ")) {
            String token = request.substring("SHUTDOWN ".length());
            if (!token.equals(shutdownSecret)) {
                throw new RequestException(403, "Invalid token " + token);
            }

            exchange.sendResponseHeaders(200, -1);
            server.stop(0);
        } else {
            throw new RequestException(400, "Unrecognized action " + request);
        }
    }
}