package org.entando.plugins.pda.pam.service.task.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.entando.plugins.pda.pam.service.process.model.KieProcessDefinition;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KieProcessDefinitionsResponse {

    @JsonProperty("processes")
    private List<KieProcessDefinition> processes;
}

