package org.entando.plugins.pda.pam.service.task.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class KieTasksResponse {

    @JsonProperty("task-summary")
    private List<KieTask> tasks;
}

