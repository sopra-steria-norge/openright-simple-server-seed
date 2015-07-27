package net.openright.infrastructure.httpserver;

import java.io.IOException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.openright.infrastructure.util.ExceptionUtil;
import net.openright.infrastructure.util.IOUtil;
import net.openright.simpleserverseed.application.SeedWebServer;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class WebServer {

    private static final Logger log = LoggerFactory.getLogger(WebServer.class);

    private HttpServer server;
    private Filter errorHandlerFilter;
    private int port;

    public WebServer(int port) {
        this.port = port;
        errorHandlerFilter = new ErrorHandlerFilter();
    }

    public void bind() throws IOException {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (BindException e) {
            throw new RuntimeException("Can't start server: Port already in use");
        }
    }

    public void addHandler(String path, HttpHandler handler) {
        server.createContext(path, handler).getFilters().add(errorHandlerFilter);
    }

    public void addControlHandler(String path, String shutdownSecret) {
        addHandler(path, new AdminControlHandler(server, shutdownSecret));
    }

    public URI getURI() {
        try {
            return new URI("http://localhost:" + this.port);
        } catch (URISyntaxException e) {
            throw ExceptionUtil.soften(e);
        }
    }

    public void start() {
        server.start();
    }

    public boolean remoteShutdown(String controlContextPath, String shutdownSecret) throws IOException {
        try {
            int response = sendShutdownRequest(controlContextPath, shutdownSecret);
            log.info("Shutdown response: {}", response);
            return response == 200;
        } catch (ConnectException e) {
            SeedWebServer.log.debug("Shutdown not needed - server is not running");
            return true;
        }
    }

    private int sendShutdownRequest(String controlContextPath, String shutdownSecret) throws ConnectException, IOException {
        return IOUtil.copy("SHUTDOWN " + shutdownSecret, getURI().resolve(controlContextPath));
    }


}
