package org.entando.plugins.pda.pam.service.process.model;

import java.util.Optional;
import org.entando.web.exception.BadRequestException;

public class KieProcessDefinitionId {
    public static final String SEPARATOR = "@";
    public static final int SIZE = 2;

    private String containerId;
    private String processDefinitionId;

    public KieProcessDefinitionId(String id) {
        String[] split = Optional.ofNullable(id)
                .orElseThrow(() -> new BadRequestException("org.entando.kie.error.id"))
                .split(SEPARATOR);

        if (split.length != SIZE) {
            throw new BadRequestException("org.entando.kie.error.id");
        }

        this.processDefinitionId = split[0];
        this.containerId = split[1];
    }

    public String getContainerId() {
        return containerId;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }
}
