package net.openright.infrastructure.rest;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface GetController {

	void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException;

}
