package org.entando.plugins.pda.pam.service.task.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KieProcessVariable {
    private String name;

    @JsonProperty("old-value")
    private String oldValue;

    private String value;

    @JsonProperty("process-instance-id")
    private Long processInstanceId;

}
