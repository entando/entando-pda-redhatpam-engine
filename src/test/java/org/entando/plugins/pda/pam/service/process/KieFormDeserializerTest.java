package org.entando.plugins.pda.pam.service.process;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.entando.plugins.pda.core.utils.TestUtils.readFromFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.util.List;
import org.entando.plugins.pda.core.model.form.Form;
import org.entando.plugins.pda.pam.util.KieProcessFormTestHelper;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class KieFormDeserializerTest {

    @Test
    public void shouldDeserializeKieJsonToForms() {

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Form.class, new KieFormDeserializer());
        mapper.registerModule(module);

        try {
            List<Form> result = mapper.readValue(readFromFile("process-form.json"), new TypeReference<List<Form>>() {});
            assertThat(result).isEqualTo(KieProcessFormTestHelper.createProcessForms());
        } catch (IOException e) {
            Assert.fail();
        }
    }

}
