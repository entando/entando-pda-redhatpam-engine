package org.entando.plugins.pda.pam.summary;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

public class KieSummaryDataRepositoryFactoryTest {

    @Test
    public void shouldLoadDataTypes() {
        //Given
        KieSummaryDataRepositoryFactory factory = new KieSummaryDataRepositoryFactory();
        BeanDefinitionRegistry registry = mock(BeanDefinitionRegistry.class);

        //When
        factory.postProcessBeanDefinitionRegistry(registry);

        //Then
        verify(registry, times(2))
                .registerBeanDefinition(matches("Kie(.*)DataType"), any(BeanDefinition.class));
    }
}
