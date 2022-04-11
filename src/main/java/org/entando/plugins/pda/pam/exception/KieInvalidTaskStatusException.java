package org.entando.plugins.pda.pam.exception;

import org.entando.plugins.pda.core.exception.InternalServerException;

public class KieInvalidTaskStatusException extends InternalServerException {

    public KieInvalidTaskStatusException() {
        super("org.entando.kie.error.task.status");
    }
}
