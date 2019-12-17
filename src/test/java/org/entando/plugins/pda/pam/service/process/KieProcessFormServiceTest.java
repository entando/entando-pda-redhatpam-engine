package org.entando.plugins.pda.pam.service.process;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.drools.core.io.impl.ClassPathResource;
import org.entando.plugins.pda.core.model.form.Form;
import org.entando.plugins.pda.core.model.form.FormField;
import org.entando.plugins.pda.core.model.form.FormFieldInteger;
import org.entando.plugins.pda.core.model.form.FormFieldText;
import org.entando.plugins.pda.core.model.form.FormFieldType;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("PMD.ExcessiveMethodLength")
public class KieProcessFormServiceTest {

    @Test
    public void deserializeProcessFormJson() {

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Form.class, new KieFormDeserializer());
        mapper.registerModule(module);

        List<Form> result = new ArrayList<>();

        String json = new String(new ClassPathResource("process-form.json").getBytes());

        try {
            JsonNode parentNode = mapper.readTree(json);

            for (JsonNode childNode : parentNode) {

                Form form  = mapper.treeToValue(childNode, Form.class);

                if (form.getFields().size() > 0) {
                    result.add(mapper.treeToValue(childNode, Form.class));
                }
            }

            assertThat(result.get(0)).isEqualTo(getExpectedListForm().get(0));

        } catch (IOException e) {
            Assert.fail();
        }
    }

    private List<Form> getExpectedListForm() {

        List<Form> result = new ArrayList<>();

        List<FormField> fields1 = new ArrayList<>();

        fields1.add(FormFieldInteger.builder()
                .id("field_815717729253767E11")
                .name("age")
                .label("Age of property")
                .required(false)
                .readOnly(false)
                .type(FormFieldType.INTEGER)
                .placeholder("Age of property")
                .build()
        );

        fields1.add(FormFieldText.builder()
                .id("field_236289653097941E11")
                .name("address")
                .label("Address of property")
                .required(false)
                .readOnly(false)
                .type(FormFieldType.STRING)
                .placeholder("Address of property")
                .maxLength(100)
                .build()
        );

        fields1.add(FormFieldText.builder()
                .id("field_9471909295199063E11")
                .name("locale")
                .label("Locale")
                .required(false)
                .readOnly(false)
                .type(FormFieldType.STRING)
                .placeholder("Locale")
                .maxLength(100)
                .build()
        );

        fields1.add(FormFieldInteger.builder()
                .id("field_4113393327260706E12")
                .name("saleprice")
                .label("Sale Price")
                .required(false)
                .readOnly(false)
                .type(FormFieldType.INTEGER)
                .placeholder("Sale Price")
                .build()
        );

        result.add(Form.builder()
                .id("2aeaf281-71e1-45a5-9ab3-0abd855d924e")
                .name("Property")
                .fields(fields1)
                .build());

        List<FormField> fields2 = new ArrayList<>();

        fields2.add(FormFieldInteger.builder()
                .id("field_290268943445829E11")
                .name("downpayment")
                .label("Down Payment")
                .required(false)
                .readOnly(false)
                .type(FormFieldType.INTEGER)
                .placeholder("Down Payment")
                .build()
        );

        fields2.add(FormFieldInteger.builder()
                .id("field_0343033866589585E12")
                .name("amortization")
                .label("Years of amortization")
                .required(false)
                .readOnly(false)
                .type(FormFieldType.INTEGER)
                .placeholder("Years of amortization")
                .build()
        );

        result.add(Form.builder()
                .id("b71de860-4d3e-4b0c-95e9-c41e4d06f787")
                .name("Application")
                .fields(fields2)
                .build());

        List<FormField> fields3 = new ArrayList<>();

        fields3.add(FormFieldText.builder()
                .id("field_922175737010885E11")
                .name("name")
                .label("Name")
                .required(false)
                .readOnly(false)
                .type(FormFieldType.STRING)
                .placeholder("Name")
                .maxLength(100)
                .build()
        );

        fields3.add(FormFieldInteger.builder()
                .id("field_405154649767496E12")
                .name("annualincome")
                .label("Annual Income")
                .required(false)
                .readOnly(false)
                .type(FormFieldType.INTEGER)
                .placeholder("Annual Income")
                .build()
        );

        fields3.add(FormFieldInteger.builder()
                .id("field_670713100411637E11")
                .name("ssn")
                .label("SSN")
                .required(false)
                .readOnly(false)
                .type(FormFieldType.INTEGER)
                .placeholder("SSN")
                .build()
        );

        result.add(Form.builder()
                .id("0cb94115-b991-4dbe-a342-00d99a1cdd2d")
                .name("Applicant")
                .fields(fields3)
                .build());

        return result;
    }

}
