package net.openright.infrastructure.rest;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.json.JSONTokener;

public class JsonPostController implements PostController {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JsonPostController.class);

	private JsonController jsonController;

	public JsonPostController(JsonController jsonController) {
		this.jsonController = jsonController;
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String[] parts = req.getPathInfo().split("\\/");

		try (BufferedReader reader = req.getReader()) {
			JSONObject jsonObject = new JSONObject(new JSONTokener(reader));
	        if (parts.length > 2) {
	            jsonController.putJSON(parts[2], jsonObject);
	        } else {
	            jsonController.postJSON(jsonObject);
	        }
			resp.sendError(200);
		} catch (RequestException e) {
			log.warn("Invalid request {}: {}", req.getRequestURL(), e.toString());
			resp.sendError(400, e.toString());
		} catch (RuntimeException e) {
			log.error("Failed to process " + req.getRequestURL(), e);
			resp.sendError(500, e.toString());
		}
	}

}
