package com.contentgrid.spring.data.rest.links;

import com.contentgrid.spring.data.rest.mapping.Property;
import com.contentgrid.spring.data.rest.mapping.jackson.JacksonBasedProperty;
import com.contentgrid.spring.data.rest.mapping.persistent.PersistentPropertyProperty;
import com.contentgrid.spring.data.rest.mapping.rest.DataRestBasedProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ResolvableType;
import org.springframework.data.mapping.SimpleAssociationHandler;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.rest.core.Path;
import org.springframework.data.rest.core.support.SelfLinkProvider;
import org.springframework.data.rest.webmvc.RestMediaTypes;
import org.springframework.data.rest.webmvc.mapping.Associations;
import org.springframework.hateoas.AffordanceModel.PayloadMetadata;
import org.springframework.hateoas.AffordanceModel.PropertyMetadata;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.mediatype.hal.HalLinkRelation;
import org.springframework.hateoas.mediatype.html.HtmlInputType;
import org.springframework.http.HttpMethod;

/**
 * Collects links to jpa relations in the {@link ContentGridLinkRelations#RELATION} link-relation
 */
@RequiredArgsConstructor
class SpringDataAssociationLinkCollector implements ContentGridLinkCollector<Object> {
    private final PersistentEntities entities;
    private final Associations associationLinks;
    private final SelfLinkProvider selfLinkProvider;
    private final MessageResolver resolver;

    @Override
    @SuppressWarnings("ConstantConditions")
    public Links getLinksFor(Object object, Links existing) {
        // Logic based on the DefaultLinkCollector from spring-data-rest
        var selfLink = selfLinkProvider.createSelfLinkFor(object);

        if (selfLink == null) {
            return existing;
        }

        Path selfPath = new Path(selfLink.expand().getHref());

        var links = new ArrayList<Link>();
        var entity = entities.getRequiredPersistentEntity(object.getClass());
        entity.doWithAssociations((SimpleAssociationHandler) association -> {
            // We create a property here, so the name matches the one generated for the HAL-FORMS configuration
            // That will automatically create the HAL-FORMS options for the property
            var property = new JacksonBasedProperty(new DataRestBasedProperty(new PersistentPropertyProperty(association.getInverse())));
            for (Link link : associationLinks.getLinksFor(association, selfPath)) {
                var linkName = HalLinkRelation.of(link.getRel()).getLocalPart();
                var cgRelLink = link
                        .withRel(ContentGridLinkRelations.RELATION)
                        .withName(linkName)
                        .withTitle(resolver.resolve(
                                LinkTitle.forProperty(entity.getType(), association.getInverse().getName())
                        ));
                links.add(addAssociationAffordance(cgRelLink, object.getClass(), property));
            }
        });

        return existing.and(links);
    }

    private Link addAssociationAffordance(Link associationLink, Class<?> owner, Property association) {
        if (association.getTypeInformation().isMap()) {
            // Map types are not really supported yet, so they don't get any affordances
            return associationLink;
        } else if (association.getTypeInformation().isCollectionLike()) {
            return Affordances.of(associationLink)
                    .afford(HttpMethod.POST)
                    .withName("add-" + associationLink.getName())
                    .withInput(createPayloadMetadataForRelation(owner, association))
                    .withInputMediaType(RestMediaTypes.TEXT_URI_LIST)
                    .toLink();
        } else {
            var affordances = Affordances.of(associationLink);
            affordances = affordances.afford(HttpMethod.PUT)
                    .withName("set-" + associationLink.getName())
                    .withInput(createPayloadMetadataForRelation(owner, association))
                    .withInputMediaType(RestMediaTypes.TEXT_URI_LIST)
                    .build();
            if (!association.isRequired()) {
                // An association that is required can't be cleared, that would cause a constraint violation error
                affordances = affordances.afford(HttpMethod.DELETE)
                        .withName("clear-" + associationLink.getName())
                        .build();
            }
            return affordances.toLink();
        }
    }

    private PayloadMetadata createPayloadMetadataForRelation(Class<?> owner, Property association) {
        return new AssociationPayloadMetadata(owner, List.of(
                new AssociationPropertyMetadata(association)
        ));
    }

    @RequiredArgsConstructor
    private static class AssociationPayloadMetadata implements PayloadMetadata {

        @Getter
        private final Class<?> type;
        private final List<PropertyMetadata> properties;


        @Override
        public Stream<PropertyMetadata> stream() {
            return properties.stream();
        }
    }

    @RequiredArgsConstructor
    private static class AssociationPropertyMetadata implements PropertyMetadata {

        private final Property association;

        @Override
        public String getName() {
            return association.getName();
        }

        @Override
        public boolean isRequired() {
            return association.isRequired();
        }

        @Override
        public boolean isReadOnly() {
            return association.isReadOnly();
        }

        @Override
        public Optional<String> getPattern() {
            return Optional.empty();
        }

        @Override
        public ResolvableType getType() {
            return association.getTypeInformation().toTypeDescriptor().getResolvableType();
        }

        @Override
        public String getInputType() {
            return HtmlInputType.URL_VALUE;
        }
    }
}
