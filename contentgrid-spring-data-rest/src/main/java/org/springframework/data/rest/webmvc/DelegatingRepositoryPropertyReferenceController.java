package org.springframework.data.rest.webmvc;

import static org.springframework.data.querydsl.QuerydslUtils.QUERY_DSL_PRESENT;
import static org.springframework.data.rest.webmvc.RestMediaTypes.SPRING_DATA_COMPACT_JSON_VALUE;
import static org.springframework.data.rest.webmvc.RestMediaTypes.TEXT_URI_LIST_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import com.contentgrid.spring.data.querydsl.QuerydslBindingsInspector;
import java.io.Serializable;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.StreamSupport;
import javax.persistence.OneToMany;
import lombok.Getter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mapping.IdentifierAccessor;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.repository.support.RepositoryInvoker;
import org.springframework.data.rest.core.mapping.PropertyAwareResourceMapping;
import org.springframework.data.rest.core.mapping.RepositoryResourceMappings;
import org.springframework.data.rest.core.mapping.ResourceMetadata;
import org.springframework.data.rest.core.support.SelfLinkProvider;
import org.springframework.data.rest.webmvc.support.BackendId;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@RepositoryRestController
public class DelegatingRepositoryPropertyReferenceController {

    private static final String BASE_MAPPING = "/{repository}/{id}/{property}";

    private final RepositoryPropertyReferenceController delegate;
    private final Repositories repositories;
    private final RepositoryResourceMappings mappings;

    private final RepositoryEntityLinks entityLinks;

    private final SelfLinkProvider selfLinkProvider;

    private final ObjectProvider<QuerydslBindingsFactory> querydslBindingsFactoryProvider;

    @Autowired
    public DelegatingRepositoryPropertyReferenceController(RepositoryPropertyReferenceController delegate,
            Repositories repositories, RepositoryResourceMappings mappings,
            RepositoryEntityLinks entityLinks, SelfLinkProvider selfLinkProvider,
            ObjectProvider<QuerydslBindingsFactory> querydslBindingsFactoryProvider) {

        this.delegate = delegate;

        this.repositories = repositories;
        this.mappings = mappings;
        this.entityLinks = entityLinks;
        this.selfLinkProvider = selfLinkProvider;
        this.querydslBindingsFactoryProvider = querydslBindingsFactoryProvider;
    }

    @RequestMapping(value = BASE_MAPPING, method = GET)
    public ResponseEntity<?> followPropertyReference(final RootResourceInformation repoRequest,
            @BackendId Serializable id, final @PathVariable String property) throws Exception {

        Function<ReferencedProperty, ResponseEntity<?>> handler = (ReferencedProperty prop) -> {
            if (prop.property.isCollectionLike()) {
                var targetType = prop.property.getPersistentEntityTypeInformation().iterator().next();
                var url = this.entityLinks.linkToCollectionResource(targetType.getType()).expand();
                var mappedBy = prop.property.getAssociation().getInverse().findAnnotation(OneToMany.class).mappedBy();
                var isQueryDslRepository = this.repositories.getRepositoryInformationFor(targetType.getType())
                        .map(repoMetadata -> QUERY_DSL_PRESENT && QuerydslPredicateExecutor.class.isAssignableFrom(
                                repoMetadata.getRepositoryInterface()))
                        .orElse(false);

                var querydslBindingsFactory = this.querydslBindingsFactoryProvider.getIfAvailable();
                if (isQueryDslRepository && querydslBindingsFactory != null) {
                    var querydslBinding = querydslBindingsFactory.createBindingsFor(targetType);
                    var querydslFilter = new QuerydslBindingsInspector(querydslBinding)
                            .findPathBindingFor(mappedBy, targetType.getType());

                    if (querydslFilter.isPresent()) {
                        var filter = querydslFilter.get();
                        var locationUri = URI.create(url.expand().getHref() + "?" + filter + "=" + id);
                        return ResponseEntity.status(HttpStatus.FOUND).location(locationUri).build();
                    }
                }

                // fallback to query-method if target type repo is NOT a querydsl repo ?!
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } else if (prop.property.isMap()) {
                throw new UnsupportedOperationException();
            } else {
                // this is a -to-one type of association, so it is safe to resolve
                return prop.mapValue(target -> this.selfLinkProvider.createSelfLinkFor(prop.getPropertyType(), target))
                        .map(link -> link.getTemplate().expand())
                        .map(uri -> ResponseEntity.status(HttpStatus.FOUND).location(uri).build())
                        .orElse(ResponseEntity.notFound().build());
            }
        };

        return doWithReferencedProperty(repoRequest, id, property, handler, HttpMethod.GET);
    }


    @RequestMapping(value = BASE_MAPPING, method = DELETE)
    public ResponseEntity<? extends RepresentationModel<?>> deletePropertyReference(RootResourceInformation repoRequest,
            @BackendId Serializable id, @PathVariable String property) throws Exception {
        return this.delegate.deletePropertyReference(repoRequest, id, property);
    }

    // Differences:
    // * collections:
    // * maps: not implemented
    // * single:
    //      - actually validates the referenced propertyId is valid
    //      - redirects
    @RequestMapping(value = BASE_MAPPING + "/{propertyId}", method = GET)
    public ResponseEntity<?> followPropertyReference(RootResourceInformation repoRequest,
            @BackendId Serializable id, @PathVariable String property, @PathVariable String propertyId)
            throws Exception {

        Function<ReferencedProperty, ResponseEntity<?>> handler = prop -> {

            if (prop.property.isCollectionLike()) {

                // Possible future optimization:
                // Figure out of there is a query-method available via the repo on the other side
                return prop
                        // this is a collection, so we can iterate over it and consume as a stream
                        .mapValue(val -> (Iterable<?>) val)
                        .map(it -> StreamSupport.stream(it.spliterator(), false))
                        .stream().flatMap(stream -> stream)

                        // try to find a subresource with an id matching propertyId
                        .filter(obj -> {

                            IdentifierAccessor accessor = prop.entity.getIdentifierAccessor(obj);
                            return propertyId.equals(Objects.requireNonNull(accessor.getIdentifier()).toString());
                        })
                        .findFirst()

                        // return HTTP 302 with self-link to the subresource
                        .map(linkedResource -> {
                            // found the linked resource with the given propertyId
                            // note that this access pattern is not recommended for larger collections
                            var link = this.selfLinkProvider.createSelfLinkFor(prop.getPropertyType(), linkedResource);
                            return ResponseEntity.status(HttpStatus.FOUND)
                                    .location(link.getTemplate().expand())
                                    .build();
                        })
                        .orElse(ResponseEntity.notFound().build());

            } else if (prop.property.isMap()) {
                throw new UnsupportedOperationException("not implemented");
            }

            // single-valued association
            return prop.mapValue(target -> {
                        var idAccessor = prop.entity.getIdentifierAccessor(target);
                        if (propertyId.equals(Objects.requireNonNull(idAccessor.getIdentifier()).toString())) {
                            // the property-id matches
                            return target;
                        }

                        // if the propertyId does not match the linked resource, filter out by returning null
                        return null;
                    })
                    .map(target -> this.selfLinkProvider.createSelfLinkFor(prop.getPropertyType(), target))
                    .map(link -> link.getTemplate().expand())
                    .map(uri -> ResponseEntity.status(HttpStatus.FOUND).location(uri).build())
                    .orElse(ResponseEntity.notFound().build());

        };

        return doWithReferencedProperty(repoRequest, id, property, handler, HttpMethod.GET);
    }

    @RequestMapping(value = BASE_MAPPING, method = GET, produces = TEXT_URI_LIST_VALUE)
    public ResponseEntity<RepresentationModel<?>> followPropertyReferenceCompact(RootResourceInformation repoRequest,
            @BackendId Serializable id, @PathVariable String property, @RequestHeader HttpHeaders requestHeaders,
            PersistentEntityResourceAssembler assembler) throws Exception {

        return this.delegate.followPropertyReferenceCompact(repoRequest, id, property, requestHeaders, assembler);
    }

    @RequestMapping(value = BASE_MAPPING, method = {PATCH, PUT, POST}, //
            consumes = {MediaType.APPLICATION_JSON_VALUE, SPRING_DATA_COMPACT_JSON_VALUE, TEXT_URI_LIST_VALUE})
    public ResponseEntity<? extends RepresentationModel<?>> createPropertyReference(
            RootResourceInformation resourceInformation, HttpMethod requestMethod,
            @RequestBody(required = false) CollectionModel<Object> incoming, @BackendId Serializable id,
            @PathVariable String property) throws Exception {

        return this.delegate.createPropertyReference(resourceInformation, requestMethod, incoming, id, property);
    }

    @RequestMapping(value = BASE_MAPPING + "/{propertyId}", method = DELETE)
    public ResponseEntity<RepresentationModel<?>> deletePropertyReferenceId(RootResourceInformation repoRequest,
            @BackendId Serializable backendId, @PathVariable String property, @PathVariable String propertyId)
            throws Exception {

        return this.delegate.deletePropertyReferenceId(repoRequest, backendId, property, propertyId);
    }

    @ExceptionHandler
    public ResponseEntity<Void> handle(
            RepositoryPropertyReferenceController.HttpRequestMethodNotSupportedException exception) {
        return exception.toResponse();
    }

    private ResponseEntity<?> doWithReferencedProperty(
            RootResourceInformation resourceInformation,
            Serializable id, String propertyPath,
            Function<ReferencedProperty, ResponseEntity<?>> handler,
            HttpMethod method) throws Exception {

        ResourceMetadata metadata = resourceInformation.getResourceMetadata();
        PropertyAwareResourceMapping mapping = metadata.getProperty(propertyPath);

        if (mapping == null || !mapping.isExported()) {
            throw new ResourceNotFoundException();
        }

        PersistentProperty<?> property = mapping.getProperty();
        resourceInformation.verifySupportedMethod(method, property);

        RepositoryInvoker invoker = resourceInformation.getInvoker();
        Optional<Object> domainObj = invoker.invokeFindById(id);

        return domainObj.map(it -> {

                    PersistentPropertyAccessor<?> accessor = property.getOwner().getPropertyAccessor(it);
                    return handler.apply(new ReferencedProperty(property, accessor));
                })
                .orElseThrow(ResourceNotFoundException::new);
    }


    private class ReferencedProperty {

        final PersistentEntity<?, ?> entity;
        final PersistentProperty<?> property;

        @Getter
        private final Class<?> propertyType;
        final PersistentPropertyAccessor<?> accessor;

        private ReferencedProperty(PersistentProperty<?> property, PersistentPropertyAccessor<?> accessor) {

            this.property = property;
            this.accessor = accessor;

            this.propertyType = property.getActualType();
            this.entity = repositories.getPersistentEntity(propertyType);
        }

        public <T> Optional<T> mapValue(Function<Object, T> function) {
            return Optional.ofNullable(accessor.getProperty(property)).map(function);
        }
    }
}
