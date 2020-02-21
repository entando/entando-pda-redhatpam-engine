package org.entando.plugins.pda.pam.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.entando.plugins.pda.core.model.form.Form;
import org.entando.plugins.pda.core.model.form.FormField;
import org.entando.plugins.pda.core.model.form.FormFieldDate;
import org.entando.plugins.pda.core.model.form.FormFieldNumber;
import org.entando.plugins.pda.core.model.form.FormFieldSelector;
import org.entando.plugins.pda.core.model.form.FormFieldSelector.Option;
import org.entando.plugins.pda.core.model.form.FormFieldSubForm;
import org.entando.plugins.pda.core.model.form.FormFieldText;
import org.entando.plugins.pda.core.model.form.FormFieldType;

// CPD-OFF
@UtilityClass
@SuppressWarnings("PMD.ExcessiveMethodLength")
public class KieProcessFormTestHelper {

    public Form createMortgageProcessForm() {
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

    public Form createSampleProcessForm() {
        List<FormField> fieldsSubForm = new ArrayList<>();
        fieldsSubForm.add(FormFieldDate.builder()
                .id("field_3423371242937776E12")
                .name("myDateTime")
                .label("My Date Time ")
                .type(FormFieldType.DATE)
                .placeholder("My Date Time ")
                .withTime(true)
                .build()
        );

        fieldsSubForm.add(FormField.builder()
                .id("field_912757066857511E11")
                .name("myBoolean")
                .label("My Boolean")
                .type(FormFieldType.BOOLEAN)
                .build()
        );

        fieldsSubForm.add(FormFieldText.builder()
                .id("field_9383327275491315E11")
                .name("myString")
                .label("My String ")
                .type(FormFieldType.STRING)
                .required(true)
                .maxLength(100)
                .placeholder("My String")
                .build()
        );

        fieldsSubForm.add(FormFieldNumber.builder()
                .id("field_889617218948984E11")
                .name("myDouble")
                .label("My Double - Read Only")
                .type(FormFieldType.DOUBLE)
                .readOnly(true)
                .placeholder("My Double")
                .build()
        );

        fieldsSubForm.add(FormFieldNumber.builder()
                .id("field_5437")
                .name("__unbound_field_field_5437")
                .label("My Integer")
                .type(FormFieldType.INTEGER)
                .placeholder("My Integer")
                .build()
        );

        Form subform = Form.builder()
                .id("MyObject")
                .name("com_myspace_forms_sample_MyObject")
                .type("com.myspace.forms_sample.MyObject")
                .fields(fieldsSubForm)
                .build();

        List<FormField> fields = new ArrayList<>();

        FormFieldSubForm fieldSubform = FormFieldSubForm.builder()
                .id("field_2127768227611858E12")
                .name("myObject")
                .label("Nested Form of My Object")
                .type(FormFieldType.SUBFORM)
                .formId("myObject")
                .formType("com.myspace.forms_sample.MyObject")
                .build();

        fields.add(fieldSubform);

        fields.add(FormFieldSelector.builder()
                .id("field_9814")
                .name("__unbound_field_field_9814")
                .label("My Radio Group")
                .type(FormFieldType.RADIO)
                .option(Option.builder()
                        .value("myValue")
                        .label("myText")
                        .build())
                .option(Option.builder()
                        .value("anotherValue")
                        .label("anotherText")
                        .build())
                .option(Option.builder()
                        .value("what?")
                        .label("yup!")
                        .build())
                .build());

        fields.add(FormFieldNumber.builder()
                .id("field_9324")
                .name("__unbound_field_field_9324")
                .label("Slider")
                .type(FormFieldType.SLIDER)
                .minValue(0.0)
                .maxValue(200.0)
                .multipleOf(5.0)
                .build());

        fields.add(FormFieldNumber.builder()
                .id("field_2802")
                .name("__unbound_field_field_2802")
                .label("Slider 0to1")
                .type(FormFieldType.SLIDER)
                .minValue(0.0)
                .maxValue(1.0)
                .multipleOf(0.1)
                .build());

        fields.add(FormFieldText.builder()
                .id("field_6196")
                .name("__unbound_field_field_6196")
                .label("This is a big text")
                .type(FormFieldType.STRING)
                .placeholder("Write here!")
                .required(true)
                .rows(4)
                .build());

        fields.add(FormFieldSelector.builder()
                .id("field_4073")
                .name("__unbound_field_field_4073")
                .label("My Multiple Selector")
                .type(FormFieldType.MULTIPLE)
                .multiple(true)
                .option(Option.builder()
                        .value("first")
                        .label("first")
                        .build())
                .option(Option.builder()
                        .value("second")
                        .label("second")
                        .build())
                .option(Option.builder()
                        .value("third")
                        .label("third")
                        .build())
                .option(Option.builder()
                        .value("last")
                        .label("last")
                        .build())
                .build());

        fields.add(FormFieldSelector.builder()
                .id("field_653")
                .name("__unbound_field_field_653")
                .label("Combo without default")
                .type(FormFieldType.COMBO)
                .option(Option.builder()
                        .value("oneValue")
                        .label("one")
                        .build())
                .option(Option.builder()
                        .value("twoValue")
                        .label("two")
                        .build())
                .option(Option.builder()
                        .value("threeValue")
                        .label("three")
                        .build())
                .build());

        fields.add(FormFieldSelector.builder()
                .id("field_68448")
                .name("__unbound_field_field_68448")
                .label("Combo")
                .type(FormFieldType.COMBO)
                .defaultValue("myValue")
                .option(Option.builder()
                        .value("myValue")
                        .label("myText")
                        .build())
                .option(Option.builder()
                        .value("anotherValue")
                        .label("anotherText")
                        .build())
                .option(Option.builder()
                        .value("what?")
                        .label("yup!")
                        .build())
                .build());

        fields.add(FormField.builder()
                .id("field_72394")
                .name("processList")
                .label("ProcessList")
                .type(FormFieldType.INPUT_LIST)
                .build());

        fields.add(FormFieldSelector.builder()
                .id("field_030592")
                .name("__unbound_field_field_030592")
                .label("My Radio Group with default value")
                .type(FormFieldType.RADIO)
                .required(true)
                .defaultValue("thirdValue")
                .option(Option.builder()
                        .value("firstValue")
                        .label("first")
                        .build())
                .option(Option.builder()
                        .value("secondValue")
                        .label("second")
                        .build())
                .option(Option.builder()
                        .value("thirdValue")
                        .label("third")
                        .build())
                .build());

        fieldSubform.setForm(subform);

        return Form.builder()
                .id("process")
                .name("forms-sample.ProcessSample-taskform.frm")
                .fields(fields)
                .build();
    }
}
