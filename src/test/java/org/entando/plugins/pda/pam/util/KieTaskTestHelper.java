package org.entando.plugins.pda.pam.util;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang.RandomStringUtils;
import org.entando.plugins.pda.core.model.Comment;
import org.entando.plugins.pda.pam.service.task.model.KieProcessVariable;
import org.entando.plugins.pda.pam.service.task.model.KieProcessVariablesResponse;
import org.entando.plugins.pda.pam.service.task.model.KieTask;
import org.entando.plugins.pda.pam.service.task.model.KieTaskDetails;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;

@UtilityClass
public class KieTaskTestHelper {

    public static final String CONTAINER_ID_1 = "c1";
    public static final String CONTAINER_ID_2 = "c2";

    public static final String TASK_ID_1 = "1";
    public static final String TASK_NAME_1 = "Task 1";

    public static final String TASK_ID_2 = "2";
    public static final String TASK_NAME_2 = "Task 2";

    public static final String TASK_ID_3 = "3";
    public static final String TASK_NAME_3 = "Task 3";

    public static final String PROCESS_INSTANCE_ID_1 = "1";
    public static final String PROCESS_INSTANCE_ID_2 = "2";

    public static final String EXTRA_VARS_ATTRIBUTE_1 = "attribute1";
    public static final String EXTRA_VARS_VALUE_1 = "value attribute1";

    public static final String EXTRA_VARS_ATTRIBUTE_2 = "attribute2";
    public static final String EXTRA_VARS_VALUE_2 = "2";

    public static final String EXTRA_VARS_ATTRIBUTE_3 = "attribute3";
    public static final String EXTRA_VARS_VALUE_3 = "value attribute3";

    public static final String EXTRA_VARS_ATTRIBUTE_4 = "attribute4";
    public static final String EXTRA_VARS_VALUE_4 = "4";

    public static final String FIELD_1 = "field1";
    public static final String FIELD_2 = "field2";

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

    public List<KieTask> createKieTaskList() {
        List<KieTask> result = new ArrayList<>();
        result.add(KieTask.builder()
                .id(TASK_ID_1)
                .name(TASK_NAME_1)
                .processInstanceId(PROCESS_INSTANCE_ID_1)
                .containerId(CONTAINER_ID_1)
                .build());
        result.add(KieTask.builder()
                .id(TASK_ID_2)
                .name(TASK_NAME_2)
                .processInstanceId(PROCESS_INSTANCE_ID_1)
                .containerId(CONTAINER_ID_1)
                .build());
        result.add(KieTask.builder()
                .id(TASK_ID_3)
                .name(TASK_NAME_3)
                .processInstanceId(PROCESS_INSTANCE_ID_2)
                .containerId(CONTAINER_ID_2)
                .build());
        return result;
    }

    public List<KieTask> createKieTaskListWithEmbeddedData() {
        List<KieTask> result = new ArrayList<>();
        result.add(KieTask.builder()
                .id(TASK_ID_1)
                .name(TASK_NAME_1)
                .processInstanceId(PROCESS_INSTANCE_ID_1)
                .containerId(CONTAINER_ID_1)
                .extraProperty(FIELD_1,
                        ImmutableMap.of("com.organization.mycustomtype", ImmutableMap.of(FIELD_2, "value")))
                .build());
        result.add(KieTask.builder()
                .id(TASK_ID_2)
                .name(TASK_NAME_2)
                .processInstanceId(PROCESS_INSTANCE_ID_1)
                .containerId(CONTAINER_ID_1)
                .extraProperty(FIELD_1,
                        ImmutableMap.of("com.organization.mycustomtype", ImmutableMap.of(FIELD_2, "value")))
                .build());
        return result;
    }

    public List<KieTask> createKieTaskListFull() {
        List<KieTask> result = new ArrayList<>();
        result.add(KieTask.builder()
                .id(TASK_ID_1)
                .name(TASK_NAME_1)
                .processInstanceId(PROCESS_INSTANCE_ID_1)
                .containerId(CONTAINER_ID_1)
                .extraProperty(EXTRA_VARS_ATTRIBUTE_1, EXTRA_VARS_VALUE_1)
                .extraProperty(EXTRA_VARS_ATTRIBUTE_2, EXTRA_VARS_VALUE_2)
                .extraProperty(EXTRA_VARS_ATTRIBUTE_3, EXTRA_VARS_VALUE_3)
                .extraProperty(EXTRA_VARS_ATTRIBUTE_4, EXTRA_VARS_VALUE_4)
                .build());

        result.add(KieTask.builder()
                .id(TASK_ID_2)
                .name(TASK_NAME_2)
                .processInstanceId(PROCESS_INSTANCE_ID_1)
                .containerId(CONTAINER_ID_1)
                .extraProperty(EXTRA_VARS_ATTRIBUTE_1, EXTRA_VARS_VALUE_1)
                .extraProperty(EXTRA_VARS_ATTRIBUTE_2, EXTRA_VARS_VALUE_2)
                .extraProperty(EXTRA_VARS_ATTRIBUTE_3, EXTRA_VARS_VALUE_3)
                .extraProperty(EXTRA_VARS_ATTRIBUTE_4, EXTRA_VARS_VALUE_4)
                .build());

        result.add(KieTask.builder()
                .id(TASK_ID_3)
                .name(TASK_NAME_3)
                .processInstanceId(PROCESS_INSTANCE_ID_2)
                .containerId(CONTAINER_ID_2)
                .extraProperty(EXTRA_VARS_ATTRIBUTE_1, EXTRA_VARS_VALUE_1)
                .extraProperty(EXTRA_VARS_ATTRIBUTE_2, EXTRA_VARS_VALUE_2)
                .extraProperty(EXTRA_VARS_ATTRIBUTE_3, EXTRA_VARS_VALUE_3)
                .extraProperty(EXTRA_VARS_ATTRIBUTE_4, EXTRA_VARS_VALUE_4)
                .build());

        return result;
    }

    public List<KieTask> createKieTaskListUser() {
        List<KieTask> result = new ArrayList<>();
        result.add(KieTask.builder()
                .id(TASK_ID_1)
                .name(TASK_NAME_1)
                .processInstanceId(PROCESS_INSTANCE_ID_1)
                .containerId(CONTAINER_ID_1)
                .extraProperty(EXTRA_VARS_ATTRIBUTE_1, EXTRA_VARS_VALUE_1)
                .extraProperty(EXTRA_VARS_ATTRIBUTE_2, EXTRA_VARS_VALUE_2)
                .extraProperty(EXTRA_VARS_ATTRIBUTE_3, EXTRA_VARS_VALUE_3)
                .extraProperty(EXTRA_VARS_ATTRIBUTE_4, EXTRA_VARS_VALUE_4)
                .build());

        return result;
    }

    public List<KieProcessVariable> createKieProcessVariables() {
        return Arrays.asList(
                KieProcessVariable.builder()
                        .name(EXTRA_VARS_ATTRIBUTE_1)
                        .value(EXTRA_VARS_VALUE_1)
                        .build(),
                KieProcessVariable.builder()
                        .name(EXTRA_VARS_ATTRIBUTE_2)
                        .value(EXTRA_VARS_VALUE_2)
                        .build()
        );
    }

    public KieTaskDetails createKieTaskDetails() {
        return KieTaskDetails.builder()
                .extraProperty(EXTRA_VARS_ATTRIBUTE_3, EXTRA_VARS_VALUE_3)
                .extraProperty(EXTRA_VARS_ATTRIBUTE_4, EXTRA_VARS_VALUE_4)
                .build();
    }

    public KieTask generateKieTask() {
        return KieTask.builder()
                .id(RandomStringUtils.randomNumeric(10))
                .name(RandomStringUtils.randomAlphabetic(20))
                .processInstanceId(RandomStringUtils.randomNumeric(10))
                .containerId(RandomStringUtils.randomNumeric(10))
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

    public static void mockVariablesRequest(MockRestServiceServer mockServer, ObjectMapper mapper,
            ExpectedCount count) throws JsonProcessingException {
        mockServer.expect(count, requestTo(containsString("/variables/instances")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(
                        new KieProcessVariablesResponse(KieTaskTestHelper.createKieProcessVariables())),
                        MediaType.APPLICATION_JSON));
    }

    public static void mockTasksRequest(MockRestServiceServer mockServer, ObjectMapper mapper,
            ExpectedCount count) throws JsonProcessingException {
        mockServer.expect(count, requestTo(containsString("/tasks")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(KieTaskTestHelper.createKieTaskDetails()),
                        MediaType.APPLICATION_JSON));
    }
}
