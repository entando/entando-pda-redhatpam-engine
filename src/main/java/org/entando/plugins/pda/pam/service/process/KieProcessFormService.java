package org.entando.plugins.pda.pam.service.process;

import static org.entando.plugins.pda.pam.service.util.KieUtils.createFormSubmission;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.entando.keycloak.security.AuthenticatedUser;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.exception.ProcessDefinitionNotFoundException;
import org.entando.plugins.pda.core.model.form.Form;
import org.entando.plugins.pda.core.service.process.ProcessFormService;
import org.entando.plugins.pda.pam.exception.KieInvalidResponseException;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.entando.plugins.pda.pam.service.util.KieDefinitionId;
import org.entando.web.exception.InternalServerException;
import org.kie.server.api.exception.KieServicesHttpException;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.UIServicesClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KieProcessFormService implements ProcessFormService {

    public static final String INITIATOR_VAR = "initiator";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final KieApiService kieApiService;

    static {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Form.class, new KieFormDeserializer());
        MAPPER.registerModule(module);
    }

    @Override
    public Form get(Connection connection, String processDefinitionId) {
        KieDefinitionId id = new KieDefinitionId(processDefinitionId);

        UIServicesClient uiServicesClient = kieApiService.getUiServicesClient(connection);

        try {
            String json = uiServicesClient.getProcessForm(id.getContainerId(), id.getDefinitionId());
            return MAPPER.readValue(json, Form.class);
        } catch (KieServicesHttpException e) {
            if (e.getHttpCode().equals(HttpStatus.NOT_FOUND.value())) {
                throw new ProcessDefinitionNotFoundException(e);
            }
            throw new KieInvalidResponseException(HttpStatus.valueOf(e.getHttpCode()), e.getMessage(), e);
        } catch (IOException e) {
            throw new InternalServerException(e.getMessage(), e);
        }
    }

    @Override
    public String submit(Connection connection, String processDefinitionId, Map<String, Object> request,
            AuthenticatedUser user) {
        Form form = get(connection, processDefinitionId);

        Map<String, Object> variables = createFormSubmission(form, request, true);

        ProcessServicesClient client = kieApiService.getProcessServicesClient(connection);

        String username = user == null ? connection.getUsername() : user.getAccessToken().getPreferredUsername();
        try {
            KieDefinitionId id = new KieDefinitionId(processDefinitionId);
            Long processInstanceId = client.startProcess(id.getContainerId(), id.getDefinitionId(), variables);
            client.setProcessVariable(id.getContainerId(), processInstanceId, INITIATOR_VAR, username);
            return String.valueOf(processInstanceId);
        } catch (KieServicesHttpException e) {
            if (e.getHttpCode().equals(HttpStatus.NOT_FOUND.value())) {
                throw new ProcessDefinitionNotFoundException(e);
            }

            throw new KieInvalidResponseException(HttpStatus.valueOf(e.getHttpCode()), e.getMessage(), e);
        }
    }
}
