package com.contentgrid.spring.test.fixture.invoicing.model;

import com.contentgrid.spring.data.rest.validation.OnAssociationUpdate;
import com.contentgrid.spring.data.rest.validation.OnEntityDelete;
import com.contentgrid.spring.data.rest.validation.OnEntityUpdate;
import com.contentgrid.spring.data.support.auditing.v1.AuditMetadata;
import com.contentgrid.spring.querydsl.annotation.CollectionFilterParam;
import com.contentgrid.spring.querydsl.predicate.EntityId;
import com.contentgrid.spring.querydsl.predicate.Text.EqualsIgnoreCase;
import com.contentgrid.spring.querydsl.predicate.None;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Embedded;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.content.commons.annotations.ContentId;
import org.springframework.content.commons.annotations.ContentLength;
import org.springframework.content.commons.annotations.MimeType;
import org.springframework.content.commons.annotations.OriginalFileName;
import org.springframework.content.rest.RestResource;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Invoice {
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

    @Column(nullable = false)
    @CollectionFilterParam(predicate = EqualsIgnoreCase.class)
    @NotNull(groups = OnEntityUpdate.class)
    private String number;

    private boolean draft;

    @CollectionFilterParam
    private boolean paid;

    @ContentId
    @JsonIgnore
    @RestResource(linkRel = "d:content")
    private String contentId;

    @ContentLength
    @JsonProperty(value = "content_length", access = Access.READ_ONLY)
    @CollectionFilterParam("content.length")
    @CollectionFilterParam(value = "content.length.lt", predicate = None.class)
    @CollectionFilterParam(value = "content.length.gt", predicate = None.class)
    private Long contentLength;

    @MimeType
    @JsonProperty("content_mimetype")
    private String contentMimetype;

    @OriginalFileName
    @JsonProperty("content_filename")
    private String contentFilename;

    @ContentId
    @JsonIgnore
    @RestResource(linkRel = "d:attachment")
    private String attachmentId;

    @ContentLength
    @JsonProperty(value = "attachment_length", access = Access.READ_ONLY)
    private Long attachmentLength;

    @MimeType
    @JsonProperty("attachment_mimetype")
    private String attachmentMimetype;

    @OriginalFileName
    @JsonProperty("attachment_filename")
    private String attachmentFilename;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "counterparty", nullable = false)
    @org.springframework.data.rest.core.annotation.RestResource(rel = "d:counterparty")
    @CollectionFilterParam(predicate = EntityId.class, documented = false)
    @NotNull(groups = OnAssociationUpdate.class)
    private Customer counterparty;

    @OneToMany
    @JoinColumn(name = "__invoice_id__orders", foreignKey = @ForeignKey(foreignKeyDefinition = "foreign key (\"__invoice_id__orders\") references \"invoice\" ON DELETE set NULL"))
    @CollectionFilterParam
    @CollectionFilterParam(value = "orders.id", predicate = EntityId.class)
    @org.springframework.data.rest.core.annotation.RestResource(rel = "d:orders")
    private Set<Order> orders = new HashSet<>();

    @OneToOne(mappedBy = "invoice", fetch = FetchType.LAZY)
    @org.springframework.data.rest.core.annotation.RestResource(rel = "d:refund")
    @Null(groups = OnEntityDelete.class)
    // Refund#invoice is required, this constraint ensures that there is no refund linked when deleting the invoice
    private Refund refund;

    public Invoice(String number, boolean draft, boolean paid, Customer counterparty, Set<Order> orders) {
        this.number = number;
        this.draft = draft;
        this.paid = paid;

        this.counterparty = counterparty;
        if (counterparty != null) {
            counterparty.getInvoices().add(this);
        }

        this.orders = orders;
        if (orders != null) {
            orders.forEach(order -> order.set__internal_invoice_id(this.id));
        }
    }

}
