package org.entando.plugins.pda.pam.service.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.model.ProcessDefinition;
import org.entando.plugins.pda.core.service.task.ProcessService;
import org.entando.plugins.pda.pam.service.KieUtils;
import org.entando.plugins.pda.pam.service.process.model.KieProcessDefinition;
import org.entando.plugins.pda.pam.service.task.model.KieProcessDefinitionsResponse;
import org.entando.web.exception.BadResponseException;
import org.entando.web.request.PagedListRequest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class KieProcessService implements ProcessService {

    private final RestTemplateBuilder restTemplateBuilder;

    public static final int MAX_KIE_PAGE_SIZE = 2_000_000_000;

    //CHECKSTYLE:OFF
    public static final String PROCESS_DEFINITION_LIST_URL = "/queries/processes/definitions";
    //CHECKSTYLE:ON

    @Override
    public List<ProcessDefinition> listDefinitions(Connection connection) {
        RestTemplate restTemplate = restTemplateBuilder
                .basicAuthorization(connection.getUsername(), connection.getPassword())
                .build();

        List<ProcessDefinition> result = new ArrayList<>();

        PagedListRequest pageRequest = new PagedListRequest();
        pageRequest.setPageSize(MAX_KIE_PAGE_SIZE); //Set max page size to get all results

        List<KieProcessDefinition> response = getProcessesDefinitions(restTemplate, connection, pageRequest);
        while (!response.isEmpty()) { //Continue requesting pages if total results bigger than max page size
            result.addAll(response);
            pageRequest.setPage(pageRequest.getPage() + 1);

            response = getProcessesDefinitions(restTemplate, connection, pageRequest);
        }

        return result;
    }

    private List<KieProcessDefinition> getProcessesDefinitions(RestTemplate restTemplate, Connection connection,
            PagedListRequest pageRequest) {
        String url = connection.getUrl() + PROCESS_DEFINITION_LIST_URL
                + KieUtils.createFilters(pageRequest, true);

        KieProcessDefinitionsResponse response = Optional.ofNullable(restTemplate.getForObject(url,
                KieProcessDefinitionsResponse.class))
                .orElseThrow(BadResponseException::new);

        return Optional.ofNullable(response.getProcesses())
                .orElse(Collections.emptyList());
    }
}
