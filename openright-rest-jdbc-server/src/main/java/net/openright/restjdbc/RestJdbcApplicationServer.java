package net.openright.restjdbc;

import java.io.File;

import net.openright.infrastructure.server.ServerUtil;
import net.openright.infrastructure.server.StatusHandler;
import net.openright.infrastructure.util.IOUtil;
import net.openright.infrastructure.util.LogUtil;

import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ShutdownHandler;

public class RestJdbcApplicationServer {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RestJdbcApplicationServer.class);
	private RestJdbcAppConfig config;

	public RestJdbcApplicationServer(RestJdbcAppConfig config) {
		this.config = config;
	}

	public static void main(String[] args) throws Exception {
		new File("logs").mkdirs();
		LogUtil.setupLogging("logging-restjdbc.xml");
		IOUtil.extractResourceFile("restjdbc.properties");
		
		new RestJdbcApplicationServer(new RestJdbcAppConfigFile("restjdbc.properties")).run(args);
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
		handlers.addHandler(new RestJdbcWebAppContext("/myapp"));
		handlers.addHandler(new StatusHandler());
        
		return ServerUtil.createStatisticsHandler(
				ServerUtil.createRequestLogHandler(handlers));
	}

	
}
