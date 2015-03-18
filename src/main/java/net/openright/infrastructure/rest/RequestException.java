package net.openright.infrastructure.rest;

public class RequestException extends RuntimeException {

	private static final long serialVersionUID = -8877435859449649574L;

	public RequestException(String string) {
		super(string);
	}

}
