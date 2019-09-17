package org.entando.plugins.pda.pam.engine;

import org.entando.plugins.pda.core.engine.Engine;
import org.entando.plugins.pda.pam.service.task.KieTaskService;
import org.springframework.stereotype.Component;

@Component
public class KieEngine extends Engine {
    public static final String TYPE = "pam";

    public KieEngine(KieTaskService taskService) {
        super(TYPE, taskService);
    }
}
