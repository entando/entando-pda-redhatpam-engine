package org.entando.plugins.pda.pam.util;

import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.entando.plugins.pda.core.model.form.Form;
import org.entando.plugins.pda.core.model.form.FormField;
import org.entando.plugins.pda.core.model.form.FormFieldInteger;
import org.entando.plugins.pda.core.model.form.FormFieldText;
import org.entando.plugins.pda.core.model.form.FormFieldType;

@UtilityClass
public class KieTaskFormTestHelper {

    public List<Form> createTaskForms() {

        List<Form> result = new ArrayList<>();

        List<FormField> fields1 = new ArrayList<>();

        fields1.add(FormFieldText.builder()
                .id("field_332058348325587E12")
                .name("reason")
                .label("Reason")
                .required(false)
                .readOnly(true)
                .type(FormFieldType.STRING)
                .placeholder("Reason")
                .build()
        );

        fields1.add(FormFieldInteger.builder()
                .id("field_336003622256354E12")
                .name("performance")
                .label("Performance")
                .required(true)
                .readOnly(false)
                .type(FormFieldType.INTEGER)
                .placeholder("Performance")
                .build()
        );

        result.add(Form.builder()
                .id("47078d21-7da5-4d3f-8355-0fcd78b09f39")
                .name("PerformanceEvaluation-taskform.frm")
                .fields(fields1)
                .build());

        return result;
    }
}
