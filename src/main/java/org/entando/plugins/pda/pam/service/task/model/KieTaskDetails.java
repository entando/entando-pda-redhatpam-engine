package org.entando.plugins.pda.pam.service.task.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.kie.server.api.model.instance.TaskInstance;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "The warned bug is exposure on the Builder,"
        + "however after .build() the constructor is called and vulnerability is resolved")
public class KieTaskDetails extends KieTask {
    private String type;
    private String form;
    private Long workItemId;
    private Integer slaCompliance;
    private Date slaDueTo;
    private List<String> potentialOwners;
    private List<String> excludedOwners;
    private List<String> businessAdmins;

    @Builder(builderMethodName = "detailsBuilder")
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public KieTaskDetails(Long id, String containerId, String name, String description, Date createdAt,
            String createdBy, Date dueTo, String status, String owner, Integer priority, String subject,
            Date activatedAt, Boolean skipable, Long processId, String processDefinitionId, Long parentId,
            Map<String, Object> inputData, Map<String, Object> outputData, String type, String form, Long workItemId,
            Integer slaCompliance, Date slaDueTo, List<String> potentialOwners, List<String> excludedOwners,
            List<String> businessAdmins) {

        super(id, containerId, name, description, createdBy, createdAt, dueTo, status, owner, priority, subject,
                activatedAt, skipable, processId, processDefinitionId, parentId, inputData, outputData);

        this.type = type;
        this.form = form;
        this.workItemId = workItemId;
        this.slaCompliance = slaCompliance;
        this.slaDueTo = slaDueTo == null ? null : new Date(slaDueTo.getTime());
        this.potentialOwners = potentialOwners;
        this.excludedOwners = excludedOwners;
        this.businessAdmins = businessAdmins;

        this.inputData = this.inputData == null ? new HashMap<>() : this.inputData;
        this.outputData = this.outputData == null ? new HashMap<>() : this.outputData;
    }

    public static KieTaskDetails from(TaskInstance kieTask) {
        return KieTaskDetails.detailsBuilder()
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

    public Date getSlaDueTo() {
        return slaDueTo == null ? null : new Date(slaDueTo.getTime());
    }

    public void setSlaDueTo(Date slaDueTo) {
        this.slaDueTo = slaDueTo == null ? null : new Date(slaDueTo.getTime());
    }

}
