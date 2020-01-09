package org.entando.plugins.pda.pam.service.util;

import java.util.Optional;
import org.entando.web.exception.BadRequestException;

public class KieDefinitionId {
    public static final String SEPARATOR = "@";
    public static final int SIZE = 2;

    private final String containerId;
    private final String definitionId;

    public KieDefinitionId(String id) {
        String[] split = Optional.ofNullable(id)
                .orElseThrow(() -> new BadRequestException("org.entando.kie.error.id"))
                .split(SEPARATOR);

        if (split.length != SIZE) {
            throw new BadRequestException("org.entando.kie.error.id");
        }

        this.definitionId = split[0];
        this.containerId = split[1];
    }

    public String getContainerId() {
        return containerId;
    }

    public String getDefinitionId() {
        return definitionId;
    }

    @Override
    public String toString() {
        return definitionId + SEPARATOR + containerId;
    }
}
