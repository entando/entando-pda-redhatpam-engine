package org.entando.plugins.pda.pam.service.task.model;

import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import org.entando.web.response.BaseMapModel;

@Data
@NoArgsConstructor
public class KieTaskDetails extends BaseMapModel {

    @Builder
    public KieTaskDetails(@Singular("extraProperty") Map<String,Object> extraProperty) {
        super(extraProperty);
    }
}
