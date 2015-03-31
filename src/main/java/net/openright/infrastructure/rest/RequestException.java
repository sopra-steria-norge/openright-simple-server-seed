package net.openright.infrastructure.rest;

public class RequestException extends RuntimeException {

    private static final long serialVersionUID = -8877435859449649574L;
    private final int statusCode;

    public RequestException(String string) {
        this(400, string);
    }

    public RequestException(int statusCode, String string) {
        super(string);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

}
