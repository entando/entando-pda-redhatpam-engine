package org.entando.plugins.pda.pam.service.process;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.entando.plugins.pda.core.utils.TestUtils.readFromFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.List;
import org.entando.plugins.pda.core.model.form.Form;
import org.entando.plugins.pda.pam.util.KieProcessFormTestHelper;
import org.entando.plugins.pda.pam.util.KieTaskFormTestHelper;
import org.junit.Test;

public class KieFormDeserializerTest {

    public static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Form.class, new KieFormDeserializer());
        MAPPER.registerModule(module);
    }

    @Test
    public void shouldDeserializeProcessKieJsonToForms() throws Exception {
        List<Form> result = MAPPER.readValue(readFromFile("process-form.json"), new TypeReference<List<Form>>() {});
        assertThat(result).isEqualTo(KieProcessFormTestHelper.createProcessForms());
    }

    @Test
    public void shouldDeserializeTaskKieJsonToForms() throws Exception {
        List<Form> result = MAPPER.readValue(readFromFile("task-form.json"), new TypeReference<List<Form>>() {});
        assertThat(result).isEqualTo(KieTaskFormTestHelper.createTaskForms());
    }
}
