package com.contentgrid.spring.boot.autoconfigure.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import com.contentgrid.spring.integration.events.ContentGridPublisherEventListener;

class EventsAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(EventsAutoConfiguration.class, RepositoryRestMvcAutoConfiguration.class))
            .withUserConfiguration(TestConfig.class);

    @Configuration
    @AutoConfigurationPackage
    @DataJpaTest
    public static class TestConfig {


    }

    @Test
    void checkContentGridPublisher_beanExists() {
        contextRunner
                .run(context -> {
                    assertThat(context).hasSingleBean(ContentGridPublisherEventListener.class);
                });
    }

    @Test
    void withoutContentGridPublisherEventListener_onClasspath() {
        contextRunner
                .withClassLoader(new FilteredClassLoader(ContentGridPublisherEventListener.class))
                .run(context -> {
                    assertThat(context).doesNotHaveBean(ContentGridPublisherEventListener.class);
                });
    }
}