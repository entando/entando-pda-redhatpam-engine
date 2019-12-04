package org.entando.plugins.pda.pam.service.process.model;

import java.util.Optional;
import org.entando.web.exception.BadRequestException;

public class KieProcessId {
    public static final String SEPARATOR = "@";

    private String containerId;
    private Long processId;

    public KieProcessId(String id) {
        String[] split = Optional.ofNullable(id)
                .orElseThrow(() -> new BadRequestException("org.entando.kie.error.id"))
                .split(SEPARATOR);

        if (split.length != 2) { //NOPMD
            throw new BadRequestException("org.entando.kie.error.id");
        }

        try {
            this.processId = Long.valueOf(split[0]);
            this.containerId = split[1];
        } catch (NumberFormatException e) {
            throw new BadRequestException("org.entando.kie.error.id", e);
        }
    }

    public String getContainerId() {
        return containerId;
    }

    public Long getProcessId() {
        return processId;
    }
}
