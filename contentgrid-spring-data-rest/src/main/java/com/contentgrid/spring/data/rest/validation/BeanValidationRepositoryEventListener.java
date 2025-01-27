package com.contentgrid.spring.data.rest.validation;

import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.mapping.SimpleAssociationHandler;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.rest.core.RepositoryConstraintViolationException;
import org.springframework.data.rest.core.ValidationErrors;
import org.springframework.data.rest.core.event.AbstractRepositoryEventListener;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.OptionalValidatorFactoryBean;

@RequiredArgsConstructor
public class BeanValidationRepositoryEventListener extends AbstractRepositoryEventListener<Object> {

    private final PersistentEntities persistentEntities;
    private final ObjectProvider<Validator> validatorProvider;

    private void validate(Object entity, Class<?>... hints) {
        if (entity == null) {
            return;
        }
        Errors errors = new ValidationErrors(entity, persistentEntities);
        var validator = validatorProvider.getIfAvailable(OptionalValidatorFactoryBean::new);

        if (validator.supports(entity.getClass())) {
            ValidationUtils.invokeValidator(validator, entity, errors, (Object[]) hints);
        }

        if (errors.hasErrors()) {
            throw new RepositoryConstraintViolationException(errors);
        }
    }


    @Override
    protected void onBeforeCreate(Object entity) {
        validate(entity, Default.class, OnEntityUpdate.class, OnAssociationUpdate.class);
    }

    @Override
    protected void onBeforeSave(Object entity) {
        validate(entity, Default.class, OnEntityUpdate.class);
    }

    @Override
    protected void onBeforeLinkSave(Object parent, Object linked) {
        validate(parent, Default.class, OnAssociationUpdate.class);
    }

    @Override
    protected void onBeforeLinkDelete(Object parent, Object linked) {
        validate(parent, Default.class, OnAssociationUpdate.class);
    }

    @Override
    protected void onBeforeDelete(Object entity) {
        ensureDirectAssociationsInitialized(entity);
        validate(entity, OnEntityDelete.class);
    }

    /**
     * When validating an object before deletion, we need to ensure that its associations are fully initialized.
     * Hibernate Validator ignores constraints on fields that are not loaded by the ORM, but we need the validations on
     * those fields to ensure that the entity that we're deleting is not the target of a required association.
     */
    private void ensureDirectAssociationsInitialized(Object entity) {
        persistentEntities.getRequiredPersistentEntity(entity.getClass())
                .doWithAssociations((SimpleAssociationHandler) association -> {
                    // fixme: we don't really need to initialize all associations, just the ones that have validation constraints.
                    //  However, which ones those are is a bit hard to determine, so right now all associations are initialized.
                    Hibernate.initialize(association.getInverse()
                            .getAccessorForOwner(entity)
                            .getProperty(association.getInverse()));
                });
    }
}
