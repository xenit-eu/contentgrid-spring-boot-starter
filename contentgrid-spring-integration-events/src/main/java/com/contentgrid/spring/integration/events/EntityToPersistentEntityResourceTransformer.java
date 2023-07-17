package com.contentgrid.spring.integration.events;

import com.contentgrid.spring.integration.events.ContentGridEventPublisher.ContentGridMessage;
import com.contentgrid.spring.integration.events.ContentGridEventPublisher.ContentGridMessage.DataEntity;
import com.contentgrid.spring.integration.events.ContentGridEventPublisher.ContentGridMessagePayload;
import com.contentgrid.spring.integration.events.ContentGridEventPublisher.ContentGridMessagePayload.PersistentEntityResourceData;
import org.springframework.hateoas.EntityModel;
import org.springframework.integration.transformer.AbstractPayloadTransformer;

public class EntityToPersistentEntityResourceTransformer
        extends AbstractPayloadTransformer<ContentGridMessage, ContentGridMessagePayload> {
    private final ContentGridHalAssembler contentGridHalAssembler;

    public EntityToPersistentEntityResourceTransformer(
            ContentGridHalAssembler contentGridHalAssembler) {
        this.contentGridHalAssembler = contentGridHalAssembler;
    }

    @Override
    protected ContentGridMessagePayload transformPayload(ContentGridMessage contentGridMessage) {
        DataEntity updatedEntity = contentGridMessage.getData();
        EntityModel<?> newModel = updatedEntity.entity != null
                ? contentGridHalAssembler.toModel(updatedEntity.entity)
                : null;
        EntityModel<?> oldModel = updatedEntity.old != null
                ? contentGridHalAssembler.toModel(updatedEntity.old)
                : null;

        return new ContentGridMessagePayload(contentGridMessage.getTrigger(), contentGridMessage.getEntityName(), new PersistentEntityResourceData(oldModel, newModel));
    }
}
