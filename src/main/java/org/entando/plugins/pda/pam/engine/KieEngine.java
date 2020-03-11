package org.entando.plugins.pda.pam.engine;

import lombok.Builder;
import org.entando.plugins.pda.core.engine.Engine;
import org.entando.plugins.pda.pam.service.group.KieGroupService;
import org.entando.plugins.pda.pam.service.process.KieProcessFormService;
import org.entando.plugins.pda.pam.service.process.KieProcessService;
import org.entando.plugins.pda.pam.service.task.KieTaskCommentService;
import org.entando.plugins.pda.pam.service.task.KieTaskDefinitionService;
import org.entando.plugins.pda.pam.service.task.KieTaskFormService;
import org.entando.plugins.pda.pam.service.task.KieTaskLifecycleBulkService;
import org.entando.plugins.pda.pam.service.task.KieTaskLifecycleService;
import org.entando.plugins.pda.pam.service.task.KieTaskService;
import org.springframework.stereotype.Component;

@Component
public class KieEngine extends Engine {

    public static final String TYPE = "pam";

    @Builder
    public KieEngine(KieTaskService taskService, KieTaskDefinitionService taskDefinitionService,
            KieTaskCommentService taskCommentService, KieTaskFormService taskTaskFormService,
            KieTaskLifecycleService taskLifecycleService, KieTaskLifecycleBulkService taskLifecycleBulkService,
            KieProcessService processService, KieProcessFormService processFormService, KieGroupService groupService) {
        super(TYPE, taskService, taskDefinitionService, taskCommentService, taskTaskFormService, taskLifecycleService,
                taskLifecycleBulkService, processService, processFormService, groupService);
    }
}
