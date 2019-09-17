package org.entando.plugins.pda.pam.service.task.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KieProcessVariablesResponse {

    @JsonProperty("variable-instance")
    private List<KieProcessVariable> variables;

}

