package net.openright.infrastructure.rest;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsonbuddy.JsonObject;
import org.jsonbuddy.JsonValueNotPresentException;
import org.jsonbuddy.parse.JsonParser;

public class JsonResourceController implements Controller {
    private final ResourceApi resourceApi;

    public JsonResourceController(ResourceApi applicationApi) {
        this.resourceApi = applicationApi;
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        try {
            doHandle(req, resp);
        } catch (JsonValueNotPresentException e) {
            throw new RequestException(400, e.getMessage());
        }
    }

    private void doHandle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (req.getMethod().equals("POST")) {
            createResource(req, resp);
        } else if (req.getMethod().equals("PUT")) {
            updateResource(req, resp);
        } else if (req.getMethod().equals("GET")) {
            getResource(req, resp);
        } else {
            resp.sendError(400);
        }
    }

    private void getResource(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = getResourceId(req);
        if (id != null) {
            sendResponse(resp, resourceApi.getResource(id));
        } else {
            sendResponse(resp, resourceApi.listResources());
        }
    }

    protected String getResourceId(HttpServletRequest req) {
        String[] parts = req.getPathInfo().split("/");
        return parts.length > 2 ? parts[2] : null;
    }

    private void updateResource(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject jsonObject;
        try (BufferedReader reader = req.getReader()) {
            jsonObject = JsonParser.parseToObject(reader);
        }
        resourceApi.updateResource(getResourceId(req), jsonObject);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private void sendResponse(HttpServletResponse resp, JsonObject response) throws IOException {
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

    private void createResource(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject jsonObject;
        try (BufferedReader reader = req.getReader()) {
            jsonObject = JsonParser.parseToObject(reader);
        }
        if (getResourceId(req) != null) {
            resourceApi.updateResource(getResourceId(req), jsonObject);
        } else {
            String id = resourceApi.createResource(jsonObject);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.setHeader("Location", req.getRequestURL() + id);
        }
    }
}
