package com.contentgrid.spring.audit;

import com.contentgrid.spring.audit.extractor.AuditEventExtractor;
import com.contentgrid.spring.audit.extractor.BasicAuditEventExtractor;
import com.contentgrid.spring.audit.extractor.EntityContentEventExtractor;
import com.contentgrid.spring.audit.extractor.EntityEventExtractor;
import com.contentgrid.spring.audit.extractor.EntityItemCreateIdExtractor;
import com.contentgrid.spring.audit.extractor.EntityItemEventExtractor;
import com.contentgrid.spring.audit.extractor.EntityRelationEventExtractor;
import com.contentgrid.spring.audit.extractor.EntitySearchEventExtractor;
import com.contentgrid.spring.audit.handler.AuditEventHandler;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.rest.core.mapping.RepositoryResourceMappings;

@Configuration(proxyBeanMethods = false)
public class ContentGridAuditEventConfiguration {

    @Bean
    AuditObservationHandler auditObservabilityHandler(List<AuditEventExtractor> auditEventExtractors,
            List<AuditEventHandler> auditEventHandlers) {
        return new AuditObservationHandler(auditEventExtractors, auditEventHandlers);
    }

    @Data
    public static class ContentgridAuditSystemProperties {
        private String deploymentId = new UUID(0, 0).toString();
        private String applicationId = new UUID(0, 0).toString();
    }

    @Bean
    BasicAuditEventExtractor basicAuditEventExtractor(ContentgridAuditSystemProperties systemProperties) {
        return new BasicAuditEventExtractor(systemProperties.getApplicationId(), systemProperties.getDeploymentId());
    }

    @Bean
    EntityEventExtractor entityEventExtractor(RepositoryResourceMappings resourceMappings) {
        return new EntityEventExtractor(resourceMappings);
    }

    @Bean
    EntitySearchEventExtractor entitySearchEventExtractor() {
        return new EntitySearchEventExtractor();
    }

    @Bean
    EntityItemEventExtractor entityItemEventExtractor() {
        return new EntityItemEventExtractor();
    }

    @Bean
    EntityItemCreateIdExtractor entityItemCreateEventExtractor(PersistentEntities persistentEntities) {
        return new EntityItemCreateIdExtractor(persistentEntities);
    }

    @Bean
    EntityRelationEventExtractor entityRelationEventExtractor() {
        return new EntityRelationEventExtractor();
    }

    @Bean
    EntityContentEventExtractor entityContentEventExtractor(PersistentEntities persistentEntities) {
        return new EntityContentEventExtractor(persistentEntities);
    }
}
