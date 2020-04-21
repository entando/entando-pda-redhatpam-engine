package org.entando.plugins.pda.pam.service.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.entando.plugins.pda.core.utils.TestUtils.CONTAINER_ID_1;
import static org.entando.plugins.pda.core.utils.TestUtils.CONTAINER_ID_2;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_ATTACHMENT_ID_1_1;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_ATTACHMENT_OWNER_2_2;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_ID_1;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_ID_2;
import static org.entando.plugins.pda.core.utils.TestUtils.getDummyConnection;
import static org.entando.plugins.pda.core.utils.TestUtils.getDummyUser;
import static org.entando.plugins.pda.core.utils.TestUtils.randomLongId;
import static org.entando.plugins.pda.core.utils.TestUtils.randomStringId;
import static org.entando.plugins.pda.core.utils.TestUtils.readFromFile;
import static org.entando.plugins.pda.pam.service.task.KieTaskAttachmentService.attachmentToDto;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Collectors;
import org.entando.plugins.pda.core.exception.AttachmentNotFoundException;
import org.entando.plugins.pda.core.model.Attachment;
import org.entando.plugins.pda.core.model.File;
import org.entando.plugins.pda.core.request.CreateAttachmentRequest;
import org.entando.plugins.pda.core.service.task.TaskAttachmentService;
import org.entando.plugins.pda.pam.exception.KieInvalidIdException;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.entando.plugins.pda.pam.service.util.KieDocument;
import org.entando.plugins.pda.pam.service.util.KieInstanceId;
import org.entando.plugins.pda.pam.util.KieTaskTestHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kie.server.api.exception.KieServicesHttpException;
import org.kie.server.api.model.instance.DocumentInstance;
import org.kie.server.client.DocumentServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.springframework.http.HttpStatus;

@SuppressWarnings({ "PMD.TooManyMethods", "PMD.ExcessiveImports" })
public class KieTaskAttachmentServiceTest {

    private TaskAttachmentService kieTaskAttachmentService;
    private UserTaskServicesClient userTaskServicesClient;
    private DocumentServicesClient documentServicesClient;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        KieApiService kieApiService = mock(KieApiService.class);
        userTaskServicesClient = mock(UserTaskServicesClient.class);
        documentServicesClient = mock(DocumentServicesClient.class);

        when(kieApiService.getUserTaskServicesClient(any())).thenReturn(userTaskServicesClient);
        when(kieApiService.getDocumentServicesClient(any())).thenReturn(documentServicesClient);

        kieTaskAttachmentService = new KieTaskAttachmentService(kieApiService);
    }

    @Test
    public void shouldListTaskAttachments() {
        // Given
        KieInstanceId taskId = new KieInstanceId(CONTAINER_ID_1, TASK_ID_1);
        List<Attachment> expected = KieTaskTestHelper.createKieTaskAttachments(TASK_ID_1);
        when(userTaskServicesClient.getTaskAttachmentsByTaskId(anyString(), anyLong()))
                .thenReturn(expected.stream()
                        .map(KieTaskAttachmentService::attachmentToDto)
                        .collect(Collectors.toList()));

        // When
        List<Attachment> attachments = kieTaskAttachmentService.list(getDummyConnection(), getDummyUser(),
                taskId.toString());

        // Then
        assertThat(attachments).isEqualTo(expected);
    }

    @Test
    public void shouldCreateTaskAttachment() {
        KieInstanceId taskId = new KieInstanceId(CONTAINER_ID_2, TASK_ID_2);
        Attachment expected = KieTaskTestHelper.createKieTaskAttachment();
        CreateAttachmentRequest request = CreateAttachmentRequest.builder()
                .file(readFromFile("task_attachment_file.txt"))
                .build();

        // Given
        when(userTaskServicesClient.addTaskAttachment(any(), anyLong(), anyString(), anyString(), any()))
                .thenReturn(randomLongId());

        when(userTaskServicesClient.getTaskAttachmentById(anyString(), anyLong(), anyLong()))
                .thenReturn(attachmentToDto(expected));

        // When
        Attachment attachment = kieTaskAttachmentService.create(getDummyConnection(),
                getDummyUser(TASK_ATTACHMENT_OWNER_2_2), taskId.toString(), request);

        // Then
        expected.setCreatedAt(attachment.getCreatedAt());
        assertThat(attachment).isEqualTo(expected);

        verify(userTaskServicesClient)
                .addTaskAttachment(eq(taskId.getContainerId()), eq(taskId.getInstanceId()),
                        eq(TASK_ATTACHMENT_OWNER_2_2), eq(expected.getName()), any());
    }

    @Test
    public void shouldGetTaskAttachment() {
        KieInstanceId taskId = new KieInstanceId(CONTAINER_ID_1, TASK_ID_1);

        // Given
        Attachment expected = KieTaskTestHelper.createKieTaskAttachments(TASK_ID_1).get(0);
        when(userTaskServicesClient.getTaskAttachmentById(anyString(), anyLong(), anyLong()))
                .thenReturn(attachmentToDto(expected));

        // When
        Attachment attachment = kieTaskAttachmentService.get(getDummyConnection(), null, taskId.toString(),
                TASK_ATTACHMENT_ID_1_1);

        // Then
        assertThat(attachment).isEqualTo(expected);
        verify(userTaskServicesClient)
                .getTaskAttachmentById(taskId.getContainerId(), taskId.getInstanceId(),
                        Long.valueOf(TASK_ATTACHMENT_ID_1_1));
    }

    @Test
    public void shouldDeleteTaskAttachment() {
        KieInstanceId taskId = new KieInstanceId(CONTAINER_ID_1, TASK_ID_1);

        // When
        String attachmentId = kieTaskAttachmentService.delete(getDummyConnection(), null, taskId.toString(),
                TASK_ATTACHMENT_ID_1_1);

        // Then
        assertThat(attachmentId).isEqualTo(TASK_ID_1);
        verify(userTaskServicesClient)
                .deleteTaskAttachment(taskId.getContainerId(), taskId.getInstanceId(),
                        Long.valueOf(TASK_ATTACHMENT_ID_1_1));
    }

    @Test
    public void shouldDownloadTaskAttachment() {
        KieInstanceId taskId = new KieInstanceId(CONTAINER_ID_1, TASK_ID_1);

        // Given
        DocumentInstance document = KieTaskTestHelper.createKieDocument();
        KieDocument expected = new KieDocument(readFromFile("task_attachment_file.txt"));
        expected.setId(randomStringId());

        when(userTaskServicesClient.getTaskAttachmentContentById(anyString(), anyLong(), anyLong()))
                .thenReturn(expected.getPayload());

        when(documentServicesClient.getDocument(any()))
                .thenReturn(document);

        // When
        File result = kieTaskAttachmentService.download(getDummyConnection(), null, taskId.toString(),
                TASK_ATTACHMENT_ID_1_1);

        // Then
        assertThat(result.getData()).isEqualTo(expected.getContent());
        assertThat(result.getName()).isEqualTo(expected.getFilename());

        verify(userTaskServicesClient)
                .getTaskAttachmentContentById(taskId.getContainerId(), taskId.getInstanceId(),
                        Long.valueOf(TASK_ATTACHMENT_ID_1_1));

        verify(documentServicesClient).getDocument(anyString());
    }

    @Test
    public void shouldThrowNotFoundWhenGetTask() {
        // Given
        KieInstanceId taskId = new KieInstanceId(CONTAINER_ID_1, TASK_ID_1);

        when(userTaskServicesClient.getTaskAttachmentById(any(), anyLong(), anyLong()))
                .thenThrow(new KieServicesHttpException(null, HttpStatus.NOT_FOUND.value(), null, null));

        // Then
        expectedException.expect(AttachmentNotFoundException.class);

        // When
        kieTaskAttachmentService.get(getDummyConnection(), null, taskId.toString(), TASK_ATTACHMENT_ID_1_1);
    }

    @Test
    public void shouldThrowNotFoundWhenDelete() {
        // Given
        KieInstanceId taskId = new KieInstanceId(CONTAINER_ID_1, TASK_ID_1);

        doThrow(new KieServicesHttpException(null, HttpStatus.NOT_FOUND.value(), null, null))
                .when(userTaskServicesClient).deleteTaskAttachment(any(), anyLong(), anyLong());

        // Then
        expectedException.expect(AttachmentNotFoundException.class);

        // When
        kieTaskAttachmentService.delete(getDummyConnection(), null, taskId.toString(), TASK_ATTACHMENT_ID_1_1);
    }

    @Test
    public void shouldThrowInvalidKieIdWithInstanceIdNotNumeric() {
        // Then
        expectedException.expect(KieInvalidIdException.class);

        // When
        kieTaskAttachmentService.list(null, null, "notnumeric@c1");
    }

    @Test
    public void shouldThrowInvalidKieIdWithInvalidSeparator() {
        // Then
        expectedException.expect(KieInvalidIdException.class);

        // When
        kieTaskAttachmentService.list(null, null, "1-c1");
    }
}
