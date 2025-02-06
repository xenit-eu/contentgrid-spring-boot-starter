package com.contentgrid.spring.data.rest.webmvc.blueprint;

import com.contentgrid.spring.data.querydsl.sort.CollectionFilterSortHalFormsPayloadMetadataContributor;
import com.contentgrid.spring.data.rest.mapping.DomainTypeMapping;
import com.contentgrid.spring.data.rest.mapping.persistent.ThroughAssociationsContainer;
import com.contentgrid.spring.data.rest.mapping.rest.DataRestBasedContainer;
import com.contentgrid.spring.querydsl.mapping.CollectionFiltersMapping;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.ResourceMappings;
import org.springframework.data.rest.webmvc.RootResourceInformation;
import org.springframework.data.util.TypeInformation;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.mediatype.hal.HalLinkRelation;
import org.springframework.hateoas.server.RepresentationModelAssembler;

@RequiredArgsConstructor
public class EntityRepresentationModelAssembler implements
        RepresentationModelAssembler<RootResourceInformation, EntityRepresentationModel> {

    private final DomainTypeMapping domainTypeMapping;
    private final MessageResolver messageResolver;
    private final AttributeRepresentationModelAssembler attributeAssembler;
    private final RelationRepresentationModelAssembler relationAssembler;

    public EntityRepresentationModelAssembler(
            Repositories repositories,
            MessageResolver messageResolver,
            RepositoryRestConfiguration repositoryRestConfiguration,
            ResourceMappings resourceMappings,
            CollectionFiltersMapping collectionFiltersMapping,
            CollectionFilterSortHalFormsPayloadMetadataContributor contributor
    ) {
        this.domainTypeMapping = new DomainTypeMapping(repositories)
                .wrapWith(container -> new ThroughAssociationsContainer(container, repositories, 1))
                .wrapWith(DataRestBasedContainer::new)
                // no JacksonBasedContainer because we still need to access the java property names
        ;
        this.messageResolver = messageResolver;
        this.attributeAssembler = new AttributeRepresentationModelAssembler(collectionFiltersMapping, messageResolver, contributor);
        this.relationAssembler = new RelationRepresentationModelAssembler(repositoryRestConfiguration, resourceMappings, messageResolver);
    }

    @Override
    public EntityRepresentationModel toModel(RootResourceInformation information) {
        var attributes = new ArrayList<AttributeRepresentationModel>();
        var relations = new ArrayList<RelationRepresentationModel>();

        var entityContainer = domainTypeMapping.forDomainType(information.getDomainType());

        entityContainer.doWithProperties(property -> {
            var attribute = attributeAssembler.toModel(information, entityContainer, List.of(property));
            attribute.ifPresent(attributes::add);
        });

        entityContainer.doWithAssociations(property -> {
            var relation = relationAssembler.toModel(information, property);
            relation.ifPresent(relations::add);
        });

        var linkRel = information.getResourceMetadata().getItemResourceRel().value();

        // Cut off a potential CURIE prefix from the link relation
        var name = HalLinkRelation.of(LinkRelation.of(linkRel)).getLocalPart();

        return EntityRepresentationModel.builder()
                .name(name)
                .title(readTitle(entityContainer.getTypeInformation()))
                .description(readDescription(information))
                .attributes(attributes)
                .relations(relations)
                .build();
    }

    private String readDescription(RootResourceInformation information) {
        var description = messageResolver.resolve(DescriptionMessageSourceResolvable.forEntity(information));
        return description == null ? "" : description;
    }

    private String readTitle(TypeInformation<?> information) {
        return messageResolver.resolve(TitleMessageSourceResolvable.forEntity(information));
    }

}
