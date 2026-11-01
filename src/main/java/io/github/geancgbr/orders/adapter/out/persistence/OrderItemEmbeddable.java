package io.github.geancgbr.orders.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.UUID;

@Embeddable
class OrderItemEmbeddable {

    @Column(name = "product_id", nullable = false)
    UUID productId;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    BigDecimal unitPrice;

    @Column(nullable = false)
    int quantity;

    protected OrderItemEmbeddable() {}

    OrderItemEmbeddable(UUID productId, BigDecimal unitPrice, int quantity) {
        this.productId = productId;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }
}
