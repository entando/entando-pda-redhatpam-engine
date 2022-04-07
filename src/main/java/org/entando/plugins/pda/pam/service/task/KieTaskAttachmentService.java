package org.entando.plugins.pda.pam.service.task;

import static org.entando.plugins.pda.pam.service.util.KieDocument.KIE_DOCUMENT_TYPE;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entando.keycloak.security.AuthenticatedUser;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.exception.AttachmentNotFoundException;
import org.entando.plugins.pda.core.exception.BadRequestException;
import org.entando.plugins.pda.core.exception.TaskNotFoundException;
import org.entando.plugins.pda.core.model.Attachment;
import org.entando.plugins.pda.core.model.File;
import org.entando.plugins.pda.core.request.CreateAttachmentRequest;
import org.entando.plugins.pda.core.service.task.TaskAttachmentService;
import org.entando.plugins.pda.pam.exception.KieInvalidResponseException;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.entando.plugins.pda.pam.service.util.KieDocument;
import org.entando.plugins.pda.pam.service.util.KieInstanceId;
import org.kie.server.api.exception.KieServicesHttpException;
import org.kie.server.api.model.instance.DocumentInstance;
import org.kie.server.api.model.instance.TaskAttachment;
import org.kie.server.client.DocumentServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KieTaskAttachmentService implements TaskAttachmentService {
    private final KieApiService kieApiService;

    @Override
    public List<Attachment> list(Connection connection, AuthenticatedUser user, String id) {
        UserTaskServicesClient client = kieApiService.getUserTaskServicesClient(connection);
        KieInstanceId taskId = new KieInstanceId(id);

        try {
            return client.getTaskAttachmentsByTaskId(taskId.getContainerId(), taskId.getInstanceId())
                    .stream()
                    .map(KieTaskAttachmentService::dtoToAttachment)
                    .collect(Collectors.toList());
        } catch (KieServicesHttpException e) {
            if (e.getHttpCode().equals(HttpStatus.NOT_FOUND.value())) {
                throw new TaskNotFoundException(e);
            }

            throw new KieInvalidResponseException(HttpStatus.valueOf(e.getHttpCode()), e.getMessage(), e);
        }
    }

    @Override
    public Attachment get(Connection connection, AuthenticatedUser user, String id, String attachmentId) {
        UserTaskServicesClient client = kieApiService.getUserTaskServicesClient(connection);
        KieInstanceId taskId = new KieInstanceId(id);

        try {
            TaskAttachment attachment = client.getTaskAttachmentById(taskId.getContainerId(), taskId.getInstanceId(),
                    Long.valueOf(attachmentId));

            return dtoToAttachment(attachment);
        } catch (KieServicesHttpException e) {
            if (e.getHttpCode().equals(HttpStatus.NOT_FOUND.value())) {
                throw new AttachmentNotFoundException(e);
            }

            throw new KieInvalidResponseException(HttpStatus.valueOf(e.getHttpCode()), e.getMessage(), e);
        }
    }

    @Override
    public Attachment create(Connection connection, AuthenticatedUser user, String id,
            CreateAttachmentRequest request) {

        UserTaskServicesClient client = kieApiService.getUserTaskServicesClient(connection);
        KieInstanceId taskId = new KieInstanceId(id);

        KieDocument document = new KieDocument(Optional.ofNullable(request.getFile())
                .orElseThrow(BadRequestException::new));

        String createdBy = user == null ? connection.getUsername() : user.getAccessToken().getPreferredUsername();

        try {
            Long attachmentId = client.addTaskAttachment(taskId.getContainerId(), taskId.getInstanceId(),
                    createdBy, document.getFilename(), document.getPayload());

            return get(connection, user, id, attachmentId.toString());
        } catch (KieServicesHttpException e) {
            if (e.getHttpCode().equals(HttpStatus.NOT_FOUND.value())) {
                throw new TaskNotFoundException(e);
            }

            throw new KieInvalidResponseException(HttpStatus.valueOf(e.getHttpCode()), e.getMessage(), e);
        }
    }

    @Override
    public String delete(Connection connection, AuthenticatedUser user, String id, String attachmentId) {
        UserTaskServicesClient client = kieApiService.getUserTaskServicesClient(connection);
        KieInstanceId taskId = new KieInstanceId(id);

        try {
            client.deleteTaskAttachment(taskId.getContainerId(), taskId.getInstanceId(), Long.valueOf(attachmentId));
            return attachmentId;
        } catch (KieServicesHttpException e) {
            if (e.getHttpCode().equals(HttpStatus.NOT_FOUND.value())) {
                throw new AttachmentNotFoundException(e);
            }

            throw new KieInvalidResponseException(HttpStatus.valueOf(e.getHttpCode()), e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public File download(Connection connection, AuthenticatedUser user, String id, String attachmentId) {
        UserTaskServicesClient client = kieApiService.getUserTaskServicesClient(connection);
        DocumentServicesClient documentClient = kieApiService.getDocumentServicesClient(connection);
        KieInstanceId taskId = new KieInstanceId(id);

        try {
            KieDocument document = new KieDocument((Map<String, Object>) client.getTaskAttachmentContentById(
                    taskId.getContainerId(), taskId.getInstanceId(), Long.valueOf(attachmentId)));

            DocumentInstance documentDetails = documentClient.getDocument(document.getId());
            document.setContent(documentDetails.getContent());

            return document.getFile();
        } catch (KieServicesHttpException e) {
            if (e.getHttpCode().equals(HttpStatus.NOT_FOUND.value())) {
                throw new AttachmentNotFoundException(e);
            }

            throw new KieInvalidResponseException(HttpStatus.valueOf(e.getHttpCode()), e.getMessage(), e);
        }
    }

    public static Attachment dtoToAttachment(TaskAttachment attachment) {
        return Attachment.builder()
                .id(attachment.getId().toString())
                .name(attachment.getName())
                .createdAt(attachment.getAddedAt())
                .createdBy(attachment.getAddedBy())
                //.type(attachment.getContentType()) //Kie API doesn't provide any way to know the file type
                .size(attachment.getSize().longValue())
                .build();
    }

    public static TaskAttachment attachmentToDto(Attachment attachment) {
        return TaskAttachment.builder()
                .id(attachment.getId() == null ? null : Long.valueOf(attachment.getId()))
                .name(attachment.getName())
                .addedAt(attachment.getCreatedAt())
                .addedBy(attachment.getCreatedBy())
                .contentType(KIE_DOCUMENT_TYPE)
                .size(attachment.getSize().intValue())
                .build();
    }
}
