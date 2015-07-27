package net.openright.simpleserverseed.application;

import java.io.IOException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.openright.infrastructure.httpserver.RedirectHandler;
import net.openright.infrastructure.httpserver.ResourceHandler;
import net.openright.infrastructure.httpserver.StaticContentHandler;
import net.openright.infrastructure.httpserver.WebServer;
import net.openright.infrastructure.util.LogUtil;
import net.openright.simpleserverseed.domain.orders.OrdersApiController;
import net.openright.simpleserverseed.domain.products.ProductsApiController;

public class SeedWebServer {

    public static final Logger log = LoggerFactory.getLogger(SeedWebServer.class);

    private SeedAppConfig config;

    private WebServer server;

    public SeedWebServer(SeedAppConfig config) {
        this(config, config.getHttpPort());
    }

    public SeedWebServer(SeedAppConfig config, int port) {
        this.config = config;
        this.server = new WebServer(port);
    }

    public void start() throws IOException {
        config.start();

        server.bind();
        server.addHandler("/seedapp/api/products", new ResourceHandler(new ProductsApiController(config)));
        server.addHandler("/seedapp/api/orders", new ResourceHandler(new OrdersApiController(config)));

        server.addHandler("/seedapp/", StaticContentHandler.classpathResource("/webapp/"));
        server.addHandler("/seedapp", new RedirectHandler("/seedapp/"));

        server.addControlHandler("/shutdown", "dsgsnl");
        server.addHandler("/", new RedirectHandler("/seedapp"));

        server.start();
    }

    public URI getURI() {
        return server.getURI();
    }

    public static void main(String[] args) throws Exception {
        LogUtil.setupLogging("logging-simpleserverseed.xml");
        log.info("Starting server");

        SeedWebServer server = new SeedWebServer(new SeedAppConfigFile());
        if (server.ensurePortIsAvailable()) {
            server.start();
            log.info("Started server " + server.getURI());
        } else {
            log.error("Failed to free port - cannot start");
        }
    }

    private boolean ensurePortIsAvailable() throws IOException {
        return server.remoteShutdown("/shutdown", "dsgsnl");
    }

}
