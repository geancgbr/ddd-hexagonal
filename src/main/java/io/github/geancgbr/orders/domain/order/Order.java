package io.github.geancgbr.orders.domain.order;

import io.github.geancgbr.orders.domain.order.OrderEvents.OrderCancelled;
import io.github.geancgbr.orders.domain.order.OrderEvents.OrderCreated;
import io.github.geancgbr.orders.domain.order.OrderEvents.OrderPlaced;
import io.github.geancgbr.orders.domain.product.ProductId;
import io.github.geancgbr.orders.domain.shared.DomainEvent;
import io.github.geancgbr.orders.domain.shared.DomainException;
import io.github.geancgbr.orders.domain.shared.Money;
import io.github.geancgbr.orders.domain.shared.Quantity;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class Order {

    public static final int MAX_DISTINCT_PRODUCTS = 10;

    private final OrderId id;
    private final CustomerId customerId;
    private OrderStatus status;
    private Coupon coupon;
    private final List<OrderItem> items;
    private final List<DomainEvent> events = new ArrayList<>();

    private Order(OrderId id, CustomerId customerId, OrderStatus status, List<OrderItem> items) {
        this.id = Objects.requireNonNull(id, "id");
        this.customerId = Objects.requireNonNull(customerId, "customerId");
        this.status = Objects.requireNonNull(status, "status");
        this.items = new ArrayList<>(items);
    }

    public static Order create(CustomerId customerId) {
        Order order = new Order(OrderId.newId(), customerId, OrderStatus.DRAFT, List.of());
        order.events.add(new OrderCreated(order.id, customerId, Instant.now()));
        return order;
    }

    public static Order restore(
            OrderId id, CustomerId customerId, OrderStatus status, List<RestoredItem> items, Coupon coupon) {
        List<OrderItem> restored = items.stream()
                .map(item -> new OrderItem(item.productId(), item.unitPrice(), item.quantity()))
                .toList();
        Order order = new Order(id, customerId, status, restored);
        order.coupon = coupon;
        return order;
    }

    public record RestoredItem(ProductId productId, Money unitPrice, Quantity quantity) {}

    public void addItem(ProductId productId, Money unitPrice, Quantity quantity) {
        requireStatus(OrderStatus.DRAFT, "Só é possível alterar itens de um pedido em rascunho");
        Optional<OrderItem> existing = findItem(productId);
        if (existing.isPresent()) {
            existing.get().increase(quantity);
            return;
        }
        if (items.size() >= MAX_DISTINCT_PRODUCTS) {
            throw new DomainException("Pedido aceita no máximo " + MAX_DISTINCT_PRODUCTS + " produtos diferentes");
        }
        items.add(new OrderItem(productId, unitPrice, quantity));
    }

    public void place() {
        requireStatus(OrderStatus.DRAFT, "Só um pedido em rascunho pode ser fechado");
        requireItems("Pedido sem itens não pode ser fechado");
        status = OrderStatus.PLACED;
        events.add(new OrderPlaced(id, customerId, total(), items.size(), Instant.now()));
    }

    public void cancel(String reason) {
        if (status == OrderStatus.CANCELLED) {
            throw new DomainException("Pedido já está cancelado");
        }
        if (reason == null || reason.isBlank()) {
            throw new DomainException("Cancelamento precisa de um motivo");
        }
        status = OrderStatus.CANCELLED;
        events.add(new OrderCancelled(id, reason.strip(), Instant.now()));
    }

    public void applyCoupon(Coupon coupon) {
        requireStatus(OrderStatus.DRAFT, "Só é possível aplicar cupom em um pedido em rascunho");
        requireItems("Pedido sem itens não aceita cupom");
        if (this.coupon != null) {
            throw new DomainException("Pedido já tem um cupom: " + this.coupon.code());
        }
        this.coupon = Objects.requireNonNull(coupon, "coupon");
    }

    public Optional<Coupon> coupon() {
        return Optional.ofNullable(coupon);
    }

    public Money subtotal() {
        return items.stream().map(OrderItem::subtotal).reduce(Money.ZERO, Money::plus);
    }

    public Money discount() {
        return coupon().map(applied -> subtotal().percentage(applied.percentage()))
                .orElse(Money.ZERO);
    }

    public Money total() {
        return subtotal().minus(discount());
    }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> pulled = List.copyOf(events);
        events.clear();
        return pulled;
    }

    private Optional<OrderItem> findItem(ProductId productId) {
        return items.stream().filter(item -> item.productId().equals(productId)).findFirst();
    }

    private void requireItems(String message) {
        if (items.isEmpty()) {
            throw new DomainException(message);
        }
    }

    private void requireStatus(OrderStatus expected, String message) {
        if (status != expected) {
            throw new DomainException(message + " (status atual: " + status + ")");
        }
    }

    public OrderId id() {
        return id;
    }

    public CustomerId customerId() {
        return customerId;
    }

    public OrderStatus status() {
        return status;
    }

    public List<OrderItem> items() {
        return Collections.unmodifiableList(items);
    }
}
