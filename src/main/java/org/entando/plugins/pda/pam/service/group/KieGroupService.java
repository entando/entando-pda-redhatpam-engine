package org.entando.plugins.pda.pam.service.group;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.service.group.GroupService;
import org.entando.plugins.pda.pam.service.api.CustomQueryService;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.kie.server.api.model.definition.AssociatedEntitiesDefinition;
import org.kie.server.client.ProcessServicesClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KieGroupService implements GroupService {

    private final CustomQueryService customQueryService;
    private final KieApiService kieApiService;

    @Override
    public List<String> list(Connection connection, String containerId, String processId) {
        if (containerId != null && processId != null) {
            ProcessServicesClient processServicesClient = kieApiService.getProcessServicesClient(connection);
            AssociatedEntitiesDefinition associatedEntityDefinitions = processServicesClient
                    .getAssociatedEntityDefinitions(containerId, processId);
            String[] inGroups = associatedEntityDefinitions.getAssociatedEntities().values().stream()
                    .flatMap(Arrays::stream).toArray(String[]::new);
            if (inGroups.length == 0) {
                return Collections.emptyList();
            }
            return customQueryService.getGroups(connection, inGroups);
        }
        return customQueryService.getGroups(connection);
    }
}
