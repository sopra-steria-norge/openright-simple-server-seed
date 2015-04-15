package net.openright.infrastructure.rest;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonGetController implements GetController {

	private static final Logger log = LoggerFactory.getLogger(JsonGetController.class);

	private JsonController jsonController;

	public JsonGetController(JsonController jsonController) {
		this.jsonController = jsonController;
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String[] parts = req.getPathInfo().split("\\/");

		try {
			JSONObject json = parts.length > 2 ? jsonController.getJSON(parts[2]) : jsonController.listJSON(req);
			log.info("HTTP response body: {}", json);
			sendResponse(resp, json);
		} catch (RequestException e) {
			log.warn("Invalid request {}: {}", req.getRequestURL(), e.toString());
			resp.sendError(e.getStatusCode(), e.toString());
		}
	}

	private void sendResponse(HttpServletResponse resp, JSONObject response) throws IOException {
		resp.setHeader("Expires", "-1");

		if (response == null) {
			resp.setStatus(204);
			return;
		}

		resp.setContentType("application/json");
		try (Writer writer = resp.getWriter()) {
			writer.write(response.toString());
		}
	}

}
