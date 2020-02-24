package org.entando.plugins.pda.pam.service.util;

import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.entando.plugins.pda.core.model.form.Form;
import org.entando.plugins.pda.core.model.form.FormField;
import org.entando.plugins.pda.core.model.form.FormFieldSubForm;
import org.entando.plugins.pda.core.model.form.FormFieldType;
import org.entando.web.exception.BadRequestException;
import org.entando.web.request.Filter;
import org.entando.web.request.PagedListRequest;

@Slf4j
@UtilityClass
public class KieUtils {

    public static String createFilters(PagedListRequest request, boolean addQueryOperator) {
        StringBuilder queryUrl = new StringBuilder();

        queryUrl.append(String.format("%spage=%d&pageSize=%d", addQueryOperator ? "?" : "&",
                request.getPage() - 1, request.getPageSize()));

        if (request.getSort() != null) {
            queryUrl.append(String.format("&sort=%s&sortOrder=%s",
                    request.getSort(), request.getDirection().equals(Filter.ASC_ORDER)));
        }

        return queryUrl.toString();
    }

    @SuppressWarnings({ "unchecked", "PMD.AvoidInstantiatingObjectsInLoops" })
    public static Map<String, Object> createFormSubmission(Form form, Map<String, Object> request) {
        Map<String, Object> submission = new ConcurrentHashMap<>();

        for (Map.Entry<String, Object> entry : request.entrySet()) {
            String key = entry.getKey();
            FormField field = form.getFieldByName(key);

            if (field == null) {
                throw new BadRequestException();
            }

            if (FormFieldType.SUBFORM.equals(field.getType())) {
                Map<String, Object> subFormSubmission = new ConcurrentHashMap<>();
                submission.put(key, subFormSubmission);

                if (!(entry.getValue() instanceof Map)) {
                    throw new BadRequestException();
                }

                FormFieldSubForm fieldSubForm = (FormFieldSubForm) field;
                subFormSubmission.put(fieldSubForm.getFormType(),
                        createFormSubmission(fieldSubForm.getForm(), (Map<String, Object>) request.get(key)));
            } else if (FormFieldType.DOCUMENT == field.getType()) {
                submission.put(key, convertFile((String) entry.getValue()));
            } else if (FormFieldType.DOCUMENT_LIST == field.getType()) {
                submission.put(key, convertFileList((List<String>) entry.getValue()));
            } else {
                submission.put(key, entry.getValue());
            }
        }

        return submission;
    }

    private Map<String, Object> convertFileList(List<String> rawDocuments) {
        Map<String, Object> documents = new ConcurrentHashMap<>();
        documents.put("documents", rawDocuments.stream()
                .map(KieUtils::convertFile)
                .collect(Collectors.toList()));


        Map<String, Object> result = new ConcurrentHashMap<>();
        result.put("org.jbpm.document.service.impl.DocumentCollectionImpl", documents);
        return result;
    }

    private Map<String, Object> convertFile(String rawData) {
        String[] split = rawData.split(";");
        String type = null;
        String name = null;
        String data = "";
        int size = 0;
        Map<String, String> attributes = new ConcurrentHashMap<>();

        for (String property : split) {
            if (property.startsWith("data:")) {
                type = property.replace("data:", "");
            } else if (property.startsWith("name=")) {
                name = property.replace("name=", "").replace("%20", " ");
            } else if (property.startsWith("base64,")) {
                data = property.replace("base64,", "");
                size = Base64.getDecoder().decode(data).length;
            }
        }

        if (type != null) {
            attributes.put("content-type", type);
        }

        Map<String, Object> document = new ConcurrentHashMap<>();
        document.put("lastModified", new Date());
        document.put("name", name);
        document.put("size", size);
        document.put("content", data);
        document.put("attributes", attributes);

        Map<String, Object> result = new ConcurrentHashMap<>();
        result.put("org.jbpm.document.service.impl.DocumentImpl", document);
        return result;
    }

}
