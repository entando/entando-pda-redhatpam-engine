package org.entando.plugins.pda.pam.exception;

import org.entando.plugins.pda.core.exception.BadRequestException;

public class KieInvalidPageStart extends BadRequestException {

    public KieInvalidPageStart() {
        super("org.entando.kie.error.list.pageStart");
    }

}
