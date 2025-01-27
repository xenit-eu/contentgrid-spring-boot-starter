package com.contentgrid.spring.test.fixture.invoicing.model;

import com.contentgrid.spring.data.rest.validation.OnAssociationUpdate;
import com.contentgrid.spring.querydsl.annotation.CollectionFilterParam;
import com.contentgrid.spring.querydsl.predicate.EntityId;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import jakarta.persistence.FetchType;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Refund {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty(access = Access.READ_ONLY)
    private UUID id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @CollectionFilterParam
    @CollectionFilterParam(predicate = EntityId.class, documented = false)
    @org.springframework.data.rest.core.annotation.RestResource(rel = "d:invoice")
    @NotNull(groups = OnAssociationUpdate.class)
    private Invoice invoice;

}
