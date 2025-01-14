package com.contentgrid.spring.data.rest.links;

import internal.org.springframework.content.rest.links.ContentLinksResourceProcessor.StoreLinkBuilder;
import internal.org.springframework.content.rest.mappingcontext.ContentPropertyToLinkrelMappingContext;
import internal.org.springframework.content.rest.mappingcontext.ContentPropertyToRequestMappingContext;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import lombok.RequiredArgsConstructor;
import org.springframework.content.commons.mappingcontext.ContentProperty;
import org.springframework.content.commons.mappingcontext.MappingContext;
import org.springframework.content.commons.repository.AssociativeStore;
import org.springframework.content.commons.storeservice.Stores;
import org.springframework.content.rest.config.RestConfiguration;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.rest.webmvc.BaseUri;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.mediatype.hal.HalLinkRelation;
import org.springframework.util.StringUtils;

/**
 * Collects links to spring-content content objects into the {@link ContentGridLinkRelations#CONTENT} link-relation
 */
@RequiredArgsConstructor
class SpringContentLinkCollector implements ContentGridLinkCollector<Object> {
    private final PersistentEntities entities;
    private final Stores stores;
    private final MappingContext mappingContext;
    private final RestConfiguration restConfiguration;
    private final ContentPropertyToRequestMappingContext requestMappingContext;
    private final ContentPropertyToLinkrelMappingContext linkrelMappingContext;
    private final MessageResolver resolver;

    @Override
    @SuppressWarnings("ConstantConditions")
    public Links getLinksFor(Object object, Links existing) {
        // This implementation is inspired on the ContentLinksResourceProcessor from spring-content, but adapted to the context of a LinkCollector

        var persistentEntity = entities.getRequiredPersistentEntity(object.getClass());

        var entityId = persistentEntity.getIdentifierAccessor(object).getIdentifier();

        if(entityId == null) {
            // No entity ID, so no content links (because they reference the entity ID)
            return existing;
        }

        var storeInfo = stores.getStore(AssociativeStore.class, Stores.withDomainClass(persistentEntity.getType()));
        if(storeInfo == null) {
            // No store, we don't have to add any links
            return existing;
        }

        Map<String, ContentProperty> contentProperties = mappingContext.getContentPropertyMap(persistentEntity.getType());

        var links = new ArrayList<Link>(contentProperties.size());

        for (Entry<String, ContentProperty> contentProperty : contentProperties.entrySet()) {
            var linkBuilder = StoreLinkBuilder.linkTo(new BaseUri(restConfiguration.getBaseUri()), storeInfo);
            linkBuilder = linkBuilder.slash(entityId);

            String requestMapping = requestMappingContext.getMappings(storeInfo.getDomainObjectClass()).get(contentProperty.getKey());

            if(StringUtils.hasText(requestMapping)) {
                linkBuilder = linkBuilder.slash(requestMapping);
            } else {
                linkBuilder = linkBuilder.slash(contentProperty.getKey());
            }

            String linkRel = linkrelMappingContext.getMappings(storeInfo.getDomainObjectClass()).get(contentProperty.getKey());
            if(!StringUtils.hasLength(linkRel)) {
                linkRel = contentProperty.getKey();
            }
            // Cut off a potential CURIE prefix from the link relation
            var linkName = HalLinkRelation.of(LinkRelation.of(linkRel)).getLocalPart();


            var link = linkBuilder
                    .withRel(ContentGridLinkRelations.CONTENT)
                    .withName(linkName)
                    .withTitle(resolver.resolve(LinkTitle.forProperty(persistentEntity.getType(), contentProperty.getKey())));

//            var mimeType = contentProperty.getValue().getMimeType(object);
//            if(mimeType != null) {
//                link = link.withType(String.valueOf(mimeType));
//            }
            links.add(link);
        }


        return existing.and(links);
    }
}
