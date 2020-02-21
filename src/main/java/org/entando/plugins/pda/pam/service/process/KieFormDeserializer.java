package org.entando.plugins.pda.pam.service.process;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import org.entando.plugins.pda.core.model.form.Form;
import org.entando.plugins.pda.core.model.form.FormField;
import org.entando.plugins.pda.core.model.form.FormFieldDate;
import org.entando.plugins.pda.core.model.form.FormFieldNumber;
import org.entando.plugins.pda.core.model.form.FormFieldSelector;
import org.entando.plugins.pda.core.model.form.FormFieldSelector.Option;
import org.entando.plugins.pda.core.model.form.FormFieldSubForm;
import org.entando.plugins.pda.core.model.form.FormFieldText;
import org.entando.plugins.pda.core.model.form.FormFieldType;

@Slf4j
public class KieFormDeserializer extends StdDeserializer<Form> {

    private static final String INTEGER_TYPE = "IntegerBox";
    private static final String DOUBLE_TYPE = "DecimalBox";
    private static final String STRING_TYPE = "TextBox";
    private static final String TEXT_TYPE = "TextArea";
    private static final String BOOLEAN_TYPE = "CheckBox";
    private static final String DATE_TYPE = "DatePicker";
    private static final String SLIDER_TYPE = "Slider";
    private static final String RADIO_TYPE = "RadioGroup";
    private static final String MULTIPLE_SELECTOR_TYPE = "MultipleSelector";
    private static final String COMBO_TYPE = "ListBox";
    private static final String INPUT_LIST_TYPE = "MultipleInput";
    private static final String SUBFORM_TYPE = "SubForm";

    private static final String MAX_LENGTH = "maxLength";
    private static final String MIN_LENGTH = "minLength";
    private static final String ROWS = "rows";
    private static final String MAX_VALUE = "maxValue";
    private static final String MIN_VALUE = "minValue";
    private static final String MAX = "max";
    private static final String MIN = "min";
    private static final String STEP = "step";
    private static final String SHOW_TIME = "showTime";
    private static final String OPTIONS = "options";
    private static final String DEFAULT_VALUE = "defaultValue";
    private static final String CODE = "code";
    private static final String VALUE = "value";
    private static final String TEXT = "text";
    private static final String LIST_OF_VALUES = "listOfValues";
    private static final String BINDING = "binding";
    private static final String STANDALONE_CLASS_NAME = "standaloneClassName";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String LABEL = "label";
    private static final String REQUIRED = "required";
    private static final String READ_ONLY = "readOnly";
    private static final String PLACE_HOLDER = "placeHolder";
    private static final String FIELDS = "fields";

    public KieFormDeserializer() {
        this(null);
    }

    public KieFormDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Form deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {

        List<Form> result = new ArrayList<>();
        Object json = jsonParser.getCodec().readTree(jsonParser);
        if (json instanceof ArrayNode) {
            for (JsonNode form : (ArrayNode) json) {
                result.add(deserialize(form));
            }
        } else {
            result.add(deserialize((JsonNode) json));
        }

        //Process all Sub Form Fields
        List<Form> processedSubForms = new ArrayList<>();
        result.forEach(form -> {
            form.getFields().stream()
                    .filter(field -> field.getType().equals(FormFieldType.SUBFORM))
                    .forEach(field -> {
                        FormFieldSubForm subFormField = (FormFieldSubForm) field;

                        Form subForm = getChildForm(result, subFormField.getFormType());
                        if (subForm == null) {
                            log.warn("Sub Form not found while processing a sub form field: {}",
                                    subFormField.getFormType());
                            return;
                        }

                        subFormField.setForm(subForm);
                        processedSubForms.add(subForm);
                    });
        });

        result.removeAll(processedSubForms);

        if (result.size() > 1 && log.isWarnEnabled()) { //NOPMD
            log.warn("Ignoring {} orphan forms while deserializing a KIE Form", result.size() - 1);
        }

        return result.get(0);
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    private Form deserialize(JsonNode json) {
        JsonNode fieldsNode = json.get(FIELDS);

        List<FormField> fields = new ArrayList<>();

        for (JsonNode field : fieldsNode) {
            try {
                FormFieldType type = getType(field);

                FormField.FormFieldBuilder fieldBuilder;

                if (FormFieldType.STRING == type) {
                    fieldBuilder = FormFieldText.builder()
                            .maxLength(getInteger(field, MAX_LENGTH))
                            .minLength(getInteger(field, MIN_LENGTH))
                            .rows(getInteger(field, ROWS));
                } else if (FormFieldType.INTEGER == type || FormFieldType.DOUBLE == type) {
                    fieldBuilder = FormFieldNumber.builder()
                            .maxValue(getDouble(field, MAX_VALUE))
                            .minValue(getDouble(field, MIN_VALUE));
                } else if (FormFieldType.SLIDER == type) {
                    fieldBuilder = FormFieldNumber.builder()
                            .maxValue(getDouble(field, MAX))
                            .minValue(getDouble(field, MIN))
                            .multipleOf(getDouble(field, STEP));
                } else if (FormFieldType.DATE == type) {
                    fieldBuilder = FormFieldDate.builder()
                            .withTime(getBoolean(field, SHOW_TIME));
                } else if (FormFieldType.RADIO == type || FormFieldType.COMBO == type) {
                    Iterable<JsonNode> iterable = () -> getArrayNode(field, OPTIONS).elements();

                    fieldBuilder = FormFieldSelector.builder()
                            .defaultValue(getString(field, DEFAULT_VALUE))
                            .multiple(MULTIPLE_SELECTOR_TYPE.equals(getString(field, CODE)))
                            .options(StreamSupport.stream(iterable.spliterator(), false)
                                    .map(node -> Option.builder()
                                            .value(getString(node, VALUE))
                                            .label(getString(node, TEXT))
                                            .build())
                                    .collect(Collectors.toList()));
                } else if (FormFieldType.MULTIPLE == type) {
                    Iterable<JsonNode> iterable = () -> getArrayNode(field, LIST_OF_VALUES).elements();

                    fieldBuilder = FormFieldSelector.builder()
                            .multiple(MULTIPLE_SELECTOR_TYPE.equals(getString(field, CODE)))
                            .options(StreamSupport.stream(iterable.spliterator(), false)
                                    .map(node -> Option.builder()
                                            .value(node.asText())
                                            .label(node.asText())
                                            .build())
                                    .collect(Collectors.toList()));
                } else if (FormFieldType.SUBFORM == type) {
                    fieldBuilder = FormFieldSubForm.builder()
                            .formId(getString(field, BINDING))
                            .formType(getString(field, STANDALONE_CLASS_NAME));
                } else {
                    fieldBuilder = FormField.builder();
                }

                fields.add(
                        fieldBuilder
                                .id(getString(field, ID))
                                .type(type)
                                .name(getString(field, NAME))
                                .label(getString(field, LABEL))
                                .required(getBoolean(field, REQUIRED))
                                .readOnly(getBoolean(field, READ_ONLY))
                                .placeholder(getString(field, PLACE_HOLDER))
                                .build());
            } catch (IllegalArgumentException e) {
                log.error("Invalid field Type: {}", field.get(CODE).asText(), e);
            }
        }

        return Form.builder()
                .id(getString(json,"model.name"))
                .name(getString(json, "name"))
                .type(getString(json, "model.className"))
                .fields(fields)
                .build();
    }

    private Form getChildForm(List<Form> forms, String type) {
        for (Form form : forms) {
            if (type.equals(form.getType())) {
                return form;
            }
        }

        return null;
    }

    private JsonNode getNode(JsonNode node, String label) {
        if (label == null) {
            return null;
        }

        JsonNode fetched = node;
        for (String key : label.split("\\.")) {
            fetched = fetched.get(key);

            if (fetched == null) {
                return null;
            }
        }

        return fetched;
    }

    private ArrayNode getArrayNode(JsonNode node, String label) {
        return (ArrayNode) getNode(node, label);
    }

    private String getString(JsonNode node, String label) {
        JsonNode value = getNode(node, label);
        return value == null ? null : value.asText();
    }

    private Integer getInteger(JsonNode node, String label) {
        JsonNode value = getNode(node, label);
        return value == null ? null : value.asInt();
    }

    private Double getDouble(JsonNode node, String label) {
        JsonNode value = getNode(node, label);
        return value == null ? null : value.asDouble();
    }

    private Boolean getBoolean(JsonNode node, String label) {
        JsonNode value = getNode(node, label);
        return value != null && value.asBoolean();
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    private FormFieldType getType(JsonNode node) {

        String className = node.get("code").asText();

        switch (className) {
            case INTEGER_TYPE:
                return FormFieldType.INTEGER;
            case DOUBLE_TYPE:
                return FormFieldType.DOUBLE;
            case TEXT_TYPE:
            case STRING_TYPE:
                return FormFieldType.STRING;
            case BOOLEAN_TYPE:
                return FormFieldType.BOOLEAN;
            case DATE_TYPE:
                return FormFieldType.DATE;
            case SLIDER_TYPE:
                return FormFieldType.SLIDER;
            case SUBFORM_TYPE:
                return FormFieldType.SUBFORM;
            case RADIO_TYPE:
                return FormFieldType.RADIO;
            case MULTIPLE_SELECTOR_TYPE:
                return FormFieldType.MULTIPLE;
            case COMBO_TYPE:
                return FormFieldType.COMBO;
            case INPUT_LIST_TYPE:
                return FormFieldType.INPUT_LIST;
            default:
                throw new IllegalArgumentException();
        }
    }

}
