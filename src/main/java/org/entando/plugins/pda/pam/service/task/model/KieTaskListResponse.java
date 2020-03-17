package org.entando.plugins.pda.pam.service.task.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KieTaskListResponse {

    @JsonProperty("task-summary")
    private List<KieTaskSummaryResponse> taskSummaries;
}
