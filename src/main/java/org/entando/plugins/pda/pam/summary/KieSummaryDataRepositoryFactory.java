package org.entando.plugins.pda.pam.summary;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

@Configuration
public class KieSummaryDataRepositoryFactory implements BeanDefinitionRegistryPostProcessor {
    private static final String DATA_TYPES_FILENAME = "summary/kie-summary-data-repositories.properties";
    private static final String DATA_TYPES_PROPERTY_KEY = "org.entando.pda.summary.data.repositories";
    private static final String KIE_DATA_TYPE_BEAN_NAME = "Kie%sDataType";

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) {
        List<String> dataTypes;

        try {
            Resource resource = new ClassPathResource(DATA_TYPES_FILENAME);
            Properties props = PropertiesLoaderUtils.loadProperties(resource);

            dataTypes = Arrays.stream(
                    Optional.ofNullable(props.getProperty(DATA_TYPES_PROPERTY_KEY))
                            .orElse("")
                            .split(","))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new BeanCreationException("Error loading summary type descriptor file", e);
        }

        for (String type : dataTypes) {
            registerNewDataTypeBean(beanDefinitionRegistry, type);
        }
    }

    private void registerNewDataTypeBean(BeanDefinitionRegistry registry, String type) {
        AbstractBeanDefinition dataTypeBean = BeanDefinitionBuilder
                .genericBeanDefinition(KieDataRepository.class)
                .setLazyInit(true)
                .addConstructorArgReference(KieApiService.class.getSimpleName())
                .addConstructorArgValue(type)
                .getBeanDefinition();

        registry.registerBeanDefinition(String.format(KIE_DATA_TYPE_BEAN_NAME, type), dataTypeBean);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) {
        //Do nothing
    }
}
