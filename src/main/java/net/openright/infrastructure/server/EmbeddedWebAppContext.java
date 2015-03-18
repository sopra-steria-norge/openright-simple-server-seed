package net.openright.simpleserverseed.infrastructure.server;

import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;

public class EmbeddedWebAppContext extends WebAppContext {

	public EmbeddedWebAppContext(String contextPath) {
		setContextPath(contextPath);
		setBaseResource(Resource.newClassPathResource("/webapp"));
        
		avoidFileLocking();
	}

	private String avoidFileLocking() {
		return setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
	}


}