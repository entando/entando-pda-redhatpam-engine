package org.entando.plugins.pda.pam.service.task.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KieTask {
    @JsonProperty("task-id")
    private int taskId;

    @JsonProperty("task-name")
    private String taskName;

    @JsonProperty("task-subject")
    private String taskSubject;

    @JsonProperty("task-description")
    private String taskDescription;

    @JsonProperty("task-status")
    private String taskStatus;

    @JsonProperty("task-priority")
    private Integer taskPriority;

    @JsonProperty("task-is-skipable")
    private Boolean taskIsSkipable;

    @JsonProperty("task-actual-owner")
    private String taskActualOwner;

    @JsonProperty("task-created-by")
    private String taskCreatedBy;

    @JsonProperty("task-proc-inst-id")
    private String taskProcInstId;

    @JsonProperty("task-proc-def-id")
    private String taskProcDefId;

    @JsonProperty("task-container-id")
    private String taskContainerId;

    @JsonProperty("task-parent-id")
    private String taskParentId;

}
