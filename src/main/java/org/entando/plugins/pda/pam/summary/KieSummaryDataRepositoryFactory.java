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
    private static final String DATA_REPOSITORIES_FILENAME = "summary/kie-summary-data-repositories.properties";
    private static final String DATA_REPOSITORIES_PROPERTY_KEY = "org.entando.pda.summary.data.repositories";
    private static final String KIE_DATA_REPOSITORY_BEAN_NAME = "Kie%sDataRepository";

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) {
        List<String> repositories;

        try {
            Resource resource = new ClassPathResource(DATA_REPOSITORIES_FILENAME);
            Properties props = PropertiesLoaderUtils.loadProperties(resource);

            repositories = Arrays.stream(
                    Optional.ofNullable(props.getProperty(DATA_REPOSITORIES_PROPERTY_KEY))
                            .orElse("")
                            .split(","))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new BeanCreationException("Error loading summary type descriptor file", e);
        }

        for (String type : repositories) {
            registerDataRepositoryBean(beanDefinitionRegistry, type);
        }
    }

    private void registerDataRepositoryBean(BeanDefinitionRegistry registry, String type) {
        AbstractBeanDefinition dataRepositoryBean = BeanDefinitionBuilder
                .genericBeanDefinition(KieDataRepository.class)
                .setLazyInit(true)
                .addConstructorArgReference(KieApiService.class.getSimpleName())
                .addConstructorArgValue(type)
                .getBeanDefinition();

        registry.registerBeanDefinition(String.format(KIE_DATA_REPOSITORY_BEAN_NAME, type), dataRepositoryBean);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) {
        //Do nothing
    }
}
