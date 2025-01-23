package com.contentgrid.spring.data.rest.links;

import internal.org.springframework.content.rest.mappingcontext.ContentPropertyToLinkrelMappingContext;
import internal.org.springframework.content.rest.mappingcontext.ContentPropertyToRequestMappingContext;
import org.springframework.content.commons.mappingcontext.MappingContext;
import org.springframework.content.commons.storeservice.Stores;
import org.springframework.content.rest.config.RestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.hateoas.mediatype.MessageResolver;

@Configuration(proxyBeanMethods = false)
@Import(ContentGridSpringDataLinksConfiguration.class)
public class ContentGridSpringContentRestLinksConfiguration {
    @Bean
    ContentGridLinkCollector<?> contentGridSpringContentLinkCollector(
            PersistentEntities entities, Stores stores, MappingContext mappingContext,
            RestConfiguration restConfiguration, ContentPropertyToRequestMappingContext requestMappingContext,
            ContentPropertyToLinkrelMappingContext linkrelMappingContext, MessageResolver resolver
    ) {
        return new SpringContentLinkCollector(entities, stores, mappingContext, restConfiguration,
                requestMappingContext, linkrelMappingContext, resolver);
    }
}
