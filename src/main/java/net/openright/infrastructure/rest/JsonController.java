package net.openright.infrastructure.rest;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

public interface JsonController {

    JSONObject listJSON(HttpServletRequest req);

    JSONObject getJSON(String id);

    void postJSON(JSONObject jsonObject);

    void putJSON(String id, JSONObject jsonObject);

}
