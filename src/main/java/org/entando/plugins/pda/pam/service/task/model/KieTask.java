package org.entando.plugins.pda.pam.service.task.model;

import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import org.entando.plugins.pda.core.model.Task;
import org.entando.web.response.BaseMapModel;

@Data
@NoArgsConstructor
public class KieTask extends BaseMapModel implements Task {
    private static final String ID = "task-id";
    private static final String NAME = "task-name";
    private static final String PROCESS_ID = "task-proc-def-id";
    private static final String PROCESS_INSTANCE_ID_ALT1 = "task-proc-inst-id";
    private static final String PROCESS_INSTANCE_ID_ALT2 = "task-process-instance-id";
    private static final String CONTAINER_ID = "task-container-id";

    @Builder
    public KieTask(Integer id, String name, String processId, Integer processInstanceId, String containerId,
            @Singular("extraProperty") Map<String,Object> extraProperties) {
        super(extraProperties);

        this.data.put(ID, id);
        this.data.put(NAME, name);
        this.data.put(PROCESS_ID, processId);
        this.data.put(PROCESS_INSTANCE_ID_ALT1, processInstanceId);
        this.data.put(CONTAINER_ID, containerId);
    }

    @Override
    public Integer getId() {
        return (Integer) data.get(ID);
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
    public Integer getProcessInstanceId() {
        if (data.containsKey(PROCESS_INSTANCE_ID_ALT1)) {
            return (Integer) data.get(PROCESS_INSTANCE_ID_ALT1);
        } else {
            return (Integer) data.get(PROCESS_INSTANCE_ID_ALT2);
        }
    }

    @Override
    public String getContainerId() {
        return (String) data.get(CONTAINER_ID);
    }

}
