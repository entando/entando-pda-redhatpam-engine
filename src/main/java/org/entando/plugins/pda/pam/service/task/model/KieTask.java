package org.entando.plugins.pda.pam.service.task.model;

import static org.entando.plugins.pda.pam.service.task.util.TaskUtil.flatProperties;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.entando.plugins.pda.core.model.Task;
import org.entando.plugins.pda.pam.exception.KieInvalidTaskStatusException;
import org.entando.plugins.pda.pam.service.util.KieInstanceId;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummary;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "The warned bug is exposure on the Builder,"
        + "however after .build() the constructor is called and vulnerability is resolved")
public class KieTask extends Task {
    private Integer priority;
    private String subject;
    private String type;
    private String form;
    private Date activatedAt;
    private Boolean skipable;
    private Long workItemId;
    private Long processId;
    private String processDefinitionId;
    private Long parentId;
    private Integer slaCompliance;
    private Date slaDueTo;
    private List<String> potentialOwners;
    private List<String> excludedOwners;
    private List<String> businessAdmins;

    public static final String KIE_STATUS_CREATED = "Created";
    public static final String KIE_STATUS_READY = "Ready";
    public static final String KIE_STATUS_RESERVED = "Reserved";
    public static final String KIE_STATUS_IN_PROGRESS = "InProgress";
    public static final String KIE_STATUS_SUSPENDED = "Suspended";
    public static final String KIE_STATUS_COMPLETED = "Completed";
    public static final String KIE_STATUS_FAILED = "Failed";
    public static final String KIE_STATUS_ERROR = "Error";
    public static final String KIE_STATUS_OBSOLETE = "Obsolete";

    @Builder
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public KieTask(Long id, String containerId, String name, String description, Date createdAt, String createdBy,
            Date dueTo, String status, String owner, Integer priority, String subject, String type, String form,
            Date activatedAt, Boolean skipable, Long workItemId, Long processId,
            String processDefinitionId, Long parentId, Integer slaCompliance, Date slaDueTo,
            List<String> potentialOwners, List<String> excludedOwners, List<String> businessAdmins,
            Map<String, Object> inputData, Map<String, Object> outputData) {

        super(new KieInstanceId(containerId, id).toString(), name, description, createdBy, createdAt, dueTo,
                convertKieTaskStatus(status), owner, flatProperties(inputData), flatProperties(outputData));

        this.priority = priority;
        this.subject = subject;
        this.type = type;
        this.form = form;
        this.activatedAt = activatedAt == null ? null : new Date(activatedAt.getTime());
        this.skipable = skipable;
        this.workItemId = workItemId;
        this.processId = processId;
        this.processDefinitionId = processDefinitionId;
        this.parentId = parentId;
        this.slaCompliance = slaCompliance;
        this.slaDueTo = slaDueTo == null ? null : new Date(slaDueTo.getTime());
        this.potentialOwners = potentialOwners;
        this.excludedOwners = excludedOwners;
        this.businessAdmins = businessAdmins;
    }

    public static KieTask from(TaskInstance kieTask) {
        return builder()
                .id(kieTask.getId())
                .containerId(kieTask.getContainerId())
                .name(kieTask.getName())
                .description(kieTask.getDescription())
                .createdAt(kieTask.getCreatedOn())
                .createdBy(kieTask.getCreatedBy())
                .dueTo(kieTask.getExpirationDate())
                .status(kieTask.getStatus())
                .owner(kieTask.getActualOwner())
                .priority(kieTask.getPriority())
                .subject(kieTask.getSubject())
                .type(kieTask.getTaskType())
                .form(kieTask.getFormName())
                .activatedAt(kieTask.getActivationTime())
                .skipable(kieTask.getSkipable())
                .workItemId(kieTask.getWorkItemId())
                .processId(kieTask.getProcessInstanceId())
                .processDefinitionId(kieTask.getProcessId())
                .parentId(kieTask.getParentId())
                .slaDueTo(kieTask.getSlaDueDate())
                .slaCompliance(kieTask.getSlaCompliance())
                .potentialOwners(kieTask.getPotentialOwners())
                .excludedOwners(kieTask.getExcludedOwners())
                .businessAdmins(kieTask.getBusinessAdmins())
                .inputData(kieTask.getInputData())
                .outputData(kieTask.getOutputData())
                .build();
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

    public Date getSlaDueTo() {
        return slaDueTo == null ? null : new Date(slaDueTo.getTime());
    }

    public void setSlaDueTo(Date slaDueTo) {
        this.slaDueTo = slaDueTo == null ? null : new Date(slaDueTo.getTime());
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    private static Task.Status convertKieTaskStatus(String status) {
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
