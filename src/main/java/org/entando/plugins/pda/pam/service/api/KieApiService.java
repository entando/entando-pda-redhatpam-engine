package org.entando.plugins.pda.pam.service.api;

import com.google.common.annotations.VisibleForTesting;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.entando.plugins.pda.core.engine.Connection;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.springframework.stereotype.Service;

@Service
public class KieApiService {

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
        return kieServicesClient;
    }

    public ProcessServicesClient getProcessServicesClient(Connection connection) {
        KieServicesClient client = getKieServicesClient(connection);
        return client.getServicesClient(ProcessServicesClient.class);
    }

    public QueryServicesClient getQueryServicesClient(Connection connection) {
        KieServicesClient client = getKieServicesClient(connection);
        return client.getServicesClient(QueryServicesClient.class);
    }

    @VisibleForTesting
    public Map<Connection, KieServicesClient> getKieServicesClientMap() {
        return Collections.unmodifiableMap(kieServicesClientMap);
    }
}
