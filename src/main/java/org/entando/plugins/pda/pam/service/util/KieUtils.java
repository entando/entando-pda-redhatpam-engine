package org.entando.plugins.pda.pam.service.util;

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
                submission.put(key, new KieDocument((String) entry.getValue()).getPayload());
            } else if (FormFieldType.DOCUMENT_LIST == field.getType()) {
                submission.put(key, convertFileList((List<String>) entry.getValue()));
            } else {
                submission.put(key, entry.getValue());
            }
        }

        return submission;
    }

    public Map<String, Object> convertFileList(List<String> rawDocuments) {
        Map<String, Object> documents = new ConcurrentHashMap<>();
        documents.put("documents", rawDocuments.stream()
                .map(s -> new KieDocument(s).getPayload())
                .collect(Collectors.toList()));


        Map<String, Object> result = new ConcurrentHashMap<>();
        result.put("org.jbpm.document.service.impl.DocumentCollectionImpl", documents);
        return result;
    }

}
