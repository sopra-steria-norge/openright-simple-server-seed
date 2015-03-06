package net.openright.restjdbc;

import java.io.File;

import net.openright.infrastructure.server.ServerUtil;
import net.openright.infrastructure.server.StatusHandler;
import net.openright.infrastructure.util.LogUtil;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ShutdownHandler;

public class RestJdbcApplicationServer {
	 private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RestJdbcApplicationServer.class);

	public static void main(String[] args) throws Exception {
		new File("logs").mkdirs();
		LogUtil.setupLogging("logging-restjdbc.xml");
		
		new RestJdbcApplicationServer().run(args);
	}

	private void run(String[] args) throws Exception {
		start();
	}

	private void start() throws Exception {
		Server server = new Server(8000);
		server.setHandler(createHandlers());
		server.start();
		
		log.info("Started server " + server.getURI());
	}

	private Handler createHandlers() {
		HandlerList handlers = new HandlerList();
		handlers.addHandler(new ShutdownHandler("sgds", false, true));
		handlers.addHandler(new StatusHandler());
		handlers.addHandler(new RestJdbcWebAppContext("/myapp"));
        
		return ServerUtil.createStatisticsHandler(
				ServerUtil.createRequestLogHandler(handlers));
	}

	
}
