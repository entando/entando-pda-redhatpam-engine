package org.entando.plugins.pda.pam.service.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.exception.ProcessNotFoundException;
import org.entando.plugins.pda.core.model.ProcessDefinition;
import org.entando.plugins.pda.core.service.process.ProcessService;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.entando.plugins.pda.pam.service.process.model.KieProcessDefinition;
import org.entando.plugins.pda.pam.service.task.model.KieProcessDefinitionsResponse;
import org.entando.plugins.pda.pam.service.util.KieInstanceId;
import org.entando.plugins.pda.pam.service.util.KieUtils;
import org.entando.web.exception.BadResponseException;
import org.entando.web.exception.InternalServerException;
import org.entando.web.request.PagedListRequest;
import org.kie.server.api.exception.KieServicesHttpException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class KieProcessService implements ProcessService {

    private final RestTemplateBuilder restTemplateBuilder;
    private final KieApiService kieApiService;

    public static final int MAX_KIE_PAGE_SIZE = 2_000_000_000;

    //CHECKSTYLE:OFF
    public static final String PROCESS_DEFINITION_LIST_URL = "/queries/processes/definitions";
    //CHECKSTYLE:ON

    @Override
    public List<ProcessDefinition> listDefinitions(Connection connection) {
        RestTemplate restTemplate = getRestTemplate(connection);

        List<ProcessDefinition> result = new ArrayList<>();

        PagedListRequest pageRequest = new PagedListRequest();
        pageRequest.setPageSize(MAX_KIE_PAGE_SIZE); //Set max page size to get all results

        List<KieProcessDefinition> response = performGetProcessesDefinitions(restTemplate, connection, pageRequest);
        while (!response.isEmpty()) { //Continue requesting pages if total results bigger than max page size
            result.addAll(response);
            pageRequest.setPage(pageRequest.getPage() + 1);

            response = performGetProcessesDefinitions(restTemplate, connection, pageRequest);
        }

        return result;
    }

    @Override
    public String getProcessDiagram(Connection connection, String id) {
        try {
            KieInstanceId compositeId = new KieInstanceId(id);

            return Optional.ofNullable(kieApiService.getUiServicesClient(connection)
                    .getProcessInstanceImage(compositeId.getContainerId(), compositeId.getInstanceId()))
                    .orElseThrow(ProcessNotFoundException::new);
        } catch (KieServicesHttpException e) {
            if (e.getHttpCode().equals(HttpStatus.NOT_FOUND.value())) {
                throw new ProcessNotFoundException(e);
            }

            throw new InternalServerException(e.getMessage(), e);
        }
    }

    private List<KieProcessDefinition> performGetProcessesDefinitions(RestTemplate restTemplate, Connection connection,
            PagedListRequest pageRequest) {
        String url = connection.getUrl() + PROCESS_DEFINITION_LIST_URL
                + KieUtils.createFilters(pageRequest, true);

        KieProcessDefinitionsResponse response = Optional.ofNullable(restTemplate.getForObject(url,
                KieProcessDefinitionsResponse.class))
                .orElseThrow(BadResponseException::new);

        return Optional.ofNullable(response.getProcesses())
                .orElse(Collections.emptyList());
    }

    private RestTemplate getRestTemplate(Connection connection) {
        return restTemplateBuilder
                .basicAuthorization(connection.getUsername(), connection.getPassword())
                .build();
    }
}
