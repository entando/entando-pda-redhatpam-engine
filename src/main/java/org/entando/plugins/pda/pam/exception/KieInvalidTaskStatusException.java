package org.entando.plugins.pda.pam.exception;

import org.entando.web.exception.InternalServerException;

public class KieInvalidTaskStatusException extends InternalServerException {

    public KieInvalidTaskStatusException() {
        super("org.entando.kie.error.task.status");
    }
}
