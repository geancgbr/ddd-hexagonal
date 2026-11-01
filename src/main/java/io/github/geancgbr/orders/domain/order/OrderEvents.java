package io.github.geancgbr.orders.domain.order;

import io.github.geancgbr.orders.domain.shared.DomainEvent;
import io.github.geancgbr.orders.domain.shared.Money;
import java.time.Instant;

public final class OrderEvents {

    private OrderEvents() {}

    public record OrderCreated(OrderId orderId, CustomerId customerId, Instant occurredAt) implements DomainEvent {}

    public record OrderPlaced(OrderId orderId, CustomerId customerId, Money total, int items, Instant occurredAt)
            implements DomainEvent {}

    public record OrderCancelled(OrderId orderId, String reason, Instant occurredAt) implements DomainEvent {}
}
