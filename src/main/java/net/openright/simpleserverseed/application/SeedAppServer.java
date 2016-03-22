package net.openright.simpleserverseed.application;

import net.openright.infrastructure.server.EmbeddedWebAppContext;
import net.openright.infrastructure.server.ServerUtil;
import net.openright.infrastructure.server.StatusHandler;
import net.openright.infrastructure.util.IOUtil;
import net.openright.infrastructure.util.LogUtil;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.MovedContextHandler;
import org.eclipse.jetty.server.handler.ShutdownHandler;

import java.io.File;
import java.net.URI;

public class SeedAppServer {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SeedAppServer.class);
    private SeedAppConfig config;
    private Server server;

    public SeedAppServer(SeedAppConfig config) {
        this.config = config;
    }

    public static void main(String[] args) throws Exception {
        SeedAppConfigFile config = new SeedAppConfigFile();

        new File("logs").mkdirs();
        LogUtil.setupLogging(config);
        IOUtil.extractResourceFile("seedapp.properties");

        SeedAppServer server = new SeedAppServer(config);
        server.start();

        if (System.getProperty("startBrowser") != null) {
            Runtime.getRuntime().exec("cmd /c \"start " + server.getURI() + "\"");
        }
    }

    private void start() throws Exception {
        start(config.getHttpPort());
    }

    public void start(int port) throws Exception {
        config.start();

        server = new Server(port);
        server.setHandler(createHandlers());
        server.start();

        log.warn("Started server " + server.getURI());
    }

    private Handler createHandlers() {
        HandlerList handlers = new HandlerList();
        handlers.addHandler(new ShutdownHandler("sgds", false, true));
        handlers.addHandler(new EmbeddedWebAppContext("/seedapp"));
        handlers.addHandler(new StatusHandler());
        handlers.addHandler(new MovedContextHandler(null, "/", "/seedapp"));

        return ServerUtil.createStatisticsHandler(
                ServerUtil.createRequestLogHandler(handlers));
    }

    public URI getURI() {
        return server.getURI();
    }

}
