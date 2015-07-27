package net.openright.infrastructure.httpserver;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.openright.infrastructure.rest.RequestException;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

public class ErrorHandlerFilter extends Filter {

    private static final Logger log = LoggerFactory.getLogger(ErrorHandlerFilter.class);

    @Override
    public String description() {
        return "Error handler";
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        try {
            chain.doFilter(exchange);
        } catch (FileNotFoundException e) {
            log.info("File not found {}: {}", exchange.getRequestURI(), e.toString());
            sendResponseMessage(exchange, 404, "404 Not found: " + exchange.getRequestURI().getPath());
        } catch (RequestException e) {
            log.warn("Request exception while handling {}: {}", exchange.getRequestURI(), e.toString());
            sendResponseMessage(exchange, e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            log.error("While handing {}", exchange.getRequestURI(), e);
            sendResponseMessage(exchange, 500, "Server error");
        }
    }

    private void sendResponseMessage(HttpExchange exchange, int statusCode, String message)
            throws IOException {
        exchange.getResponseHeaders().add("Content-type", "text/plain");
        exchange.sendResponseHeaders(statusCode, 0);
        try (Writer response = new OutputStreamWriter(exchange.getResponseBody())) {
            response.write(message);
        }
    }

}