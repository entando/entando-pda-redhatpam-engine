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

    public static final String ID_SEPARATOR = "@";
    private static final int COMPOSED_ID_SIZE = 2;

    private final CustomQueryService customQueryService;
    private final KieApiService kieApiService;

    @Override
    public List<String> list(Connection connection, String processId) {
        String[] composedId = processId == null ? new String[0] : processId.split(ID_SEPARATOR);
        if (composedId.length == COMPOSED_ID_SIZE) {
            ProcessServicesClient processServicesClient = kieApiService.getProcessServicesClient(connection);
            AssociatedEntitiesDefinition associatedEntityDefinitions = processServicesClient
                    .getAssociatedEntityDefinitions(composedId[0], composedId[1]);
            String[] inGroups = associatedEntityDefinitions.getAssociatedEntities().values().stream()
                    .flatMap(Arrays::stream).toArray(String[]::new);
            return inGroups.length == 0 ? Collections.emptyList() : customQueryService.getGroups(connection, inGroups);
        }
        return customQueryService.getGroups(connection);
    }
}
