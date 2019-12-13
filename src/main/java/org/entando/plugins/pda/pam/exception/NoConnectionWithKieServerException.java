package org.entando.plugins.pda.pam.exception;

import org.entando.web.exception.InternalServerException;

public class NoConnectionWithKieServerException extends InternalServerException {

    public NoConnectionWithKieServerException(Throwable throwable) {
        super("org.entando.kie.error.noConnection", throwable);
    }

}
