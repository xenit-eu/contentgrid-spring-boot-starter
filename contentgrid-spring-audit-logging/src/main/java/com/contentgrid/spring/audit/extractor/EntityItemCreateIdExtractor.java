package com.contentgrid.spring.audit.extractor;

import com.contentgrid.spring.audit.event.AbstractEntityItemAuditEvent;
import com.contentgrid.spring.audit.event.AuditEvent.AuditEventBuilder;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.rest.core.event.AbstractRepositoryEventListener;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.filter.ServerHttpObservationFilter;

@RequiredArgsConstructor
public class EntityItemCreateIdExtractor extends AbstractRepositoryEventListener<Object> implements
        AuditEventExtractor {

    private final PersistentEntities entities;

    @Override
    public Optional<AuditEventBuilder<?, ?>> createEventBuilder(ServerRequestObservationContext context) {
        return Optional.empty();
    }

    @Override
    public AuditEventBuilder<?, ?> enhance(ServerRequestObservationContext context,
            AuditEventBuilder<?, ?> eventBuilder) {
        if(eventBuilder instanceof AbstractEntityItemAuditEvent.AbstractEntityItemAuditEventBuilder<?, ?> itemAuditEventBuilder) {
            Optional.ofNullable(context.get(EntityItemCreateIdExtractor.class))
                    .ifPresent(itemAuditEventBuilder::id);
        }
        return eventBuilder;
    }


    @Override
    protected void onAfterCreate(Object entity) {
        Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .flatMap(attrs -> Optional.ofNullable(
                        attrs.getAttribute(ServerHttpObservationFilter.CURRENT_OBSERVATION_CONTEXT_ATTRIBUTE,
                                RequestAttributes.SCOPE_REQUEST)))
                .filter(ServerRequestObservationContext.class::isInstance)
                .map(ServerRequestObservationContext.class::cast)
                .ifPresent(observationContext -> {
                    var idProperty = entities.getRequiredPersistentEntity(entity.getClass()).getRequiredIdProperty();

                    var entityId = idProperty.getAccessorForOwner(entity).getProperty(idProperty);
                    observationContext.put(EntityItemCreateIdExtractor.class, entityId);
                });
    }
}
