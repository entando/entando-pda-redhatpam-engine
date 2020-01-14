package org.entando.plugins.pda.pam.service.task.model;

import static org.entando.plugins.pda.pam.service.task.util.TaskUtil.flatProperties;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.entando.plugins.pda.core.model.Task;
import org.entando.plugins.pda.pam.exception.KieInvalidTaskStatusException;
import org.entando.plugins.pda.pam.service.util.KieInstanceId;
import org.kie.server.api.model.instance.TaskSummary;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "The warned bug is exposure on the Builder,"
        + "however after .build() the constructor is called and vulnerability is resolved")
public class KieTask extends Task {
    private Integer priority;
    private String subject;
    private Date activatedAt;
    private Boolean skipable;
    private String processDefinitionId;
    private Long processId;
    private Long parentId;

    public static final String KIE_STATUS_CREATED = "Created";
    public static final String KIE_STATUS_READY = "Ready";
    public static final String KIE_STATUS_RESERVED = "Reserved";
    public static final String KIE_STATUS_IN_PROGRESS = "InProgress";
    public static final String KIE_STATUS_SUSPENDED = "Suspended";
    public static final String KIE_STATUS_COMPLETED = "Completed";
    public static final String KIE_STATUS_FAILED = "Failed";
    public static final String KIE_STATUS_ERROR = "Error";
    public static final String KIE_STATUS_OBSOLETE = "Obsolete";

    public static final Map<String, String> SORT_PROPERTIES = new ConcurrentHashMap<>();

    static {
        SORT_PROPERTIES.put("id", "t.id");
        SORT_PROPERTIES.put("name", "t.name");
        SORT_PROPERTIES.put("subject", "t.subject");
        SORT_PROPERTIES.put("description", "t.description");
        SORT_PROPERTIES.put("priority", "t.priority");
        SORT_PROPERTIES.put("status", "t.taskData.status");
        SORT_PROPERTIES.put("owner", "t.taskData.actualOwner.id");
        SORT_PROPERTIES.put("createdBy", "t.taskData.createdBy.id");
        SORT_PROPERTIES.put("createdAt", "t.taskData.createdOn");
        SORT_PROPERTIES.put("activatedAt", "t.taskData.activationTime");
        SORT_PROPERTIES.put("dueTo", "t.taskData.expirationTime");
        SORT_PROPERTIES.put("processDefinition", "t.taskData.processId");
        SORT_PROPERTIES.put("processId", "t.taskData.processInstanceId");
        SORT_PROPERTIES.put("parentId", "t.taskData.parentId");
        SORT_PROPERTIES.put("skipable", "t.taskData.skipable");
    }

    @Builder
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public KieTask(Long id, String containerId, String name, String description, String createdBy, Date createdAt,
            Date dueTo, String status, String owner, Integer priority, String subject, Date activatedAt,
            Boolean skipable, Long processId, String processDefinitionId, Long parentId,
            Map<String, Object> inputData, Map<String, Object> outputData) {

        super(new KieInstanceId(containerId, id).toString(), name, description, createdBy, createdAt, dueTo,
                convertKieTaskStatus(status), owner, flatProperties(inputData), flatProperties(outputData));

        this.priority = priority;
        this.subject = subject;
        this.activatedAt = activatedAt == null ? null : new Date(activatedAt.getTime());
        this.skipable = skipable;
        this.processId = processId;
        this.processDefinitionId = processDefinitionId;
        this.parentId = parentId;
    }

    public static KieTask from(TaskSummary kieTask) {
        return builder()
                .id(kieTask.getId())
                .containerId(kieTask.getContainerId())
                .name(kieTask.getName())
                .description(kieTask.getDescription())
                .createdAt(kieTask.getCreatedOn())
                .createdBy(kieTask.getCreatedBy())
                .dueTo(kieTask.getExpirationTime())
                .status(kieTask.getStatus())
                .owner(kieTask.getActualOwner())
                .priority(kieTask.getPriority())
                .subject(kieTask.getSubject())
                .activatedAt(kieTask.getActivationTime())
                .skipable(kieTask.getSkipable())
                .processId(kieTask.getProcessInstanceId())
                .processDefinitionId(kieTask.getProcessId())
                .parentId(kieTask.getParentId())
                .build();
    }

    public Date getActivatedAt() {
        return activatedAt == null ? null : new Date(activatedAt.getTime());
    }

    public void setActivatedAt(Date activatedAt) {
        this.activatedAt = activatedAt == null ? null : new Date(activatedAt.getTime());
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    private static Status convertKieTaskStatus(String status) {
        switch (status) {
            case KIE_STATUS_CREATED:
            case KIE_STATUS_READY:
                return Status.CREATED;
            case KIE_STATUS_RESERVED:
                return Status.RESERVED;
            case KIE_STATUS_IN_PROGRESS:
                return Status.IN_PROGRESS;
            case KIE_STATUS_SUSPENDED:
                return Status.PAUSED;
            case KIE_STATUS_COMPLETED:
                return Status.COMPLETED;
            case KIE_STATUS_FAILED:
            case KIE_STATUS_ERROR:
            case KIE_STATUS_OBSOLETE:
                return Status.FAILED;
            default:
                throw new KieInvalidTaskStatusException();
        }
    }

}
