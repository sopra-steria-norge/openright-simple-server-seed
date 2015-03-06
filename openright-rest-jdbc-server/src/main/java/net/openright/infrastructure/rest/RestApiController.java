package net.openright.infrastructure.rest;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class RestApiController extends HttpServlet {
	private static class NotFoundController implements GetController, PostController {
		@Override
		public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
			resp.sendError(404);
		}
		
		@Override
		public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
			resp.sendError(404);
		}
	}

	protected abstract Map<String, GetController> getControllers();

	protected abstract Map<String, PostController> postControllers();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		GetController controller = getControllers()
				.getOrDefault(req.getPathInfo(), new NotFoundController());
		controller.doGet(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		PostController controller = postControllers()
				.getOrDefault(req.getPathInfo(), new NotFoundController());
		controller.doPost(req, resp);
	}

}