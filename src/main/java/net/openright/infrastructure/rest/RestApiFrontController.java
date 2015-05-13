package net.openright.infrastructure.rest;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RestApiFrontController extends HttpServlet {


    private static final Logger log = LoggerFactory.getLogger(RestApiFrontController.class);

	private static final long serialVersionUID = 7392483750149012130L;

	private static class NotFoundController implements GetController, PostController {
		@Override
		public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		    log.warn("404 {} {}", req.getMethod(), req.getRequestURL());
			resp.sendError(404);
		}

		@Override
		public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	        log.warn("404 {} {}", req.getMethod(), req.getRequestURL());
			resp.sendError(404);
		}
	}

	protected abstract Map<String, GetController> getControllers();

	protected abstract Map<String, PostController> postControllers();

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
	    log.info("{} {} {}",req.getProtocol(), req.getMethod(), req.getRequestURL());
		super.service(req, resp);
		log.info("{} {}",req.getProtocol(), resp.getStatus());
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		GetController controller = getControllers()
				.getOrDefault(getControllerName(req), new NotFoundController());
		controller.doGet(req, resp);
	}

    private String getControllerName(HttpServletRequest req) {
        return req.getPathInfo().split("\\/")[1];
    }

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		PostController controller = postControllers()
				.getOrDefault(getControllerName(req), new NotFoundController());
		controller.doPost(req, resp);
	}

}