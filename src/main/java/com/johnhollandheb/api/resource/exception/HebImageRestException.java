package com.johnhollandheb.api.resource.exception;

import javax.ws.rs.core.Response;

public class HebImageRestException extends RuntimeException {
    private Response.Status status;
    private Exception cause;

    public HebImageRestException(Response.Status status, Exception exception) {
        this.status = status;
        cause = exception;
    }

    public Response.Status getStatus() {
        return this.status;
    }

    public void setStatus(Response.Status status) {
        this.status = status;
    }

    public Throwable getCause() {
        return cause;
    }
}
