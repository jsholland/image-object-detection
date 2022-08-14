package com.johnhollandheb.api.service.exception;

public class ImageFileSaveException extends RuntimeException {
    private static final String MESSAGE = "An error occurred saving the image prior to image object detection.";

    public ImageFileSaveException(Exception ex) {
        super(MESSAGE, ex);
    }
}
