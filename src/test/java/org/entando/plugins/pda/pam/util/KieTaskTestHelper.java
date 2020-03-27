package org.entando.plugins.pda.pam.util;

import static org.entando.plugins.pda.core.utils.TestUtils.CONTAINER_ID_1;
import static org.entando.plugins.pda.core.utils.TestUtils.CONTAINER_ID_2;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_ATTACHMENT_CREATED_1_1;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_ATTACHMENT_CREATED_1_2;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_ATTACHMENT_CREATED_2_1;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_ATTACHMENT_CREATED_2_2;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_ATTACHMENT_ID_1_1;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_ATTACHMENT_ID_1_2;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_ATTACHMENT_ID_2_1;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_ATTACHMENT_NAME_1_1;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_ATTACHMENT_NAME_1_2;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_ATTACHMENT_NAME_2_1;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_ATTACHMENT_NAME_2_2;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_ATTACHMENT_OWNER_1_1;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_ATTACHMENT_OWNER_1_2;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_ATTACHMENT_OWNER_2_1;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_ATTACHMENT_OWNER_2_2;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_ATTACHMENT_SIZE_1_1;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_ATTACHMENT_SIZE_1_2;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_ATTACHMENT_SIZE_2_1;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_ATTACHMENT_SIZE_2_2;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_ID_1;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_ID_2;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_ID_3;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_NAME_1;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_NAME_2;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_NAME_3;
import static org.entando.plugins.pda.core.utils.TestUtils.randomLongId;
import static org.entando.plugins.pda.core.utils.TestUtils.readFromFile;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang.RandomStringUtils;
import org.entando.plugins.pda.core.model.Attachment;
import org.entando.plugins.pda.core.model.Comment;
import org.entando.plugins.pda.core.model.File;
import org.entando.plugins.pda.pam.service.task.model.KieTask;
import org.entando.plugins.pda.pam.service.util.KieInstanceId;
import org.kie.server.api.model.instance.DocumentInstance;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummary;

@UtilityClass
@SuppressWarnings({ "PMD.TooManyMethods", "PMD.ExcessiveImports" })
public class KieTaskTestHelper {

    public static final Long PROCESS_INSTANCE_ID_1 = 1L;
    public static final Long PROCESS_INSTANCE_ID_2 = 2L;

    public static final String EXTRA_VARS_ATTRIBUTE_1 = "attribute1";
    public static final String EXTRA_VARS_VALUE_1 = "value attribute1";

    public static final String EXTRA_VARS_ATTRIBUTE_2 = "attribute2";
    public static final String EXTRA_VARS_FLAT_ATTRIBUTE_2 = "attribute2.object.innerObject";
    public static final String EXTRA_VARS_VALUE_2 = "value attribute2";
    public static final Map<String, Object> EXTRA_VARS_COMPLEX_VALUE_2 = Collections.singletonMap("object",
            Collections.singletonMap("innerObject", EXTRA_VARS_VALUE_2));

    public static final String EXTRA_VARS_ATTRIBUTE_3 = "attribute3";
    public static final String EXTRA_VARS_FLAT_ATTRIBUTE_3 = "attribute3.object.otherObject.innerObject";
    public static final String EXTRA_VARS_VALUE_3 = "value attribute3";
    public static final Map<String, Object> EXTRA_VARS_COMPLEX_VALUE_3 = Collections.singletonMap("object",
            Collections.singletonMap("otherObject", Collections.singletonMap("innerObject", EXTRA_VARS_VALUE_3)));

    public static final String TASK_COMMENT_ID_1_1 = "1";
    public static final String TASK_COMMENT_1_1 = "This is a task comment!";
    public static final String TASK_COMMENT_ID_1_2 = "2";
    public static final String TASK_COMMENT_1_2 = "Whatever he said...";
    public static final String TASK_COMMENT_ID_2_1 = "3";
    public static final String TASK_COMMENT_2_1 = "This is another task comment!";
    public static final String TASK_COMMENT_ID_2_2 = "4";
    public static final String TASK_COMMENT_2_2 = "For sure!";
    public static final String TASK_COMMENT_OWNER_1 = "Chuck Norris";
    public static final String TASK_COMMENT_OWNER_2 = "Jack Bauer";

    public static final Set<String> TASK_DEFINITION_COLUMNS = Stream.of(
            "id", "name", "description", "createdBy", "createdAt", "dueTo", "status", "owner", "priority", "subject",
            "activatedAt", "skipable", "instanceId", "containerId", "processId", "processDefinitionId", "parentId")
            .collect(Collectors.toSet());

    public List<TaskSummary> createKieTaskList() {
        List<TaskSummary> result = new ArrayList<>();
        result.add(TaskSummary.builder()
                .id(Long.valueOf(TASK_ID_1))
                .name(TASK_NAME_1)
                .status(KieTask.KIE_STATUS_COMPLETED)
                .processInstanceId(PROCESS_INSTANCE_ID_1)
                .containerId(CONTAINER_ID_1)
                .build());
        result.add(TaskSummary.builder()
                .id(Long.valueOf(TASK_ID_2))
                .name(TASK_NAME_2)
                .status(KieTask.KIE_STATUS_RESERVED)
                .processInstanceId(PROCESS_INSTANCE_ID_2)
                .containerId(CONTAINER_ID_1)
                .build());
        result.add(TaskSummary.builder()
                .id(Long.valueOf(TASK_ID_3))
                .name(TASK_NAME_3)
                .status(KieTask.KIE_STATUS_IN_PROGRESS)
                .processInstanceId(PROCESS_INSTANCE_ID_2)
                .containerId(CONTAINER_ID_2)
                .build());
        return result;
    }

    public TaskInstance generateKieTask() {
        return generateKieTask(new KieInstanceId(
                RandomStringUtils.randomAlphabetic(10),
                Long.valueOf(RandomStringUtils.randomNumeric(10))));
    }

    public TaskInstance generateKieTask(KieInstanceId id) {
        return TaskInstance.builder()
                .id(id.getInstanceId())
                .containerId(id.getContainerId())
                .status(KieTask.KIE_STATUS_RESERVED)
                .name(RandomStringUtils.randomAlphabetic(20))
                .processInstanceId(Long.valueOf(RandomStringUtils.randomNumeric(10)))
                .inputData(ImmutableMap.of(EXTRA_VARS_ATTRIBUTE_1, EXTRA_VARS_VALUE_1,
                        EXTRA_VARS_ATTRIBUTE_2, EXTRA_VARS_COMPLEX_VALUE_2))
                .outputData(ImmutableMap.of(EXTRA_VARS_ATTRIBUTE_1, EXTRA_VARS_VALUE_1,
                        EXTRA_VARS_ATTRIBUTE_3, EXTRA_VARS_COMPLEX_VALUE_3))
                .build();
    }

    public List<Comment> createKieTaskComments(String taskId) {
        List<Comment> result = new ArrayList<>();

        if (TASK_ID_1.equals(taskId)) {
            result.add(Comment.builder()
                    .id(TASK_COMMENT_ID_1_1)
                    .text(TASK_COMMENT_1_1)
                    .createdBy(TASK_COMMENT_OWNER_1)
                    .createdAt(new Date())
                    .build());
            result.add(Comment.builder()
                    .id(TASK_COMMENT_ID_1_2)
                    .text(TASK_COMMENT_1_2)
                    .createdBy(TASK_COMMENT_OWNER_1)
                    .createdAt(new Date())
                    .build());
        } else if (TASK_ID_2.equals(taskId)){
            result.add(Comment.builder()
                    .id(TASK_COMMENT_ID_2_1)
                    .text(TASK_COMMENT_2_1)
                    .createdBy(TASK_COMMENT_OWNER_2)
                    .createdAt(new Date())
                    .build());
        }

        return result;
    }

    public Comment createKieTaskComment() {
        return Comment.builder()
                .id(TASK_COMMENT_ID_2_2)
                .text(TASK_COMMENT_2_2)
                .createdBy(TASK_COMMENT_OWNER_2)
                .createdAt(new Date())
                .build();
    }

    public List<Attachment> createKieTaskAttachments(String taskId) {
        List<Attachment> result = new ArrayList<>();

        if (TASK_ID_1.equals(taskId)) {
            result.add(Attachment.builder()
                    .id(TASK_ATTACHMENT_ID_1_1)
                    .name(TASK_ATTACHMENT_NAME_1_1)
                    .createdBy(TASK_ATTACHMENT_OWNER_1_1)
                    .createdAt(TASK_ATTACHMENT_CREATED_1_1)
                    .size(TASK_ATTACHMENT_SIZE_1_1)
                    .build());
            result.add(Attachment.builder()
                    .id(TASK_ATTACHMENT_ID_1_2)
                    .name(TASK_ATTACHMENT_NAME_1_2)
                    .createdBy(TASK_ATTACHMENT_OWNER_1_2)
                    .createdAt(TASK_ATTACHMENT_CREATED_1_2)
                    .size(TASK_ATTACHMENT_SIZE_1_2)
                    .build());
        } else if (TASK_ID_2.equals(taskId)){
            result.add(Attachment.builder()
                    .id(TASK_ATTACHMENT_ID_2_1)
                    .name(TASK_ATTACHMENT_NAME_2_1)
                    .createdBy(TASK_ATTACHMENT_OWNER_2_1)
                    .createdAt(TASK_ATTACHMENT_CREATED_2_1)
                    .size(TASK_ATTACHMENT_SIZE_2_1)
                    .build());
        }

        return result;
    }

    public Attachment createKieTaskAttachment() {
        return Attachment.builder()
                .id(randomLongId().toString())
                .name(TASK_ATTACHMENT_NAME_2_2)
                .createdBy(TASK_ATTACHMENT_OWNER_2_2)
                .createdAt(TASK_ATTACHMENT_CREATED_2_2)
                .size(TASK_ATTACHMENT_SIZE_2_2)
                .build();
    }

    public DocumentInstance createKieDocument() {
        File file = new File(readFromFile("task_attachment_file.txt"));

        return DocumentInstance.builder()
                .name(TASK_ATTACHMENT_NAME_2_2)
                .size(TASK_ATTACHMENT_SIZE_2_2)
                .lastModified(new Date())
                .id(UUID.randomUUID().toString())
                .content(file.getData())
                .build();
    }
}
