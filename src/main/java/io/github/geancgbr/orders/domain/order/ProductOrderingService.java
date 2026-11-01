package io.github.geancgbr.orders.domain.order;

import io.github.geancgbr.orders.domain.product.Product;
import io.github.geancgbr.orders.domain.shared.DomainException;
import io.github.geancgbr.orders.domain.shared.Quantity;

public class ProductOrderingService {

    public void addToOrder(Order order, Product product, Quantity quantity) {
        if (!product.isActive()) {
            throw new DomainException("Produto indisponível: " + product.name());
        }
        order.addItem(product.id(), product.price(), quantity);
    }
}
