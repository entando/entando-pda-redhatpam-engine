package org.entando.plugins.pda.pam.service.process.model;

import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import org.entando.plugins.pda.core.model.ProcessDefinition;

@Data
@NoArgsConstructor
public class KieProcessDefinition extends ProcessDefinition {

    @Builder
    public KieProcessDefinition(@Singular("extraProperty") Map<String,Object> extraProperties) {
        super(extraProperties);
    }

}
