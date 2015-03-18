package net.openright.simpleserverseed.application;

import java.io.File;

import net.openright.simpleserverseed.infrastructure.server.ServerUtil;
import net.openright.simpleserverseed.infrastructure.server.StatusHandler;
import net.openright.simpleserverseed.infrastructure.util.IOUtil;
import net.openright.simpleserverseed.infrastructure.util.LogUtil;

import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ShutdownHandler;

public class ApplicationServer {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ApplicationServer.class);
	private SimpleseedAppConfig config;

	public ApplicationServer(SimpleseedAppConfig config) {
		this.config = config;
	}

	public static void main(String[] args) throws Exception {
		new File("logs").mkdirs();
		LogUtil.setupLogging("logging-simpleserverseed.xml");
		IOUtil.extractResourceFile("simpleserverseed.properties");
		
		new ApplicationServer(new SimpleseedAppConfigFile("src/main/resources/simpleserverseed.properties")).run(args);
	}

	private void run(String[] args) throws Exception {
		start();
	}

	private void start() throws Exception {
		new EnvEntry("jdbc/restjdbc", config.createDataSource());
		
		Server server = new Server(8000);
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

	
}
