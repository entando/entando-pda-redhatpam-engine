package org.entando.plugins.pda.pam.service;

import org.entando.keycloak.security.AuthenticatedUser;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.web.request.Filter;
import org.entando.web.request.PagedListRequest;

public class KieUtils {

    public static String createUserFilter(Connection connection, AuthenticatedUser user) {
        String username = user == null ? connection.getUsername() : user.getAccessToken().getPreferredUsername();
        return "?user=" + username;
    }

    public static String createFilters(PagedListRequest request) {
        return createFilters(request, false);
    }

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
