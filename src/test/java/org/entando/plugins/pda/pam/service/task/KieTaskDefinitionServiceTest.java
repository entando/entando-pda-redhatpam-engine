package org.entando.plugins.pda.pam.service.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.entando.plugins.pda.pam.util.KieTaskTestHelper.TASK_DEFINITION_COLUMNS;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;

public class KieTaskDefinitionServiceTest {

    private KieTaskDefinitionService taskDefinitionService;

    @Before
    public void setUp() {
        taskDefinitionService = new KieTaskDefinitionService();
    }

    @Test
    public void shouldReturnTaskColumns() {
        // When
        Set<String> response = taskDefinitionService.listColumns();

        // Then
        assertThat(response)
                .containsAll(TASK_DEFINITION_COLUMNS);
    }
}
