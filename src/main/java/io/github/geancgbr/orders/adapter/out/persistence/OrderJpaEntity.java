package io.github.geancgbr.orders.adapter.out.persistence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
class OrderJpaEntity {

    @Id
    UUID id;

    @Column(name = "customer_id", nullable = false)
    String customerId;

    @Column(nullable = false)
    String status;

    @Column(name = "coupon_code")
    String couponCode;

    @Column(name = "coupon_percentage")
    Integer couponPercentage;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    List<OrderItemEmbeddable> items = new ArrayList<>();

    protected OrderJpaEntity() {}

    OrderJpaEntity(
            UUID id,
            String customerId,
            String status,
            String couponCode,
            Integer couponPercentage,
            List<OrderItemEmbeddable> items) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.couponCode = couponCode;
        this.couponPercentage = couponPercentage;
        this.items = new ArrayList<>(items);
    }
}
