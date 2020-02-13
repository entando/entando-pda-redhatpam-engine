package org.entando.plugins.pda.pam.service.process;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.entando.plugins.pda.core.utils.TestUtils.readFromFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
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
    public void shouldDeserializeMortgageProcessKieJsonToForm() throws Exception {
        Form result = MAPPER.readValue(readFromFile("form/process-form-mortgage.json"), Form.class);
        assertThat(result).isEqualTo(KieProcessFormTestHelper.createMortgageProcessForm());
    }

    @Test
    public void shouldDeserializeSampleProcessKieJsonToForm() throws Exception {
        Form result = MAPPER.readValue(readFromFile("form/process-form-sample.json"), Form.class);
        assertThat(result).isEqualTo(KieProcessFormTestHelper.createSampleProcessForm());
    }

    @Test
    public void shouldDeserializeTaskKieJsonToForms() throws Exception {
        Form result = MAPPER.readValue(readFromFile("form/task-form.json"), Form.class);
        assertThat(result).isEqualTo(KieTaskFormTestHelper.createTaskForm());
    }
}
