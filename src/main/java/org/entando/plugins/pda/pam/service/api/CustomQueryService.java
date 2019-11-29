package org.entando.plugins.pda.pam.service.api;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.entando.plugins.pda.core.engine.Connection;
import org.kie.server.api.model.definition.QueryDefinition;
import org.kie.server.api.model.definition.QueryFilterSpec;
import org.kie.server.api.util.QueryFilterSpecBuilder;
import org.kie.server.client.QueryServicesClient;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomQueryService {

    public static final String PDA_GROUPS = "pdaGroups";
    private static final int PAGE_SIZE = 1000;

    private final KieApiService kieApiService;

    public List<String> getGroups(Connection connection, String... inGroups) {
        QueryServicesClient queryClient = kieApiService.getQueryServicesClient(connection);
        QueryDefinition queryDefinition = QueryDefinition.builder()
                .name(PDA_GROUPS)
                .source("${org.kie.server.persistence.ds}")
                .expression("SELECT id FROM organizationalentity\n"
                        + "WHERE dtype = 'Group'\n"
                ).target("CUSTOM")
                .build();
        queryClient.replaceQuery(queryDefinition);

        List<List> result;
        if (inGroups.length == 0) {
            result = queryClient.query(PDA_GROUPS, QueryServicesClient.QUERY_MAP_RAW, 0, PAGE_SIZE, List.class);
        } else {
            QueryFilterSpec spec = new QueryFilterSpecBuilder()
                    .in("id", Arrays.asList(inGroups))
                    .get();
            result = queryClient.query(PDA_GROUPS, QueryServicesClient.QUERY_MAP_RAW, spec, 0, PAGE_SIZE, List.class);
        }
        return result.stream().flatMap(List::stream).map(Object::toString).collect(Collectors.toList());
    }
}
