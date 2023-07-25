package com.contentgrid.spring.boot.autoconfigure.data.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.data.rest.webmvc.ContentGridSpringDataRestConfiguration;
import org.springframework.data.rest.webmvc.DelegatingRepositoryPropertyReferenceController;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.hateoas.mediatype.hal.HalConfiguration;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsConfiguration;

class ContentGridSpringDataRestAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    ContentGridSpringDataRestAutoConfiguration.class,
                    RepositoryRestMvcAutoConfiguration.class
            ));

    @Test
    void checkContentGridSpringDataRest() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(DelegatingRepositoryPropertyReferenceController.class);
            assertThat(context).hasBean("repositoryPropertyReferenceController");
            assertThat(context).hasNotFailed();
        });
    }

    @Test
    void checkContentGridProfile_halConfiguration_used() {
        var halConfiguration = new HalConfiguration();
        contextRunner
                .withBean(HalConfiguration.class, () -> halConfiguration)
                .run(context -> {
                    assertThat(context).hasSingleBean(HalFormsConfiguration.class);
                    assertThat(context.getBean(HalFormsConfiguration.class).getHalConfiguration()).isSameAs(halConfiguration);
                });
    }

    @Test
    void when_contentGridSpringDataRest_isNotOnClasspath() {
        contextRunner
                .withClassLoader(new FilteredClassLoader(ContentGridSpringDataRestConfiguration.class))
                .run(context -> {
                    assertThat(context).doesNotHaveBean(DelegatingRepositoryPropertyReferenceController.class);
                    assertThat(context).hasBean("repositoryPropertyReferenceController");
                    assertThat(context).hasNotFailed();
                });
    }

    @Test
    void when_springDataRestWebmvc_isNotOnClasspath() {
        contextRunner
                .withClassLoader(new FilteredClassLoader(RepositoryRestMvcConfiguration.class))
                .run(context -> {
                    assertThat(context).doesNotHaveBean(DelegatingRepositoryPropertyReferenceController.class);
                    assertThat(context).doesNotHaveBean("repositoryPropertyReferenceController");
                    assertThat(context).hasNotFailed();
                });
    }
}