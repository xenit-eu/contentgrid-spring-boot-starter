package com.contentgrid.spring.test.fixture.invoicing.model;

import com.contentgrid.spring.data.rest.validation.OnEntityUpdate;
import com.contentgrid.spring.querydsl.annotation.CollectionFilterParam;
import com.contentgrid.spring.querydsl.predicate.EntityId;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.data.rest.core.annotation.RestResource;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class PromotionCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty(access = Access.READ_ONLY)
    private UUID id;

    @Column(updatable = false, nullable = false)
    @CollectionFilterParam(value = "promo_code")
    @NotNull(groups = OnEntityUpdate.class)
    private String promoCode;

    String description;

    @ManyToMany(mappedBy = "promos")
    @RestResource(exported = false)
    @CollectionFilterParam(predicate = EntityId.class, documented = false)
    private Set<Order> orders = new HashSet<>();

    @ManyToMany(mappedBy = "manualPromos")
    @RestResource(exported = false)
    @CollectionFilterParam(predicate = EntityId.class, documented = false)
    private Set<Order> ordersWithManualPromos = new HashSet<>();

    public PromotionCampaign(String promoCode, String description) {
        this.promoCode = promoCode;
        this.description = description;
    }

    public void addOrder(@NonNull Order order) {
        if (this.orders.add(order)) {
            order.addPromo(this);
        }
    }
}
