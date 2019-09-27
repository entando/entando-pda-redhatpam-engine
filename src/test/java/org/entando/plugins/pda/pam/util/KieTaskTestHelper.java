package org.entando.plugins.pda.pam.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.entando.plugins.pda.pam.service.task.model.KieProcessVariable;
import org.entando.plugins.pda.pam.service.task.model.KieTask;
import org.entando.plugins.pda.pam.service.task.model.KieTaskDetails;

@UtilityClass
public class KieTaskTestHelper {

    public static final Integer TASK_ID_1 = 1;
    public static final String TASK_NAME_1 = "Task 1";

    public static final Integer TASK_ID_2 = 2;
    public static final String TASK_NAME_2 = "Task 2";

    public static final Integer TASK_ID_3 = 3;
    public static final String TASK_NAME_3 = "Task 3";

    public static final Integer PROCESS_INSTANCE_ID_1 = 1;
    public static final Integer PROCESS_INSTANCE_ID_2 = 2;

    public static final String EXTRA_VARS_ATTRIBUTE_1 = "attribute1";
    public static final String EXTRA_VARS_VALUE_1 = "value attribute1";

    public static final String EXTRA_VARS_ATTRIBUTE_2 = "attribute2";
    public static final String EXTRA_VARS_VALUE_2 = "2";

    public static final String EXTRA_VARS_ATTRIBUTE_3 = "attribute3";
    public static final String EXTRA_VARS_VALUE_3 = "value attribute3";

    public static final String EXTRA_VARS_ATTRIBUTE_4 = "attribute4";
    public static final String EXTRA_VARS_VALUE_4 = "4";

    public List<KieTask> createKieTaskList() {
        List<KieTask> result = new ArrayList<>();
        result.add(KieTask.builder()
                .id(TASK_ID_1)
                .name(TASK_NAME_1)
                .processInstanceId(PROCESS_INSTANCE_ID_1)
                .build());
        result.add(KieTask.builder()
                .id(TASK_ID_2)
                .name(TASK_NAME_2)
                .processInstanceId(PROCESS_INSTANCE_ID_1)
                .build());
        result.add(KieTask.builder()
                .id(TASK_ID_3)
                .name(TASK_NAME_3)
                .processInstanceId(PROCESS_INSTANCE_ID_2)
                .build());
        return result;
    }

    public List<KieTask> createKieTaskListFull() {
        List<KieTask> result = new ArrayList<>();
        result.add(KieTask.builder()
                .id(TASK_ID_1)
                .name(TASK_NAME_1)
                .processInstanceId(PROCESS_INSTANCE_ID_1)
                .extraProperty(EXTRA_VARS_ATTRIBUTE_1, EXTRA_VARS_VALUE_1)
                .extraProperty(EXTRA_VARS_ATTRIBUTE_2, EXTRA_VARS_VALUE_2)
                .extraProperty(EXTRA_VARS_ATTRIBUTE_3, EXTRA_VARS_VALUE_3)
                .extraProperty(EXTRA_VARS_ATTRIBUTE_4, EXTRA_VARS_VALUE_4)
                .build());

        result.add(KieTask.builder()
                .id(TASK_ID_2)
                .name(TASK_NAME_2)
                .processInstanceId(PROCESS_INSTANCE_ID_1)
                .extraProperty(EXTRA_VARS_ATTRIBUTE_1, EXTRA_VARS_VALUE_1)
                .extraProperty(EXTRA_VARS_ATTRIBUTE_2, EXTRA_VARS_VALUE_2)
                .extraProperty(EXTRA_VARS_ATTRIBUTE_3, EXTRA_VARS_VALUE_3)
                .extraProperty(EXTRA_VARS_ATTRIBUTE_4, EXTRA_VARS_VALUE_4)
                .build());

        result.add(KieTask.builder()
                .id(TASK_ID_3)
                .name(TASK_NAME_3)
                .processInstanceId(PROCESS_INSTANCE_ID_2)
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
}
