package org.entando.plugins.pda.pam.util;

import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.entando.plugins.pda.core.model.form.Form;
import org.entando.plugins.pda.core.model.form.FormField;
import org.entando.plugins.pda.core.model.form.FormFieldNumber;
import org.entando.plugins.pda.core.model.form.FormFieldSubForm;
import org.entando.plugins.pda.core.model.form.FormFieldText;
import org.entando.plugins.pda.core.model.form.FormFieldType;

// CPD-OFF
@UtilityClass
public class KieTaskFormTestHelper {

    @SuppressWarnings("PMD.ExcessiveMethodLength")
    public Form createTaskForm() {

        List<FormField> holderFields = new ArrayList<>();

        holderFields.add(FormFieldNumber.builder()
                .id("field_4086")
                .name("mortgageamount")
                .label("Mortgage amount")
                .type(FormFieldType.INTEGER)
                .placeholder("Mortgage amount")
                .build()
        );

        holderFields.add(FormFieldNumber.builder()
                .id("field_3875")
                .name("downpayment")
                .label("Down Payment")
                .readOnly(true)
                .type(FormFieldType.INTEGER)
                .placeholder("Down Payment")
                .build()
        );

        holderFields.add(FormFieldNumber.builder()
                .id("field_1184")
                .name("amortization")
                .label("Years of amortization")
                .readOnly(true)
                .type(FormFieldType.INTEGER)
                .placeholder("Years of amortization")
                .build()
        );

        FormFieldSubForm fieldSubform1 = FormFieldSubForm.builder()
                .id("field_7992")
                .name("applicant")
                .label("Applicant")
                .readOnly(true)
                .type(FormFieldType.SUBFORM)
                .formId("applicant")
                .formType("com.myspace.mortgage_app.Applicant")
                .build();


        FormFieldSubForm fieldSubform2 = FormFieldSubForm.builder()
                .id("field_4885")
                .name("property")
                .label("Property")
                .readOnly(true)
                .type(FormFieldType.SUBFORM)
                .formId("property")
                .formType("com.myspace.mortgage_app.Property")
                .build();

        holderFields.add(fieldSubform1);
        holderFields.add(fieldSubform2);

        Form holder = Form.builder()
                .id("Application")
                .type("com.myspace.mortgage_app.Application")
                .name("ApplicationMortgage")
                .fields(holderFields)
                .build();

        List<FormField> fields1 = new ArrayList<>();

        fields1.add(FormFieldText.builder()
                .id("field_922175737010885E11")
                .name("name")
                .label("Name")
                .type(FormFieldType.STRING)
                .placeholder("Name")
                .maxLength(100)
                .build()
        );

        fields1.add(FormFieldNumber.builder()
                .id("field_405154649767496E12")
                .name("annualincome")
                .label("Annual Income")
                .type(FormFieldType.INTEGER)
                .placeholder("Annual Income")
                .build()
        );

        fields1.add(FormFieldNumber.builder()
                .id("field_670713100411637E11")
                .name("ssn")
                .label("SSN")
                .type(FormFieldType.INTEGER)
                .placeholder("SSN")
                .build()
        );

        List<FormField> fields2 = new ArrayList<>();

        fields2.add(FormFieldNumber.builder()
                .id("field_815717729253767E11")
                .name("age")
                .label("Age of property")
                .type(FormFieldType.INTEGER)
                .placeholder("Age of property")
                .build()
        );

        fields2.add(FormFieldText.builder()
                .id("field_236289653097941E11")
                .name("address")
                .label("Address of property")
                .type(FormFieldType.STRING)
                .placeholder("Address of property")
                .maxLength(100)
                .build()
        );

        fields2.add(FormFieldText.builder()
                .id("field_9471909295199063E11")
                .name("locale")
                .label("Locale")
                .type(FormFieldType.STRING)
                .placeholder("Locale")
                .maxLength(100)
                .build()
        );

        fields2.add(FormFieldNumber.builder()
                .id("field_4113393327260706E12")
                .name("saleprice")
                .label("Sale Price")
                .type(FormFieldType.INTEGER)
                .placeholder("Sale Price")
                .build()
        );

        Form subform1 = Form.builder()
                .id("applicant")
                .name("Applicant")
                .type("com.myspace.mortgage_app.Applicant")
                .fields(fields1)
                .build();

        Form subform2 = Form.builder()
                .id("property")
                .name("Property")
                .type("com.myspace.mortgage_app.Property")
                .fields(fields2)
                .build();

        fieldSubform1.setForm(subform1);
        fieldSubform2.setForm(subform2);

        return Form.builder()
                .id("task")
                .name("Qualify-taskform.frm")
                .field(FormFieldSubForm.builder()
                        .id("field_0627466111868674E11")
                        .name("application")
                        .label("Application")
                        .readOnly(true)
                        .type(FormFieldType.SUBFORM)
                        .formId("application")
                        .formType("com.myspace.mortgage_app.Application")
                        .form(holder)
                        .build())
                .field(FormField.builder()
                        .id("field_282450953523892E10")
                        .name("inlimit")
                        .label("Is mortgage application in limit?")
                        .type(FormFieldType.BOOLEAN)
                        .build())
                .build();
    }
}
