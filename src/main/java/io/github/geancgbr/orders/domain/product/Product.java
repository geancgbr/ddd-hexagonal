package io.github.geancgbr.orders.domain.product;

import io.github.geancgbr.orders.domain.shared.DomainException;
import io.github.geancgbr.orders.domain.shared.Money;
import java.util.Objects;

public class Product {

    private final ProductId id;
    private final String name;
    private Money price;
    private boolean active;

    private Product(ProductId id, String name, Money price, boolean active) {
        this.id = Objects.requireNonNull(id, "id");
        if (name == null || name.isBlank()) {
            throw new DomainException("Produto precisa de nome");
        }
        this.name = name.strip();
        this.price = Objects.requireNonNull(price, "price");
        this.active = active;
    }

    public static Product register(String name, Money price) {
        return new Product(ProductId.newId(), name, price, true);
    }

    public static Product restore(ProductId id, String name, Money price, boolean active) {
        return new Product(id, name, price, active);
    }

    public void changePrice(Money newPrice) {
        this.price = Objects.requireNonNull(newPrice, "newPrice");
    }

    public void deactivate() {
        this.active = false;
    }

    public ProductId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Money price() {
        return price;
    }

    public boolean isActive() {
        return active;
    }
}
