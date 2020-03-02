package org.entando.plugins.pda.pam.service.task;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.entando.keycloak.security.AuthenticatedUser;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.exception.CommentNotFoundException;
import org.entando.plugins.pda.core.exception.TaskNotFoundException;
import org.entando.plugins.pda.core.model.Comment;
import org.entando.plugins.pda.core.service.task.TaskCommentService;
import org.entando.plugins.pda.core.service.task.request.CreateCommentRequest;
import org.entando.plugins.pda.pam.exception.KieInvalidResponseException;
import org.entando.plugins.pda.pam.service.api.CustomQueryService;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.entando.plugins.pda.pam.service.util.KieInstanceId;
import org.kie.server.api.exception.KieServicesHttpException;
import org.kie.server.api.model.instance.TaskComment;
import org.kie.server.client.UserTaskServicesClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KieTaskCommentService implements TaskCommentService {

    public static final String PDA_PREFIX = "pda_";

    private final KieApiService kieApiService;
    private final CustomQueryService customQueryService;

    @Override
    public List<Comment> listComments(Connection connection, AuthenticatedUser user, String id) {
        UserTaskServicesClient client = kieApiService.getUserTaskServicesClient(connection);
        KieInstanceId taskId = new KieInstanceId(id);

        try {
            return client.getTaskCommentsByTaskId(taskId.getContainerId(), taskId.getInstanceId())
                    .stream()
                    .map(KieTaskCommentService::dtoToComment)
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
    public Comment getComment(Connection connection, AuthenticatedUser user, String id, String commentId) {
        UserTaskServicesClient client = kieApiService.getUserTaskServicesClient(connection);
        KieInstanceId taskId = new KieInstanceId(id);

        try {
            TaskComment comment = client.getTaskCommentById(taskId.getContainerId(), taskId.getInstanceId(),
                    Long.valueOf(commentId));

            return dtoToComment(comment);
        } catch (KieServicesHttpException e) {
            if (e.getHttpCode().equals(HttpStatus.NOT_FOUND.value())
                    //Some endpoints return 500 instead of 404
                    || e.getHttpCode().equals(HttpStatus.INTERNAL_SERVER_ERROR.value())) {
                throw new CommentNotFoundException(e);
            }

            throw new KieInvalidResponseException(HttpStatus.valueOf(e.getHttpCode()), e.getMessage(), e);
        }
    }

    @Override
    public Comment createComment(Connection connection, AuthenticatedUser user, String id,
            CreateCommentRequest request) {
        UserTaskServicesClient client = kieApiService.getUserTaskServicesClient(connection);
        KieInstanceId taskId = new KieInstanceId(id);

        String createdBy = user == null ? connection.getUsername() : user.getAccessToken().getPreferredUsername();
        Date createdAt = new Date();

        try {
            // add prefix if username clashes with group name
            List<String> groups = customQueryService.getGroups(connection, createdBy);
            if (CollectionUtils.isNotEmpty(groups)) {
                createdBy = PDA_PREFIX + createdBy; //NOPMD: String concatenation is better here
            }
            Long commentId = client.addTaskComment(taskId.getContainerId(), taskId.getInstanceId(),
                    request.getComment(), createdBy, createdAt);

            return Comment.builder()
                    .id(commentId.toString())
                    .text(request.getComment())
                    .createdAt(createdAt)
                    .createdBy(createdBy)
                    .build();
        } catch (KieServicesHttpException e) {
            if (e.getHttpCode().equals(HttpStatus.NOT_FOUND.value())) {
                throw new TaskNotFoundException(e);
            }
            throw new KieInvalidResponseException(HttpStatus.valueOf(e.getHttpCode()), e.getMessage(), e);
        }
    }

    @Override
    public String deleteComment(Connection connection, AuthenticatedUser user, String id, String commentId) {
        UserTaskServicesClient client = kieApiService.getUserTaskServicesClient(connection);
        KieInstanceId taskId = new KieInstanceId(id);

        try {
            client.deleteTaskComment(taskId.getContainerId(), taskId.getInstanceId(), Long.valueOf(commentId));
            return commentId;
        } catch (KieServicesHttpException e) {
            if (e.getHttpCode().equals(HttpStatus.NOT_FOUND.value())
                    //Some endpoints return 500 instead of 404
                    || e.getHttpCode().equals(HttpStatus.INTERNAL_SERVER_ERROR.value())) {
                throw new CommentNotFoundException(e);
            }

            throw new KieInvalidResponseException(HttpStatus.valueOf(e.getHttpCode()), e.getMessage(), e);
        }
    }

    public static Comment dtoToComment(TaskComment comment) {
        return Comment.builder()
                .id(comment.getId().toString())
                .text(comment.getText())
                .createdAt(comment.getAddedAt())
                .createdBy(comment.getAddedBy())
                .build();
    }

    public static TaskComment commentToDto(Comment comment) {
        return TaskComment.builder()
                .id(comment.getId() == null ? null : Long.valueOf(comment.getId()))
                .text(comment.getText())
                .addedAt(comment.getCreatedAt())
                .addedBy(comment.getCreatedBy())
                .build();
    }
}
