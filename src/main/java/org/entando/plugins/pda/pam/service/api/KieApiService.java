package org.entando.plugins.pda.pam.service.api;

import com.google.common.annotations.VisibleForTesting;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.pam.exception.NoConnectionWithKieServerException;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.definition.QueryDefinition;
import org.kie.server.client.DocumentServicesClient;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.UIServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.springframework.stereotype.Service;

@Service("KieApiService")
public class KieApiService {

    public static final String PDA_GROUPS = "pdaGroups";

    private final Map<Connection, KieServicesClient> kieServicesClientMap = new ConcurrentHashMap<>();

    public KieServicesClient getKieServicesClient(Connection connection) {
        KieServicesClient kieServicesClient = kieServicesClientMap.get(connection);
        if (kieServicesClient == null) {
            removeConnectionFromCacheByName(connection.getName());
            kieServicesClient = createKieServicesClient(connection);
            kieServicesClientMap.put(connection, kieServicesClient);
        }
        return kieServicesClient;
    }

    private void removeConnectionFromCacheByName(String connectionName) {
        kieServicesClientMap.keySet().stream()
                .filter(c -> c.getName().equals(connectionName))
                .findFirst()
                .ifPresent(kieServicesClientMap::remove);
    }

    private KieServicesClient createKieServicesClient(Connection connection) {
        KieServicesClient kieServicesClient;
        KieServicesConfiguration configuration = KieServicesFactory
                .newRestConfiguration(connection.getUrl(), connection.getUsername(), connection.getPassword());
        configuration.setMarshallingFormat(MarshallingFormat.JSON);
        kieServicesClient = KieServicesFactory.newKieServicesClient(configuration);
        registerCustomQueries(kieServicesClient);
        return kieServicesClient;
    }

    private void registerCustomQueries(KieServicesClient kieServicesClient) {
        QueryServicesClient queryClient = kieServicesClient.getServicesClient(QueryServicesClient.class);
        registerPdaGroupsQuery(queryClient);
    }

    private void registerPdaGroupsQuery(QueryServicesClient queryClient) {
        QueryDefinition queryDefinition = QueryDefinition.builder()
                .name(PDA_GROUPS)
                .source("${org.kie.server.persistence.ds}")
                .expression("SELECT id FROM organizationalentity\n"
                        + "WHERE dtype = 'Group'\n"
                ).target("CUSTOM")
                .build();
        queryClient.replaceQuery(queryDefinition);
    }

    public ProcessServicesClient getProcessServicesClient(Connection connection) {
        try {
            KieServicesClient client = getKieServicesClient(connection);
            return client.getServicesClient(ProcessServicesClient.class);
        } catch (RuntimeException e) { //NOPMD client throws generic exception
            throw new NoConnectionWithKieServerException(e);
        }
    }

    public QueryServicesClient getQueryServicesClient(Connection connection) {
        try {
            KieServicesClient client = getKieServicesClient(connection);
            return client.getServicesClient(QueryServicesClient.class);
        } catch (RuntimeException e) { //NOPMD client throws generic exception
            throw new NoConnectionWithKieServerException(e);
        }
    }

    public UIServicesClient getUiServicesClient(Connection connection) {
        try {
            KieServicesClient client = getKieServicesClient(connection);
            return client.getServicesClient(UIServicesClient.class);
        } catch (RuntimeException e) { //NOPMD client throws generic exception
            throw new NoConnectionWithKieServerException(e);
        }
    }

    public UserTaskServicesClient getUserTaskServicesClient(Connection connection) {
        try {
            KieServicesClient client = getKieServicesClient(connection);
            return client.getServicesClient(UserTaskServicesClient.class);
        } catch (RuntimeException e) { //NOPMD client throws generic exception
            throw new NoConnectionWithKieServerException(e);
        }
    }

    public DocumentServicesClient getDocumentServicesClient(Connection connection) {
        try {
            KieServicesClient client = getKieServicesClient(connection);
            return client.getServicesClient(DocumentServicesClient.class);
        } catch (RuntimeException e) { //NOPMD client throws generic exception
            throw new NoConnectionWithKieServerException(e);
        }
    }

    @VisibleForTesting
    public Map<Connection, KieServicesClient> getKieServicesClientMap() {
        return Collections.unmodifiableMap(kieServicesClientMap);
    }
}
