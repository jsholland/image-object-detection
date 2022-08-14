package com.johnhollandheb.api.service.exception;

public class ImageNotFoundException extends RuntimeException {
    private static final String MESSAGE = "An error occurred finding an image with the provded image id.";

    public ImageNotFoundException() {
        super(MESSAGE);
    }
}
