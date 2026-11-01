package io.github.geancgbr.orders.adapter.out.persistence;

import io.github.geancgbr.orders.domain.order.CustomerId;
import io.github.geancgbr.orders.domain.order.Order;
import io.github.geancgbr.orders.domain.order.OrderId;
import io.github.geancgbr.orders.domain.order.OrderRepository;
import io.github.geancgbr.orders.domain.order.OrderStatus;
import io.github.geancgbr.orders.domain.product.ProductId;
import io.github.geancgbr.orders.domain.shared.Money;
import io.github.geancgbr.orders.domain.shared.Quantity;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
class JpaOrderRepositoryAdapter implements OrderRepository {

    private final SpringDataOrderRepository jpa;

    JpaOrderRepositoryAdapter(SpringDataOrderRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Order save(Order order) {
        jpa.save(toEntity(order));
        return order;
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return jpa.findById(id.value()).map(JpaOrderRepositoryAdapter::toDomain);
    }

    private static OrderJpaEntity toEntity(Order order) {
        var items = order.items().stream()
                .map(item -> new OrderItemEmbeddable(
                        item.productId().value(),
                        item.unitPrice().amount(),
                        item.quantity().value()))
                .toList();
        return new OrderJpaEntity(
                order.id().value(), order.customerId().value(), order.status().name(), items);
    }

    private static Order toDomain(OrderJpaEntity entity) {
        var items = entity.items.stream()
                .map(item -> new Order.RestoredItem(
                        new ProductId(item.productId), new Money(item.unitPrice), new Quantity(item.quantity)))
                .toList();
        return Order.restore(
                new OrderId(entity.id), new CustomerId(entity.customerId), OrderStatus.valueOf(entity.status), items);
    }
}
