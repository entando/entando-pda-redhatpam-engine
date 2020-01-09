package org.entando.plugins.pda.pam.service.util;

import lombok.experimental.UtilityClass;
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

}
