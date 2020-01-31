package org.entando.plugins.pda.pam.service.task;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entando.keycloak.security.AuthenticatedUser;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.exception.AttachmentNotFoundException;
import org.entando.plugins.pda.core.exception.TaskNotFoundException;
import org.entando.plugins.pda.core.model.Attachment;
import org.entando.plugins.pda.core.request.CreateAttachmentRequest;
import org.entando.plugins.pda.core.service.task.TaskAttachmentService;
import org.entando.plugins.pda.pam.exception.KieInvalidResponseException;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.entando.plugins.pda.pam.service.util.KieInstanceId;
import org.entando.web.exception.BadRequestException;
import org.entando.web.exception.InternalServerException;
import org.kie.server.api.exception.KieServicesHttpException;
import org.kie.server.api.model.instance.TaskAttachment;
import org.kie.server.client.UserTaskServicesClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
            if (e.getHttpCode().equals(HttpStatus.NOT_FOUND.value())
                    //Some endpoints return 500 instead of 404
                    || e.getHttpCode().equals(HttpStatus.INTERNAL_SERVER_ERROR.value())) {
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
            if (e.getHttpCode().equals(HttpStatus.NOT_FOUND.value())
                    //Some endpoints return 500 instead of 404
                    || e.getHttpCode().equals(HttpStatus.INTERNAL_SERVER_ERROR.value())) {
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

        MultipartFile file = Optional.ofNullable(request.getFile())
                .orElseThrow(BadRequestException::new);

        String createdBy = user == null ? connection.getUsername() : user.getAccessToken().getPreferredUsername();

        try {
            Long attachmentId = client.addTaskAttachment(taskId.getContainerId(), taskId.getInstanceId(),
                    createdBy, file.getOriginalFilename(), file.getBytes());

            return get(connection, user, id, attachmentId.toString());
        } catch (KieServicesHttpException e) {
            if (e.getHttpCode().equals(HttpStatus.NOT_FOUND.value())
                    //Some endpoints return 500 instead of 404
                    || e.getHttpCode().equals(HttpStatus.INTERNAL_SERVER_ERROR.value())) {
                throw new TaskNotFoundException(e);
            }

            throw new KieInvalidResponseException(HttpStatus.valueOf(e.getHttpCode()), e.getMessage(), e);
        } catch (IOException e) {
            log.error("Error reading file", e);
            throw new InternalServerException(e.getMessage(), e);
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
            if (e.getHttpCode().equals(HttpStatus.NOT_FOUND.value())
                    //Some endpoints return 500 instead of 404
                    || e.getHttpCode().equals(HttpStatus.INTERNAL_SERVER_ERROR.value())) {
                throw new AttachmentNotFoundException(e);
            }

            throw new KieInvalidResponseException(HttpStatus.valueOf(e.getHttpCode()), e.getMessage(), e);
        }
    }

    @Override
    public byte[] file(Connection connection, HttpServletResponse response, AuthenticatedUser user, String id,
            String attachmentId) {
        UserTaskServicesClient client = kieApiService.getUserTaskServicesClient(connection);
        KieInstanceId taskId = new KieInstanceId(id);

        response.setContentType("application/jpeg");
        response.setHeader("Content-Type", "application/jpeg");
        response.addHeader("Content-Type", "application/jpeg");

        try {
            return (byte[]) client.getTaskAttachmentContentById(taskId.getContainerId(), taskId.getInstanceId(),
                    Long.valueOf(attachmentId));
        } catch (KieServicesHttpException e) {
            if (e.getHttpCode().equals(HttpStatus.NOT_FOUND.value())
                    //Some endpoints return 500 instead of 404
                    || e.getHttpCode().equals(HttpStatus.INTERNAL_SERVER_ERROR.value())) {
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
                .type(attachment.getContentType())
                .size(attachment.getSize().longValue())
                .build();
    }

    public static TaskAttachment attachmentToDto(Attachment attachment) {
        return TaskAttachment.builder()
                .id(attachment.getId() == null ? null : Long.valueOf(attachment.getId()))
                .name(attachment.getName())
                .addedAt(attachment.getCreatedAt())
                .addedBy(attachment.getCreatedBy())
                .contentType(attachment.getType())
                .size(attachment.getSize().intValue())
                .build();
    }
}
