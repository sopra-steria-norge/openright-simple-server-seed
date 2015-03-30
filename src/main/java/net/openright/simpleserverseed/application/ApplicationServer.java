package net.openright.simpleserverseed.application;

import java.io.File;
import java.net.URI;

import net.openright.infrastructure.server.ServerUtil;
import net.openright.infrastructure.server.StatusHandler;
import net.openright.infrastructure.util.IOUtil;
import net.openright.infrastructure.util.LogUtil;

import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ShutdownHandler;

public class ApplicationServer {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ApplicationServer.class);
	private SimpleseedAppConfig config;
	private Server server;

	public ApplicationServer(SimpleseedAppConfig config) {
		this.config = config;
	}

	public static void main(String[] args) throws Exception {
		new File("logs").mkdirs();
		LogUtil.setupLogging("logging-simpleserverseed.xml");
		IOUtil.extractResourceFile("simpleserverseed.properties");

		SimpleseedAppConfig config = new SimpleseedAppConfigFile("src/main/resources/simpleserverseed.properties");
		new ApplicationServer(config).start(config.getHttpPort());
	}

	public void start(int port) throws Exception {
		new EnvEntry("jdbc/restjdbc", config.createDataSource());

		server = new Server(port);
		server.setHandler(createHandlers());
		server.start();

		log.info("Started server " + server.getURI());
	}

	private Handler createHandlers() {
		HandlerList handlers = new HandlerList();
		handlers.addHandler(new ShutdownHandler("sgds", false, true));
		handlers.addHandler(new WebAppContext("/myapp"));
		handlers.addHandler(new StatusHandler());

		return ServerUtil.createStatisticsHandler(
				ServerUtil.createRequestLogHandler(handlers));
	}

	public URI getURI() {
		return server.getURI();
	}


}
