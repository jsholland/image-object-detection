package com.johnhollandheb.api.service.exception;

public class ObjectDetectionException extends RuntimeException {
    private static final String MESSAGE = "An error occurred attempting to detect objects for an image";

    public ObjectDetectionException(Exception ex) {
        super(MESSAGE, ex);
    }
}
