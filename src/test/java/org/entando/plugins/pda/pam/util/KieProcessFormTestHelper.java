package org.entando.plugins.pda.pam.util;

import java.util.ArrayList;
import java.util.Collections;
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
@SuppressWarnings("PMD.ExcessiveMethodLength")
public class KieProcessFormTestHelper {

    public Form createProcessForm() {
        List<FormField> fields = new ArrayList<>();

        FormFieldSubForm fieldSubform1 = FormFieldSubForm.builder()
                .id("field_1786956128605089E11")
                .name("applicant")
                .label("Applicant")
                .type(FormFieldType.SUBFORM)
                .formId("applicant")
                .formType("com.myspace.mortgage_app.Applicant")
                .build();


        FormFieldSubForm fieldSubform2 = FormFieldSubForm.builder()
                .id("field_1811697043491191E12")
                .name("property")
                .label("Property")
                .type(FormFieldType.SUBFORM)
                .formId("property")
                .formType("com.myspace.mortgage_app.Property")
                .build();

        fields.add(fieldSubform1);
        fields.add(fieldSubform2);

        fields.add(FormFieldNumber.builder()
                .id("field_290268943445829E11")
                .name("downpayment")
                .label("Down Payment")
                .type(FormFieldType.INTEGER)
                .placeholder("Down Payment")
                .build()
        );

        fields.add(FormFieldNumber.builder()
                .id("field_0343033866589585E12")
                .name("amortization")
                .label("Years of amortization")
                .type(FormFieldType.INTEGER)
                .placeholder("Years of amortization")
                .build()
        );

        Form holder = Form.builder()
                .id("application")
                .type("com.myspace.mortgage_app.Application")
                .name("Application")
                .fields(fields)
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
                .id("process")
                .name("Mortgage_Process.MortgageApprovalProcess-taskform.frm")
                .fields(Collections.singletonList(
                        FormFieldSubForm.builder()
                                .id("field_0906698901603516E10")
                                .name("application")
                                .label("Application")
                                .type(FormFieldType.SUBFORM)
                                .formId("application")
                                .formType("com.myspace.mortgage_app.Application")
                                .form(holder)
                                .build()))
                .build();
    }
}
