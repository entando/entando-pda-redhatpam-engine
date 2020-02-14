package org.entando.plugins.pda.pam.service.task;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.LongNode;
import java.io.IOException;
import java.util.Date;

public class KieDateDeserializer extends JsonDeserializer<Date> {

    private static final String DATE_KEY = "java.util.Date";

    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        TreeNode jsonObject = jsonParser.getCodec().readTree(jsonParser);
        LongNode treeNode = (LongNode) jsonObject.get(DATE_KEY);
        return new Date(treeNode.longValue());
    }
}
