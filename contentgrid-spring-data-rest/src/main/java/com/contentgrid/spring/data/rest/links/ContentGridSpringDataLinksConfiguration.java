package com.contentgrid.spring.data.rest.links;

import com.contentgrid.spring.data.rest.hal.CurieProviderCustomizer;
import com.contentgrid.spring.data.rest.webmvc.ProfileLinksResource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.ResourceMappings;
import org.springframework.data.rest.core.support.SelfLinkProvider;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.data.rest.webmvc.mapping.Associations;
import org.springframework.data.rest.webmvc.mapping.LinkCollector;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.mediatype.MediaTypeConfigurationCustomizer;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.mediatype.hal.CurieProvider;
import org.springframework.hateoas.mediatype.hal.HalConfiguration;
import org.springframework.hateoas.mediatype.hal.HalConfiguration.RenderSingleLinks;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.hateoas.server.RepresentationModelProcessor;

@Configuration(proxyBeanMethods = false)
public class ContentGridSpringDataLinksConfiguration {

    @Bean
    RepositoryRestConfigurer contentGridLinkCollectorConfigurer(
            ObjectProvider<ContentGridLinkCollector<?>> collectors
    ) {
        return new RepositoryRestConfigurer() {
            @Override
            public LinkCollector customizeLinkCollector(LinkCollector collector) {
                return new AggregateLinkCollector(collector, collectors);
            }
        };
    }

    @Bean
    ContentGridLinkCollector<?> contentGridRelationLinkCollector(PersistentEntities entities, Associations associations,
            SelfLinkProvider selfLinkProvider, MessageResolver resolver) {
        return new SpringDataAssociationLinkCollector(entities, associations, selfLinkProvider, resolver);
    }

    @Bean
    CurieProviderCustomizer contentGridCurieProviderCustomizer() {
        return CurieProviderCustomizer.register(ContentGridLinkRelations.CURIE, ContentGridLinkRelations.TEMPLATE);
    }

    @Bean
    MediaTypeConfigurationCustomizer<HalConfiguration> contentGridLinksMediaTypeConfigurationCustomizer() {
        return halConfiguration -> halConfiguration
                .withRenderSingleLinksFor(ContentGridLinkRelations.CONTENT, RenderSingleLinks.AS_ARRAY)
                .withRenderSingleLinksFor(ContentGridLinkRelations.RELATION, RenderSingleLinks.AS_ARRAY)
                .withRenderSingleLinksFor(ContentGridLinkRelations.ENTITY, RenderSingleLinks.AS_ARRAY);
    }

    @Bean
    RepresentationModelProcessor<RepositoryLinksResource> contentGridRepositoryLinksResourceProcessor(Repositories repositories, ResourceMappings resourceMappings, EntityLinks entityLinks, MessageResolver resolver) {
        return new SpringDataRepositoryLinksResourceProcessor(repositories, resourceMappings, entityLinks, resolver);
    }

    @Bean
    RepresentationModelProcessor<ProfileLinksResource> contentGridProfileLinksResourceProcessor(Repositories repositories, ResourceMappings resourceMappings, RepositoryRestConfiguration configuration, MessageResolver resolver) {
        return new SpringDataProfileLinksResourceProcessor(repositories, resourceMappings, configuration, resolver);
    }

    @Bean
    RepresentationModelProcessor<CollectionModel<?>> contentGridSpringDataEmbeddedItemResourceProcessor() {
        return new SpringDataEmbeddedItemResourceProcessor();
    }

    @Bean
    RepresentationModelProcessor<CollectionModel<?>> contentGridSpringDataEmbeddedCuriesResourceProcessor(
            LinkRelationProvider linkRelationProvider, CurieProvider curieProvider
    ) {
        return new SpringDataEmbeddedCuriesResourceProcessor(linkRelationProvider, curieProvider);
    }
}
