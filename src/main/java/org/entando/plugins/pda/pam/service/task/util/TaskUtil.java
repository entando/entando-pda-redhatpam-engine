package org.entando.plugins.pda.pam.service.task.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wnameless.json.flattener.JsonFlattener;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.entando.plugins.pda.core.model.Task;

@UtilityClass
@Slf4j
public class TaskUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public void flatProperties(List<Task> tasks) {
        for (Task task : tasks) {
            flatProperties(task);
        }
    }

    public void flatProperties(Task task) {
        try {
            Map<String, Object> data = task.getData();
            String flatten = JsonFlattener.flatten(OBJECT_MAPPER.writeValueAsString(data));
            // remove type information
            Map<String, Object> newData = OBJECT_MAPPER.readValue(flatten.replaceAll("\\[.*?\\]", ""), Map.class);
            task.getData().clear();
            task.putAll(newData);
        } catch (IOException e) {
            log.error("Error flattening properties for task {}", task.getId(), e);
        }
    }
}
