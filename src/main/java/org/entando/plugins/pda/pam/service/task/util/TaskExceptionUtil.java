package org.entando.plugins.pda.pam.service.task.util;

import lombok.experimental.UtilityClass;
import org.entando.plugins.pda.core.exception.TaskNotFoundException;
import org.entando.plugins.pda.core.service.task.response.TaskBulkActionResponse;
import org.entando.plugins.pda.pam.exception.KieInvalidResponseException;
import org.kie.server.api.exception.KieServicesHttpException;
import org.springframework.http.HttpStatus;

@UtilityClass
public class TaskExceptionUtil {

    public static void handleKieServicesHttpException(KieServicesHttpException e) {
        if (e.getHttpCode().equals(HttpStatus.NOT_FOUND.value())) {
            throw new TaskNotFoundException(e);
        }
        throw new KieInvalidResponseException(HttpStatus.valueOf(e.getHttpCode()), e.getMessage(), e);
    }

    public static TaskBulkActionResponse convertToTaskBulkActionResponse(KieServicesHttpException e, String id) {
        return TaskBulkActionResponse.builder()
                .id(id)
                .statusCode(e.getHttpCode())
                .build();
    }
}
