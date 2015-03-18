package net.openright.infrastructure.rest;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

public interface JsonController {

	JSONObject getJSON(HttpServletRequest req);

	void postJSON(JSONObject jsonObject);

}
