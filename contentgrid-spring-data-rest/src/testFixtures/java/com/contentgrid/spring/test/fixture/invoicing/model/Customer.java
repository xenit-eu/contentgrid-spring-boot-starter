package com.contentgrid.spring.test.fixture.invoicing.model;

import com.contentgrid.spring.data.rest.validation.AllowedValues;
import com.contentgrid.spring.data.rest.validation.OnEntityDelete;
import com.contentgrid.spring.data.rest.validation.OnEntityUpdate;
import com.contentgrid.spring.data.support.auditing.v1.AuditMetadata;
import com.contentgrid.spring.querydsl.annotation.CollectionFilterParam;
import com.contentgrid.spring.querydsl.predicate.EntityId;
import com.contentgrid.spring.querydsl.predicate.Text.EqualsIgnoreCase;
import com.contentgrid.spring.test.fixture.invoicing.model.support.Content;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.EntityListeners;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.content.rest.RestResource;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.hateoas.InputType;
import org.springframework.hateoas.mediatype.html.HtmlInputType;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty(access = Access.READ_ONLY)
    private UUID id;

    @Version
    private Long version = 0L;

    @JsonProperty(value = "audit_metadata", access = Access.READ_ONLY)
    @Embedded
    @jakarta.persistence.Access(AccessType.PROPERTY)
    @AttributeOverride(name = "createdBy.id", column = @Column(name = "audit_metadata__created_by_id"))
    @AttributeOverride(name = "createdBy.namespace", column = @Column(name = "audit_metadata__created_by_ns"))
    @AttributeOverride(name = "createdBy.name", column = @Column(name = "audit_metadata__created_by_name"))
    @AttributeOverride(name = "createdDate", column = @Column(name = "audit_metadata__created_date"))
    @AttributeOverride(name = "lastModifiedBy.id", column = @Column(name = "audit_metadata__last_modified_by_id"))
    @AttributeOverride(name = "lastModifiedBy.namespace", column = @Column(name = "audit_metadata__last_modified_by_ns"))
    @AttributeOverride(name = "lastModifiedBy.name", column = @Column(name = "audit_metadata__last_modified_by_name"))
    @AttributeOverride(name = "lastModifiedDate", column = @Column(name = "audit_metadata__last_modified_date"))
    private AuditMetadata auditMetadata = new AuditMetadata();

    public void setAuditMetadata(AuditMetadata auditMetadata) {
        if (auditMetadata == null) {
            auditMetadata = new AuditMetadata();
        }
        this.auditMetadata = auditMetadata;
    }

    private String name;

    @Column(unique = true, nullable = false)
    @CollectionFilterParam(predicate = EqualsIgnoreCase.class)
    @NotNull(groups = OnEntityUpdate.class)
    private String vat;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "content__id"))
    @AttributeOverride(name = "length", column = @Column(name = "content__length"))
    @AttributeOverride(name = "mimetype", column = @Column(name = "content__mimetype"))
    @AttributeOverride(name = "filename", column = @Column(name = "content__filename"))
    @RestResource(linkRel = "d:content", path = "content")
    @CollectionFilterParam
    private Content content;

    @CollectionFilterParam
    private Instant birthday;

    @AllowedValues({"female", "male"})
    @InputType(HtmlInputType.RADIO_VALUE)
    @CollectionFilterParam
    private String gender;

    @JsonProperty("total_spend")
    private Integer totalSpend;

    @OneToMany(mappedBy = "customer")
    @org.springframework.data.rest.core.annotation.RestResource(rel = "d:orders")
    @CollectionFilterParam(predicate = EntityId.class, documented = false)
    private Set<Order> orders = new HashSet<>();

    @OneToMany(mappedBy = "counterparty")
    @CollectionFilterParam
    @CollectionFilterParam(value = "invoices.$$id", predicate = EntityId.class, documented = false)
    @org.springframework.data.rest.core.annotation.RestResource(rel = "d:invoices")
    // Invoice#counterparty is required, this constraint ensures that there are no more invoices linked when deleting the customer
    @Size(max = 0, groups = OnEntityDelete.class)
    private Set<Invoice> invoices = new HashSet<>();

    public Customer(String name, String vat) {
        this.name = name;
        this.vat = vat;
    }
}
