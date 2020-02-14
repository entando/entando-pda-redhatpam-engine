package org.entando.plugins.pda.pam.service.task.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.entando.plugins.pda.pam.service.task.KieDateDeserializer;
import org.kie.server.api.model.instance.TaskSummary;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
@SuppressWarnings("PMD.TooManyFields")
public class KieTaskSummaryResponse {

    @JsonProperty("task-id")
    private Long id;

    @JsonProperty("task-name")
    private String name;

    @JsonProperty("task-subject")
    private String subject;

    @JsonProperty("task-description")
    private String description;

    @JsonProperty("task-status")
    private String status;

    @JsonProperty("task-priority")
    private Integer priority;

    @JsonProperty("task-is-skipable")
    private Boolean skipable;

    @JsonProperty("task-actual-owner")
    private String actualOwner;

    @JsonProperty("task-created-by")
    private String createdBy;

    @JsonProperty("task-created-on")
    @JsonDeserialize(using = KieDateDeserializer.class)
    private Date createdOn;

    @JsonProperty("task-activation-time")
    @JsonDeserialize(using = KieDateDeserializer.class)
    private Date activationTime;

    @JsonProperty("task-expiration-time")
    @JsonDeserialize(using = KieDateDeserializer.class)
    private Date expirationTime;

    @JsonProperty("task-proc-inst-id")
    private Long processInstanceId;

    @JsonProperty("task-proc-def-id")
    private String processId;

    @JsonProperty("task-container-id")
    private String containerId;

    @JsonProperty("task-parent-id")
    private Long parentId;

    public TaskSummary toTaskSummary() {
        TaskSummary taskSummary = new TaskSummary();
        BeanUtils.copyProperties(this, taskSummary);
        return taskSummary;
    }
}
