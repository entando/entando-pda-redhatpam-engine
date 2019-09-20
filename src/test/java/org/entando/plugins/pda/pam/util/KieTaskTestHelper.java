package org.entando.plugins.pda.pam.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.entando.plugins.pda.pam.service.task.model.KieProcessVariable;
import org.entando.plugins.pda.pam.service.task.model.KieTask;

@UtilityClass
public class KieTaskTestHelper {

    public static final String TASK_ID_1 = "1";
    public static final String TASK_NAME_1 = "Task 1";
    public static final String TASK_ID_2 = "2";
    public static final String TASK_NAME_2 = "Task 2";
    public static final String EXTRA_VARS_ATTRIBUTE_1 = "attribute1";
    public static final String EXTRA_VARS_STRING = "String";
    public static final String EXTRA_VARS_VALUE_1 = "value attribute1";
    public static final String EXTRA_VARS_ATTRIBUTE_2 = "attribute2";
    public static final String EXTRA_VARS_INTEGER = "Integer";
    public static final String EXTRA_VARS_VALUE_2 = "2";
    public static final String EXTRA_VARS_ATTRIBUTE_3 = "attribute3";
    public static final String EXTRA_VARS_VALUE_3 = "value attribute3";
    public static final String EXTRA_VARS_ATTRIBUTE_4 = "attribute4";
    public static final String EXTRA_VARS_VALUE_4 = "4";

    public List<KieTask> createKieTaskList() {
        List<KieTask> result = new ArrayList<>();
        result.add(KieTask.builder()
                .taskId(Integer.valueOf(TASK_ID_1))
                .taskName(TASK_NAME_1)
                .taskProcInstId("1")
                .build());
        result.add(KieTask.builder()
                .taskId(Integer.valueOf(TASK_ID_2))
                .taskName(TASK_NAME_2)
                .taskProcInstId("2")
                .build());
        return result;
    }

    public List<KieProcessVariable> createKieProcessVariables(Long processInstanceId) {
        return Arrays.asList(
                KieProcessVariable.builder()
                        .name(EXTRA_VARS_ATTRIBUTE_1)
                        .value(EXTRA_VARS_VALUE_1)
                        .processInstanceId(processInstanceId)
                        .build(),
                KieProcessVariable.builder()
                        .name(EXTRA_VARS_ATTRIBUTE_2)
                        .value(EXTRA_VARS_VALUE_2)
                        .processInstanceId(processInstanceId)
                        .build()
        );
    }
}
