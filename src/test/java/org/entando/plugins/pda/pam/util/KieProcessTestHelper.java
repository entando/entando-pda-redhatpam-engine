package org.entando.plugins.pda.pam.util;

import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.entando.plugins.pda.pam.service.process.model.KieProcessDefinition;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.ServiceResponse;

@UtilityClass
public class KieProcessTestHelper {

    public static final String PROCESS_ID_KEY = "id";
    public static final String PROCESS_NAME_KEY = "name";
    public static final String PROCESS_PROP_KEY = "new-key";

    public static final String PROCESS_DEFINITION_ID_1 = "1";
    public static final String CONTAINER_ID_1 = "container-1";
    public static final String PROCESS_NAME_1 = "Process 1";
    public static final String PROCESS_PROP_1 = "New Prop 1";

    public static final String PROCESS_DEFINITION_ID_2 = "2";
    public static final String CONTAINER_ID_2 = "container-2";
    public static final String PROCESS_NAME_2 = "Process 2";
    public static final String PROCESS_PROP_2 = "New Prop 2";

    public List<KieProcessDefinition> createKieProcessDefinitionList() {
        List<KieProcessDefinition> result = new ArrayList<>();

        result.add(KieProcessDefinition.builder()
                .extraProperty(PROCESS_ID_KEY, PROCESS_DEFINITION_ID_1)
                .extraProperty(PROCESS_NAME_KEY, PROCESS_NAME_1)
                .extraProperty(PROCESS_PROP_KEY, PROCESS_PROP_1)
                .build());

        result.add(KieProcessDefinition.builder()
                .extraProperty(PROCESS_ID_KEY, PROCESS_DEFINITION_ID_2)
                .extraProperty(PROCESS_NAME_KEY, PROCESS_NAME_2)
                .extraProperty(PROCESS_PROP_KEY, PROCESS_PROP_2)
                .build());

        return result;
    }

    public static ServiceResponse<KieContainerResourceList> createKieContainersResponse() {
        return new ServiceResponse<>(null, null,
                new KieContainerResourceList(createKieContainersList()));
    }

    private static List<KieContainerResource> createKieContainersList() {
        List<KieContainerResource> result = new ArrayList<>();

        result.add(new KieContainerResource(CONTAINER_ID_1, null));
        result.add(new KieContainerResource(CONTAINER_ID_2, null));

        return result;
    }
}
