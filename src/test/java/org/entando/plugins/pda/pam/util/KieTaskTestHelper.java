package org.entando.plugins.pda.pam.util;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang.RandomStringUtils;
import org.entando.plugins.pda.core.model.Comment;
import org.entando.plugins.pda.pam.service.task.model.KieTask;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummary;

@UtilityClass
@SuppressWarnings("PMD.TooManyMethods")
public class KieTaskTestHelper {

    public static final String CONTAINER_ID_1 = "c1";
    public static final String CONTAINER_ID_2 = "c2";

    public static final Long TASK_ID_1 = 1L;
    public static final String TASK_NAME_1 = "Task 1";

    public static final Long TASK_ID_2 = 2L;
    public static final String TASK_NAME_2 = "Task 2";

    public static final Long TASK_ID_3 = 3L;
    public static final String TASK_NAME_3 = "Task 3";

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
            "type", "form", "activatedAt", "skipable", "workItemId", "processId", "slaCompliance", "slaDueTo",
            "potentialOwners", "businessAdmins")
            .collect(Collectors.toSet());

    public List<TaskSummary> createKieTaskList() {
        List<TaskSummary> result = new ArrayList<>();
        result.add(TaskSummary.builder()
                .id(TASK_ID_1)
                .name(TASK_NAME_1)
                .status(KieTask.KIE_STATUS_COMPLETED)
                .processInstanceId(PROCESS_INSTANCE_ID_1)
                .containerId(CONTAINER_ID_1)
                .build());
        result.add(TaskSummary.builder()
                .id(TASK_ID_2)
                .name(TASK_NAME_2)
                .status(KieTask.KIE_STATUS_RESERVED)
                .processInstanceId(PROCESS_INSTANCE_ID_2)
                .containerId(CONTAINER_ID_1)
                .build());
        result.add(TaskSummary.builder()
                .id(TASK_ID_3)
                .name(TASK_NAME_3)
                .status(KieTask.KIE_STATUS_IN_PROGRESS)
                .processInstanceId(PROCESS_INSTANCE_ID_2)
                .containerId(CONTAINER_ID_2)
                .build());
        return result;
    }

    public List<TaskSummary> createKieTaskListUser() {
        List<TaskSummary> result = new ArrayList<>();
        result.add(TaskSummary.builder()
                .id(TASK_ID_2)
                .name(TASK_NAME_2)
                .status(KieTask.KIE_STATUS_RESERVED)
                .processInstanceId(PROCESS_INSTANCE_ID_2)
                .containerId(CONTAINER_ID_1)
                .build());

        return result;
    }

    public TaskInstance generateKieTask() {
        return TaskInstance.builder()
                .id(Long.valueOf(RandomStringUtils.randomNumeric(10)))
                .containerId(RandomStringUtils.randomAlphabetic(10))
                .status(KieTask.KIE_STATUS_RESERVED)
                .name(RandomStringUtils.randomAlphabetic(20))
                .processInstanceId(Long.valueOf(RandomStringUtils.randomNumeric(10)))
                .containerId(RandomStringUtils.randomNumeric(10))
                .inputData(ImmutableMap.of(EXTRA_VARS_ATTRIBUTE_1, EXTRA_VARS_VALUE_1,
                        EXTRA_VARS_ATTRIBUTE_2, EXTRA_VARS_COMPLEX_VALUE_2))
                .outputData(ImmutableMap.of(EXTRA_VARS_ATTRIBUTE_1, EXTRA_VARS_VALUE_1,
                        EXTRA_VARS_ATTRIBUTE_3, EXTRA_VARS_COMPLEX_VALUE_3))
                .build();
    }

    public List<Comment> createKieTaskComments(Long taskId) {
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
}
