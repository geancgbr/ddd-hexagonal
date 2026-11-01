package io.github.geancgbr.orders.domain.order;

import io.github.geancgbr.orders.domain.shared.DomainException;

public record CustomerId(String value) {

    public CustomerId {
        if (value == null || value.isBlank()) {
            throw new DomainException("Pedido precisa de um cliente");
        }
        value = value.strip();
    }
}
