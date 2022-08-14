package com.johnhollandheb.api.service.exception;

public class ImageSaveException extends RuntimeException {
    private static final String MESSAGE = "An error occurred converting the image upload.";

    public ImageSaveException(Exception ex) {
        super(MESSAGE, ex);
    }
}
