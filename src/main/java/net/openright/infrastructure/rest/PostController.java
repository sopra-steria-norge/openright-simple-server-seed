package net.openright.infrastructure.rest;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface PostController {

	void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException;

}
