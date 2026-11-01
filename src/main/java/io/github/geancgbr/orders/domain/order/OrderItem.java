package io.github.geancgbr.orders.domain.order;

import io.github.geancgbr.orders.domain.product.ProductId;
import io.github.geancgbr.orders.domain.shared.Money;
import io.github.geancgbr.orders.domain.shared.Quantity;
import java.util.Objects;

public final class OrderItem {

    private final ProductId productId;
    private final Money unitPrice;
    private Quantity quantity;

    OrderItem(ProductId productId, Money unitPrice, Quantity quantity) {
        this.productId = Objects.requireNonNull(productId, "productId");
        this.unitPrice = Objects.requireNonNull(unitPrice, "unitPrice");
        this.quantity = Objects.requireNonNull(quantity, "quantity");
    }

    void increase(Quantity more) {
        this.quantity = quantity.plus(more);
    }

    public ProductId productId() {
        return productId;
    }

    public Money unitPrice() {
        return unitPrice;
    }

    public Quantity quantity() {
        return quantity;
    }

    public Money subtotal() {
        return unitPrice.times(quantity.value());
    }
}
