package net.openright.infrastructure.httpserver;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import net.openright.infrastructure.util.IOUtil;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class StaticContentHandler implements HttpHandler {
    private String relativePath(HttpExchange exchange) {
        return exchange.getRequestURI().getPath().substring(exchange.getHttpContext().getPath().length());
    }

    private final Path rootResource;

    public StaticContentHandler(Path rootResource) {
        this.rootResource = rootResource;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Path resource = rootResource.resolve(relativePath(exchange));
        if (Files.isDirectory(resource)) {
            resource = resource.resolve("index.html");
        }
        sendContent(exchange, resource);
    }

    private void sendContent(HttpExchange exchange, Path resource) throws IOException {
        if (Files.notExists(resource) || !resource.startsWith(rootResource)) {
            throw new FileNotFoundException(resource.toString());
        }
        exchange.getResponseHeaders().add("Content-type", Files.probeContentType(resource));
        exchange.sendResponseHeaders(200, 0);
        try (OutputStream writer = exchange.getResponseBody()) {
            IOUtil.copy(resource, writer);
        }
    }

    public static StaticContentHandler classpathResource(String rootDirectory) {
        return new StaticContentHandler(IOUtil.getResourcePath(rootDirectory));
    }
}