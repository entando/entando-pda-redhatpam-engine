package org.entando.plugins.pda.pam.service.util;

import java.util.Optional;
import org.entando.plugins.pda.pam.exception.KieInvalidIdException;

public class KieInstanceId {
    public static final String SEPARATOR = "@";
    public static final int SIZE = 2;

    private final String containerId;
    private final Long instanceId;

    public KieInstanceId(String id) {
        String[] split = Optional.ofNullable(id)
                .orElseThrow(KieInvalidIdException::new)
                .split(SEPARATOR);

        if (split.length != SIZE) {
            throw new KieInvalidIdException();
        }

        try {
            this.instanceId = Long.valueOf(split[0]);
            this.containerId = split[1];
        } catch (NumberFormatException e) {
            throw new KieInvalidIdException(e);
        }
    }

    public KieInstanceId(String containerId, String instanceId) {
        try {
            this.instanceId = Long.valueOf(instanceId);
            this.containerId = containerId;
        } catch (NumberFormatException e) {
            throw new KieInvalidIdException(e);
        }
    }

    public KieInstanceId(String containerId, Long instanceId) {
        this.instanceId = instanceId;
        this.containerId = containerId;
    }

    public String getContainerId() {
        return containerId;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    @Override
    public String toString() {
        return instanceId.toString() + SEPARATOR + containerId;
    }
}
