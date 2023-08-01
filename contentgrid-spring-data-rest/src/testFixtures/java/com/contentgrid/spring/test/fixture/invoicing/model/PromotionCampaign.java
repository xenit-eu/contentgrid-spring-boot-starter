package com.contentgrid.spring.test.fixture.invoicing.model;

import com.contentgrid.spring.querydsl.annotation.CollectionFilterParam;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
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
    @RestResource(rel = "d:promo-code")
    private String promoCode;

    String description;

    @ManyToMany(mappedBy = "promos")
    @RestResource(rel = "d:orders")
    private Set<Order> orders = new HashSet<>();

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
