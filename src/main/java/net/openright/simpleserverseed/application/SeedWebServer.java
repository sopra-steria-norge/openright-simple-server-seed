package net.openright.simpleserverseed.application;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.BindException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import net.openright.infrastructure.rest.RequestException;
import net.openright.infrastructure.util.ExceptionUtil;
import net.openright.infrastructure.util.IOUtil;
import net.openright.infrastructure.util.LogUtil;
import net.openright.simpleserverseed.domain.orders.OrdersApiController;
import net.openright.simpleserverseed.domain.products.ProductsApiController;

public class SeedWebServer {

    public static class ErrorHandlerFilter extends Filter {

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

    public static class AdminControlHandler implements HttpHandler {

        private HttpServer server;

        public AdminControlHandler(HttpServer server) {
            this.server = server;
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
                if (!token.equals("dsgsnl")) {
                    throw new RequestException(403, "Invalid token " + token);
                }

                exchange.sendResponseHeaders(200, -1);
                server.stop(0);
            } else {
                throw new RequestException(400, "Unrecognized action " + request);
            }
        }
    }

    public static class StaticContentHandler implements HttpHandler {
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
    }

    public static class RedirectHandler implements HttpHandler {
        private final String target;

        public RedirectHandler(String target) {
            this.target = target;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            verifyExactContextPath(exchange);
            exchange.getResponseHeaders().add("Location", "http://" + exchange.getRequestHeaders().getFirst("Host") + target);
            exchange.sendResponseHeaders(301, -1);
        }
    }


    private static final Logger log = LoggerFactory.getLogger(SeedWebServer.class);

    private SeedAppConfig config;

    private int port;

    public SeedWebServer(SeedAppConfig config) {
        this(config, config.getHttpPort());
    }

    public SeedWebServer(SeedAppConfig config, int port) {
        this.config = config;
        this.port = port;
    }

    private void start() throws IOException {
        start(port);
    }

    public void start(int port) throws IOException {
        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (BindException e) {
            log.error("Can't start server: Port already in use");
            return;
        }

        Filter errorHandlerFilter = new ErrorHandlerFilter();

        server.createContext("/seedapp/api/products", new ResourceHandler(new ProductsApiController(config)))
            .getFilters().add(errorHandlerFilter);
        server.createContext("/seedapp/api/orders", new ResourceHandler(new OrdersApiController(config)))
            .getFilters().add(errorHandlerFilter);

        server.createContext("/seedapp/", new StaticContentHandler(IOUtil.getResourcePath("/webapp/")))
            .getFilters().add(errorHandlerFilter);
        server.createContext("/seedapp", new RedirectHandler("/seedapp/"))
            .getFilters().add(errorHandlerFilter);

        server.createContext("/shutdown", new AdminControlHandler(server))
            .getFilters().add(errorHandlerFilter);
        server.createContext("/", new RedirectHandler("/seedapp"))
            .getFilters().add(errorHandlerFilter);

        config.start();
        server.start();

        this.port = server.getAddress().getPort();
    }

    private static void verifyExactContextPath(HttpExchange exchange) throws FileNotFoundException {
        if (!relativePath(exchange).isEmpty()) {
            throw new FileNotFoundException("Context " + exchange.getHttpContext().getPath() + " doesn't serve subpaths");
        }
    }

    private static String relativePath(HttpExchange exchange) {
        return exchange.getRequestURI().getPath().substring(exchange.getHttpContext().getPath().length());
    }

    public URI getURI() {
        try {
            return new URI("http://localhost:" + this.port);
        } catch (URISyntaxException e) {
            throw ExceptionUtil.soften(e);
        }
    }

    public static void main(String[] args) throws Exception {
        LogUtil.setupLogging("logging-simpleserverseed.xml");
        log.info("Starting server");

        SeedAppConfig config = new SeedAppConfigFile(IOUtil.extractResourceFile("seedapp.properties"));

        SeedWebServer server = new SeedWebServer(config);
        if (server.ensurePortIsAvailable()) {
            server.start();
            log.info("Started server " + server.getURI());
        } else {
            log.error("Failed to free port - cannot start");
        }
    }

    private boolean ensurePortIsAvailable() throws IOException {
        try {
            int response = sendShutdownRequest();
            log.info("Shutdown response: {}", response);
            return response == 200;
        } catch (ConnectException e) {
            log.debug("Shutdown not needed - server is not running");
            return true;
        }
    }

    private int sendShutdownRequest() throws ConnectException, IOException {
        return IOUtil.copy("SHUTDOWN dsgsnl", getURI().resolve("/shutdown"));
    }

}
