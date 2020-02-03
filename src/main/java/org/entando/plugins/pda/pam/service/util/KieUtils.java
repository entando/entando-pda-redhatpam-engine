package org.entando.plugins.pda.pam.service.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.experimental.UtilityClass;
import org.entando.plugins.pda.core.model.form.Form;
import org.entando.plugins.pda.core.model.form.FormField;
import org.entando.plugins.pda.core.model.form.FormFieldSubForm;
import org.entando.plugins.pda.core.model.form.FormFieldType;
import org.entando.web.exception.BadRequestException;
import org.entando.web.request.Filter;
import org.entando.web.request.PagedListRequest;

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

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
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
            } else {
                submission.put(key, entry.getValue());
            }
        }

        return submission;
    }

}
