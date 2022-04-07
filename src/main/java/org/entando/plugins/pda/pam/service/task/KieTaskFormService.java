package org.entando.plugins.pda.pam.service.task;

import static org.entando.plugins.pda.pam.service.util.KieUtils.createFormSubmission;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.entando.keycloak.security.AuthenticatedUser;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.exception.InternalServerException;
import org.entando.plugins.pda.core.exception.TaskNotFoundException;
import org.entando.plugins.pda.core.model.form.Form;
import org.entando.plugins.pda.core.service.task.TaskFormService;
import org.entando.plugins.pda.pam.exception.KieInvalidResponseException;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.entando.plugins.pda.pam.service.process.KieFormDeserializer;
import org.entando.plugins.pda.pam.service.util.KieInstanceId;
import org.kie.server.api.exception.KieServicesHttpException;
import org.kie.server.client.UIServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KieTaskFormService implements TaskFormService {

    private final KieApiService kieApiService;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Form.class, new KieFormDeserializer());
        MAPPER.registerModule(module);
    }

    @Override
    public Form get(Connection connection, String id) {
        KieInstanceId taskId = new KieInstanceId(id);

        UIServicesClient client = kieApiService.getUiServicesClient(connection);

        try {
            String json = client.getTaskForm(taskId.getContainerId(), taskId.getInstanceId());
            return MAPPER.readValue(json, Form.class);
        } catch (KieServicesHttpException e) {
            if (e.getHttpCode().equals(HttpStatus.NOT_FOUND.value())) {
                throw new TaskNotFoundException(e);
            }
            throw new KieInvalidResponseException(HttpStatus.valueOf(e.getHttpCode()), e.getMessage(), e);
        } catch (IOException e) {
            throw new InternalServerException(e.getMessage(), e);
        }
    }

    @Override
    public String submit(Connection connection, AuthenticatedUser user, String id, Map<String, Object> request) {
        Form form = get(connection, id);

        Map<String, Object> variables = createFormSubmission(form, request, true);

        UserTaskServicesClient client = kieApiService.getUserTaskServicesClient(connection);

        try {
            KieInstanceId taskId = new KieInstanceId(id);
            client.saveTaskContent(taskId.getContainerId(), taskId.getInstanceId(), variables);
            return id;
        } catch (KieServicesHttpException e) {
            if (e.getHttpCode().equals(HttpStatus.NOT_FOUND.value())) {
                throw new TaskNotFoundException(e);
            }

            throw new KieInvalidResponseException(HttpStatus.valueOf(e.getHttpCode()), e.getMessage(), e);
        }
    }
}
