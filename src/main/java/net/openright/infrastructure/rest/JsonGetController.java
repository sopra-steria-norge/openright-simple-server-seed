package net.openright.infrastructure.rest;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

public class JsonGetController implements GetController {

    private JsonController jsonController;

    public JsonGetController(JsonController jsonController) {
        this.jsonController = jsonController;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String[] parts = req.getPathInfo().split("\\/");

        if (parts.length > 2) {
            sendResponse(resp, jsonController.getJSON(parts[2]));
        } else {
            sendResponse(resp, jsonController.listJSON(req));
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
