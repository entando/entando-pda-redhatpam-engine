package org.entando.plugins.pda.pam.exception;

import org.entando.web.exception.InternalServerException;

public class KieInvalidIdException extends InternalServerException {

    public KieInvalidIdException(Throwable throwable) {
        super("org.entando.kie.error.id", throwable);
    }

    public KieInvalidIdException() {
        this(null);
    }
}
