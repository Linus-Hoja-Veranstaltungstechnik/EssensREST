package de.linushoja.essensrest.server.exceptions;

public class ForbiddenAccessException extends Exception{
    public ForbiddenAccessException() {
        super("Forbidden.");
    }
}
