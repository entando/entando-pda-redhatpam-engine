package org.entando.plugins.pda.pam.service.api;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.entando.plugins.pda.core.engine.Connection;
import org.kie.server.api.model.definition.QueryFilterSpec;
import org.kie.server.api.util.QueryFilterSpecBuilder;
import org.kie.server.client.QueryServicesClient;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomQueryService {

    private static final int ALL_ITEMS = -1;

    private final KieApiService kieApiService;

    public List<String> getGroups(Connection connection, String... inGroups) {
        QueryServicesClient queryClient = kieApiService.getQueryServicesClient(connection);
        List<List> result;
        if (inGroups.length == 0) {
            result = queryClient
                    .query(KieApiService.PDA_GROUPS, QueryServicesClient.QUERY_MAP_RAW, 0, ALL_ITEMS, List.class);
        } else {
            QueryFilterSpec spec = new QueryFilterSpecBuilder().in("id", Arrays.asList(inGroups)).get();
            result = queryClient
                    .query(KieApiService.PDA_GROUPS, QueryServicesClient.QUERY_MAP_RAW, spec, 0, ALL_ITEMS, List.class);
        }
        return result.stream().flatMap(List::stream).map(Object::toString).collect(Collectors.toList());
    }
}
