package com.contentgrid.spring.data.rest.webmvc.blueprint;

import com.contentgrid.spring.data.rest.webmvc.blueprint.config.ConstraintModel;
import com.contentgrid.spring.data.rest.webmvc.blueprint.config.ConstraintModel.AllowedValuesConstraintModel;
import com.contentgrid.spring.data.rest.webmvc.blueprint.config.ConstraintModel.RequiredConstraintModel;
import com.contentgrid.spring.data.rest.webmvc.blueprint.config.ConstraintModel.UniqueConstraintModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import org.springframework.hateoas.server.core.Relation;

public sealed interface AttributeConstraintRepresentationModel {

    @JsonProperty
    String getType();

    static AttributeConstraintRepresentationModel from(ConstraintModel constraint) {
        if (constraint instanceof AllowedValuesConstraintModel allowedValuesConstraint) {
            return allowedValues(allowedValuesConstraint.getValues());
        } else if (constraint instanceof RequiredConstraintModel) {
            return required();
        } else if (constraint instanceof UniqueConstraintModel) {
            return unique();
        } else {
            // We want to be able to update this without breaking applications
            return null;
        }
    }

    static AllowedValuesConstraintRepresentationModel allowedValues(List<String> values) {
        return AllowedValuesConstraintRepresentationModel.builder()
                .values(values)
                .build();
    }

    static RequiredConstraintRepresentationModel required() {
        return new RequiredConstraintRepresentationModel();
    }

    static UniqueConstraintRepresentationModel unique() {
        return new UniqueConstraintRepresentationModel();
    }

    @Builder
    @Value
    @Relation(BlueprintLinkRelations.CONSTRAINT_STRING)
    class AllowedValuesConstraintRepresentationModel implements AttributeConstraintRepresentationModel {

        @Builder.Default
        List<String> values = new ArrayList<>();

        @Override
        public String getType() {
            return "allowed-values";
        }
    }

    @Relation(BlueprintLinkRelations.CONSTRAINT_STRING)
    final class RequiredConstraintRepresentationModel implements AttributeConstraintRepresentationModel {

        @Override
        public String getType() {
            return "required";
        }
    }

    @Relation(BlueprintLinkRelations.CONSTRAINT_STRING)
    final class UniqueConstraintRepresentationModel implements AttributeConstraintRepresentationModel {

        @Override
        public String getType() {
            return "unique";
        }
    }
}
