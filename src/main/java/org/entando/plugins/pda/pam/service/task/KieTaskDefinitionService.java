package org.entando.plugins.pda.pam.service.task;

import static org.entando.plugins.pda.pam.service.task.util.TaskUtil.extractKeys;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entando.plugins.pda.core.service.task.TaskDefinitionService;
import org.entando.plugins.pda.pam.service.task.model.KieTask;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KieTaskDefinitionService implements TaskDefinitionService {

    @Override
    public Set<String> listColumns() {
        return extractKeys(new KieTask());
    }

}
