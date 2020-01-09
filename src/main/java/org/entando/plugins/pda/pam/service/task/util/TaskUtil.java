package org.entando.plugins.pda.pam.service.task.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wnameless.json.flattener.JsonFlattener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
            String flatten = JsonFlattener.flatten(OBJECT_MAPPER.writeValueAsString(
                    Optional.ofNullable(data).orElse(new HashMap<>())));
            // remove type information
            return OBJECT_MAPPER.readValue(flatten.replaceAll("\\[.*?\\]", ""), Map.class);
        } catch (IOException e) {
            log.error("Error flattening properties", e);
            return new HashMap<>();
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
