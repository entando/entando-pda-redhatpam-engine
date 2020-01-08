package org.entando.plugins.pda.pam.exception;

import org.entando.web.exception.BadRequestException;

public class KieInvalidPageStart extends BadRequestException {

    public KieInvalidPageStart() {
        super("org.entando.kie.error.list.pageStart");
    }

}
