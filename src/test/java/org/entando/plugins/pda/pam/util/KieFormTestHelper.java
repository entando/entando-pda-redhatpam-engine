package org.entando.plugins.pda.pam.util;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

@UtilityClass
public class KieFormTestHelper {

    @SuppressWarnings("unchecked")
    public static Map<String, Object> trimIgnoreProperties(Map<String, Object> result) {
        return result.entrySet().stream()
                .peek(e -> {
                    if (e.getValue() instanceof Map) {
                        e.setValue(trimIgnoreProperties((Map<String, Object>) e.getValue()));
                    } else if (e.getValue() instanceof List && e.getKey().equals("documents")) {
                        e.setValue(trimIgnoreProperties((List<Map<String, Object>>) e.getValue()));
                    } else if (e.getKey().equals("lastModified")) {
                        e.setValue("**ignore**");
                    }
                })
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    public static List<Map<String, Object>> trimIgnoreProperties(List<Map<String, Object>> result) {
        return result.stream()
                .filter(Objects::nonNull)
                .peek(KieFormTestHelper::trimIgnoreProperties)
                .collect(Collectors.toList());
    }
}
