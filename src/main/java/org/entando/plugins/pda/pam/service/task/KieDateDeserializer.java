package org.entando.plugins.pda.pam.service.task;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KieDateDeserializer extends JsonDeserializer<Date> {

    private static final String DATE_KEY = "java.util.Date";

    private final String dateFormatStr = System
            .getProperty("org.kie.server.json.date_format", "yyyy-MM-dd'T'hh:mm:ss.SSSZ");
    private final DateFormat dateFormat = new SimpleDateFormat(dateFormatStr, Locale.US);

    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        TreeNode node = jsonParser.getCodec().readTree(jsonParser);
        try {
            if (node.isObject()) {
                LongNode treeNode = (LongNode) node.get(DATE_KEY);
                return new Date(treeNode.longValue());
            } else {
                return dateFormat.parse(((TextNode) node).asText());
            }
        } catch (ParseException e) {
            log.error("Error deserializing date attribute.", e);
        }
        return null;
    }
}
