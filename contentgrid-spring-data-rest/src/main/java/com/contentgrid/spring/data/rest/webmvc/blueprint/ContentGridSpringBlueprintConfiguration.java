package com.contentgrid.spring.data.rest.webmvc.blueprint;

import com.contentgrid.spring.data.querydsl.sort.CollectionFilterSortHalFormsPayloadMetadataContributor;
import com.contentgrid.spring.data.rest.hal.CurieProviderCustomizer;
import com.contentgrid.spring.querydsl.mapping.CollectionFiltersMapping;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.ResourceMappings;
import org.springframework.hateoas.mediatype.MessageResolver;

@Configuration
public class ContentGridSpringBlueprintConfiguration {

    @Bean
    CurieProviderCustomizer dataModelCurieProvider() {
        return CurieProviderCustomizer.register(BlueprintLinkRelations.CURIE, BlueprintLinkRelations.TEMPLATE);
    }

    @Bean
    EntityRepresentationModelAssembler entityRepresentationModelAssembler(
            Repositories repositories, MessageResolver messageResolver,
            RepositoryRestConfiguration repositoryRestConfiguration, ResourceMappings resourceMappings,
            CollectionFiltersMapping collectionFiltersMapping,
            CollectionFilterSortHalFormsPayloadMetadataContributor contributor
    ) {
        return new EntityRepresentationModelAssembler(repositories, messageResolver, repositoryRestConfiguration,
                resourceMappings, collectionFiltersMapping, contributor);
    }
}
