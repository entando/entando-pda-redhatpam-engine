package org.entando.plugins.pda.pam.exception;

import org.entando.plugins.pda.core.exception.NoConnectionWithBpmServerException;

public class NoConnectionWithKieServerException extends NoConnectionWithBpmServerException {

    public NoConnectionWithKieServerException(Throwable throwable) {
        super("org.entando.kie.error.noConnection", throwable);
    }

}
