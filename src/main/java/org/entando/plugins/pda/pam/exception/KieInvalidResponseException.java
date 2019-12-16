package org.entando.plugins.pda.pam.exception;

import org.entando.web.exception.InternalServerException;
import org.springframework.http.HttpStatus;

public class KieInvalidResponseException extends InternalServerException {

    public KieInvalidResponseException(final HttpStatus status, final String message, Throwable throwable) {
        super(String.format("Unexpected KIE Response: {%s} - %s", status.toString(), message), throwable);
    }

    public KieInvalidResponseException(final HttpStatus status, final String message) {
        this(status, message, null);
    }
}
