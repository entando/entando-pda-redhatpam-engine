package org.entando.plugins.pda.pam.service.task.model;

import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import org.entando.plugins.pda.core.model.Task;

@Data
@NoArgsConstructor
public class KieTask extends Task {

    public static final String ID = "task-id";
    public static final String NAME = "task-name";
    public static final String PROCESS_ID = "task-proc-def-id";
    public static final String PROCESS_INSTANCE_ID_ALT1 = "task-proc-inst-id";
    public static final String PROCESS_INSTANCE_ID_ALT2 = "task-process-instance-id";
    public static final String CONTAINER_ID = "task-container-id";

    @Builder
    public KieTask(String id, String name, String processId, String processInstanceId, String containerId,
            @Singular("extraProperty") Map<String, Object> extraProperties) {
        super(extraProperties);

        this.data.put(ID, id);
        this.data.put(NAME, name);
        this.data.put(PROCESS_ID, processId);
        this.data.put(PROCESS_INSTANCE_ID_ALT1, processInstanceId);
        this.data.put(CONTAINER_ID, containerId);
    }

    @Override
    public String getId() {
        return data.get(ID).toString();
    }

    @Override
    public String getName() {
        return (String) data.get(NAME);
    }

    @Override
    public String getProcessId() {
        return (String) data.get(PROCESS_ID);
    }

    @Override
    public String getProcessInstanceId() {
        if (data.containsKey(PROCESS_INSTANCE_ID_ALT1)) {
            return data.get(PROCESS_INSTANCE_ID_ALT1).toString();
        } else {
            return data.get(PROCESS_INSTANCE_ID_ALT2).toString();
        }
    }

    @Override
    public String getContainerId() {
        return (String) data.get(CONTAINER_ID);
    }

}
