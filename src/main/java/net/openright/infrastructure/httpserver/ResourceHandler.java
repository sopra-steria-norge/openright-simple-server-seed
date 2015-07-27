package net.openright.infrastructure.httpserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.json.JSONObject;
import org.json.JSONTokener;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import net.openright.infrastructure.rest.RequestException;
import net.openright.infrastructure.rest.ResourceApi;

public class ResourceHandler implements HttpHandler {

    private ResourceApi resourceApi;

    public ResourceHandler(ResourceApi resourceApi) {
        this.resourceApi = resourceApi;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String reqMethod = exchange.getRequestMethod();
        String reqId = getResourceId(exchange);
        if (reqMethod.equals("GET") && reqId != null) {
            respondWithJson(exchange, resourceApi.getResource(reqId));
        } else if (reqMethod.equals("GET")) {
            respondWithJson(exchange, resourceApi.listResources());
        } else if (reqMethod.equals("POST") && reqId == null) {
            String id = resourceApi.createResource(requestJson(exchange));
            exchange.getResponseHeaders().add("Location", exchange.getRequestURI() + id);
            exchange.sendResponseHeaders(201, -1);
        } else if (reqMethod.equals("PUT") || reqMethod.equals("POST")) {
            resourceApi.updateResource(reqId, requestJson(exchange));
            exchange.sendResponseHeaders(200, -1);
        } else {
            throw new RequestException("Unknown request " + reqMethod + " " + exchange.getRequestURI());
        }
    }

    private JSONObject requestJson(HttpExchange exchange) throws IOException {
        try (InputStream input = exchange.getRequestBody()) {
            return new JSONObject(new JSONTokener(input));
        }
    }

    private String getResourceId(HttpExchange exchange) {
        String[] parts = parseLocalPath(exchange);
        return parts.length == 0 ? null : parts[0];
    }

    String[] parseLocalPath(HttpExchange exchange) {
        return parseLocalPath(exchange.getHttpContext().getPath(), exchange.getRequestURI().getPath());
    }

    String[] parseLocalPath(String contextPath, String requestPath) {
        String localPath = requestPath.substring(contextPath.length());
        if (localPath.startsWith("/")) localPath = localPath.substring(1);
        if (localPath.isEmpty()) return new String[0];
        return localPath.split("/");
    }

    private void respondWithJson(HttpExchange exchange, JSONObject response) throws IOException {
        exchange.getResponseHeaders().set("Expires", "-1");
        exchange.getResponseHeaders().add("Content-type", "application/json");
        exchange.sendResponseHeaders(200, 0);
        try (Writer writer = new OutputStreamWriter(exchange.getResponseBody())) {
            response.write(writer);
        }
    }

}
