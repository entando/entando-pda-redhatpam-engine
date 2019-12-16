package org.entando.plugins.pda.pam.service.process;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.entando.plugins.pda.core.model.form.Form;
import org.entando.plugins.pda.core.model.form.FormField;
import org.entando.plugins.pda.core.model.form.FormFieldInteger;
import org.entando.plugins.pda.core.model.form.FormFieldText;
import org.entando.plugins.pda.core.model.form.FormFieldType;

public class FormDeserializer extends StdDeserializer<Form> {

    private static final String INTEGER_CLASS_NAME = "java.lang.Integer";
    private static final String STRING_CLASS_NAME = "java.lang.String";
    private static final String BOOLEAN_CLASS_NAME = "java.lang.Boolean";

    public FormDeserializer() {
        this(null);
    }

    public FormDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Form deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {

        JsonNode rootNode = jsonParser.getCodec().readTree(jsonParser);
        JsonNode fieldsNode = rootNode.get("fields");

        List<FormField> fields = new ArrayList<>();

        for (JsonNode field : fieldsNode) {

            FormFieldType type = getType(field, "standaloneClassName");

            if (null == type) {
                continue;
            }

            FormField.FormFieldBuilder fieldBuilder;

            if (FormFieldType.STRING == type) {
                fieldBuilder = FormFieldText.builder()
                        .maxLength(getInteger(field, "maxLength"))
                        .minLength(getInteger(field, "minLength"));
            } else if (FormFieldType.INTEGER == type) {
                fieldBuilder = FormFieldInteger.builder()
                        .maxValue(getInteger(field, "maxValue"))
                        .minValue(getInteger(field, "minValue"));
            } else {
                fieldBuilder = FormField.builder();
            }

            fields.add(
                    fieldBuilder
                            .id(getString(field, "id"))
                            .type(type)
                            .name(getString(field, "name"))
                            .label(getString(field, "label"))
                            .required(getBoolean(field, "required"))
                            .readOnly(getBoolean(field, "readOnly"))
                            .placeholder(getString(field, "placeHolder"))
                            .build());
        }

        return Form.builder()
                .id(rootNode.get("id").asText())
                .name(rootNode.get("name").asText())
                .fields(fields)
                .build();
    }

    private String getString(JsonNode node, String label) {
        return node.get(label) == null ? null : node.get(label).asText();
    }

    private Integer getInteger(JsonNode node, String label) {
        return node.get(label) == null ? null : node.get(label).asInt();
    }

    private Boolean getBoolean(JsonNode node, String label) {
        return node.get(label) == null ? null : node.get(label).asBoolean();
    }

    private FormFieldType getType(JsonNode node, String label) {

        String className = node.get(label).asText();

        switch (className) {
            case INTEGER_CLASS_NAME:
                return FormFieldType.INTEGER;
            case STRING_CLASS_NAME:
                return FormFieldType.STRING;
            case BOOLEAN_CLASS_NAME:
                return FormFieldType.BOOLEAN;
            default:
                return null;
        }
    }

}
