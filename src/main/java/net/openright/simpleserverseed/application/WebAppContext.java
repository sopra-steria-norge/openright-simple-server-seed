package net.openright.simpleserverseed.application;

import net.openright.simpleserverseed.infrastructure.server.EmbeddedWebAppContext;


public class WebAppContext extends EmbeddedWebAppContext {

	public WebAppContext(String contextPath) {
		super(contextPath);
	}

}
