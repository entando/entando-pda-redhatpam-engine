package org.entando.plugins.pda.pam.service.task.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wnameless.json.flattener.JsonFlattener;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.entando.plugins.pda.core.model.Task;
import org.entando.web.exception.InternalServerException;

@UtilityClass
@Slf4j
public class TaskUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @SuppressWarnings("unchecked")
    public static Map<String, Object> flatProperties(Map<String, Object> data) {
        try {
            if (data == null || data.isEmpty()) {
                return null;
            }

            String flatten = JsonFlattener.flatten(OBJECT_MAPPER.writeValueAsString(data));
            // remove type information
            return OBJECT_MAPPER.readValue(flatten.replaceAll("\\[.*?\\]", ""), Map.class);
        } catch (IOException e) {
            log.error("Error flattening properties", e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static Set<String> extractKeys(Task obj) {
        try {
            return OBJECT_MAPPER.convertValue(obj, Map.class).keySet();
        }  catch (IllegalArgumentException e) {
            throw new InternalServerException("org.entando.kie.error.task.columns", e);
        }
    }
}
