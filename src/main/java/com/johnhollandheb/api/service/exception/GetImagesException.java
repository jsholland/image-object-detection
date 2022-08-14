package com.johnhollandheb.api.service.exception;

public class GetImagesException extends RuntimeException {
    private static final String MESSAGE = "An unexpected error occurred retrieving list of images";

    public GetImagesException(Exception ex) {
        super(MESSAGE, ex);
    }
}
