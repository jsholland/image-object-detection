package com.johnhollandheb.api.service.exception;

public class InvalidImageUploadRequestException extends RuntimeException {
    public InvalidImageUploadRequestException(String message) {
        super(message);
    }
}
