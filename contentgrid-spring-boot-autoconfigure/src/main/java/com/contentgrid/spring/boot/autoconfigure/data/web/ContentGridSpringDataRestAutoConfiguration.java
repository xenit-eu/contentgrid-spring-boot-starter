package com.contentgrid.spring.boot.autoconfigure.data.web;

import com.contentgrid.spring.data.rest.affordances.ContentGridSpringDataRestAffordancesConfiguration;
import com.contentgrid.spring.data.rest.hal.ContentGridCurieConfiguration;
import com.contentgrid.spring.data.rest.hal.CurieProviderCustomizer;
import com.contentgrid.spring.data.rest.links.ContentGridSpringContentRestLinksConfiguration;
import com.contentgrid.spring.data.rest.links.ContentGridSpringDataLinksConfiguration;
import com.contentgrid.spring.data.rest.validation.ContentGridSpringDataRestValidationConfiguration;
import com.contentgrid.spring.data.rest.webmvc.ContentGridSpringDataRestProfileConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.content.rest.config.RestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.rest.webmvc.ContentGridRestProperties;
import org.springframework.data.rest.webmvc.ContentGridSpringDataRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;

@AutoConfiguration
@ConditionalOnBean(RepositoryRestMvcConfiguration.class)
@ConditionalOnClass({ContentGridSpringDataRestConfiguration.class, RepositoryRestMvcConfiguration.class})
@Import({
        ContentGridSpringDataRestConfiguration.class,
        ContentGridSpringDataRestProfileConfiguration.class,
        ContentGridSpringDataRestAffordancesConfiguration.class,
        ContentGridSpringDataRestValidationConfiguration.class
})
@AutoConfigureAfter(
        name = {
                // Specifically ContentGridSpringContentRestLinksAutoConfiguration must run after spring-content
                // is initialized so @ConditionalOnBean works correctly. Putting the annotation directly on that class
                // does not work, because it is not an autoconfiguration, but is initialized directly when this parent class
                // is initialized.
                "internal.org.springframework.content.rest.boot.autoconfigure.ContentRestAutoConfiguration"
        },
        value = {
                RepositoryRestMvcAutoConfiguration.class
        }
)
public class ContentGridSpringDataRestAutoConfiguration {

    @Bean
    @ConfigurationProperties("contentgrid.rest")
    ContentGridRestProperties contentGridRestProperties() {
        return new ContentGridRestProperties();
    }

    @ConditionalOnBean(CurieProviderCustomizer.class)
    @Import({
            ContentGridCurieConfiguration.class,
            ContentGridSpringDataLinksConfiguration.class
    })
    @Configuration(proxyBeanMethods = false)
    static class ContentGridSpringDataRestCurieAutoConfiguration {

        @ConditionalOnClass(RestConfiguration.class)
        @ConditionalOnBean(type = "org.springframework.content.commons.storeservice.Stores")
        @Import(ContentGridSpringContentRestLinksConfiguration.class)
        @Configuration(proxyBeanMethods = false)
        static class ContentGridSpringContentRestLinksAutoConfiguration {

        }
    }

}
