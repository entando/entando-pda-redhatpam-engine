package org.entando.plugins.pda.pam.service.task;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.exception.TaskNotFoundException;
import org.entando.plugins.pda.core.model.form.Form;
import org.entando.plugins.pda.core.service.task.TaskFormService;
import org.entando.plugins.pda.pam.exception.KieInvalidResponseException;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.entando.plugins.pda.pam.service.process.KieFormDeserializer;
import org.entando.plugins.pda.pam.service.process.model.KieInstanceId;
import org.entando.web.exception.InternalServerException;
import org.kie.server.api.exception.KieServicesHttpException;
import org.kie.server.client.UIServicesClient;
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
    public List<Form> getTaskForm(Connection connection, String id) {
        KieInstanceId taskId = new KieInstanceId(id);

        UIServicesClient uiServicesClient = kieApiService.getUiServicesClient(connection);

        try {
            String json = uiServicesClient
                    .getTaskForm(taskId.getContainerId(), taskId.getInstanceId());

            return MAPPER.readValue(json, new TypeReference<List<Form>>() {});
        } catch (KieServicesHttpException e) {
            if (e.getHttpCode().equals(HttpStatus.NOT_FOUND.value())) {
                throw new TaskNotFoundException(e);
            }
            throw new KieInvalidResponseException(HttpStatus.valueOf(e.getHttpCode()), e.getMessage(), e);
        } catch (IOException e) {
            throw new InternalServerException(e.getMessage(), e);
        }
    }
}
