package org.entando.plugins.pda.pam.service.util;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.entando.plugins.pda.core.model.File;

public class KieDocument {

    private static final String LAST_MODIFIED = "lastModified";
    private static final String IDENTIFIER = "identifier";
    private static final String NAME = "name";
    private static final String SIZE = "size";
    private static final String CONTENT = "content";
    private static final String ATTRIBUTES = "attributes";
    private static final String CONTENT_TYPE = "content-type";

    public static final String KIE_DOCUMENT_TYPE = "org.jbpm.document.service.impl.DocumentImpl";

    private final Map<String, Object> document;
    private final File file;

    public KieDocument(String rawData) {
        file = new File(rawData);

        Map<String, String> attributes = new ConcurrentHashMap<>();
        if (file.getType() != null) {
            attributes.put(CONTENT_TYPE, file.getType());
        }

        document = new ConcurrentHashMap<>();
        document.put(LAST_MODIFIED, new Date());
        document.put(NAME, file.getName());
        document.put(SIZE, file.getSize());
        document.put(CONTENT, file.getData());
        document.put(ATTRIBUTES, attributes);
    }

    @SuppressWarnings("unchecked")
    public KieDocument(Map<String, Object> payload) {
        document = (Map<String, Object>) Optional.ofNullable(payload)
                .orElse(new ConcurrentHashMap<>())
                .get(KIE_DOCUMENT_TYPE);

        Map<String, String> attributes = (Map<String, String>) document.get(ATTRIBUTES);

        file = File.builder()
                .name((String) document.get(NAME))
                .size((Integer) document.get(SIZE))
                .type(attributes.get(CONTENT_TYPE))
                .data((String) document.get(CONTENT_TYPE))
                .build();
    }

    public void setId(String id) {
        document.put(IDENTIFIER, id);
    }

    public String getId() {
        return (String) document.get(IDENTIFIER);
    }

    public String getFilename() {
        return (String) document.get(NAME);
    }

    public String getContent() {
        return file.getData();
    }

    public void setContent(byte[] content) {
        file.setData(content);
        file.setSize(content.length);
    }

    public void setContent(String content) {
        file.setData(content);
        file.setSize(content.getBytes(StandardCharsets.UTF_8).length);
    }

    public File getFile() {
        return file;
    }

    public Map<String, Object> getPayload() {
        Map<String, Object> payload = new ConcurrentHashMap<>();
        payload.put(KIE_DOCUMENT_TYPE, document);
        return payload;
    }
}
