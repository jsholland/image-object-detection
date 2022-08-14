package com.johnhollandheb.api.resource.exception;

public class GenericInternalServerException extends RuntimeException {
    public GenericInternalServerException(Exception ex) {
        super("An unexpected error occurred", ex);
    }
}
