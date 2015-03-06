package net.openright.infrastructure.rest;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.json.JSONTokener;

public class JsonPostController implements PostController {

	private JsonController jsonController;

	public JsonPostController(JsonController jsonController) {
		this.jsonController = jsonController;
	}
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		try (BufferedReader reader = req.getReader()) {
			JSONObject jsonObject = new JSONObject(new JSONTokener(reader));
			jsonController.postJSON(jsonObject);
			resp.sendError(200);
		}
	}

}
