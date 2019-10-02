package org.entando.plugins.pda.pam.service.task.model;

import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import org.entando.web.response.BaseMapModel;

@Data
@NoArgsConstructor
public class KieProcessVariable extends BaseMapModel {

    @Builder
    public KieProcessVariable(String name, String value,
            @Singular("extraProperty") Map<String,Object> extraProperties) {
        super(extraProperties);

        data.put(name, value);
    }

    public String getName() {
        return (String) data.get("name");
    }

    public String getValue() {
        return (String) data.get("value");
    }

}
