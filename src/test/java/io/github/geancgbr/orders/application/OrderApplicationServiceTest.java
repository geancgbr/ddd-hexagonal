package io.github.geancgbr.orders.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.geancgbr.orders.application.port.out.DomainEventPublisher;
import io.github.geancgbr.orders.application.service.NotFoundException;
import io.github.geancgbr.orders.application.service.OrderApplicationService;
import io.github.geancgbr.orders.domain.order.Order;
import io.github.geancgbr.orders.domain.order.OrderEvents.OrderCreated;
import io.github.geancgbr.orders.domain.order.OrderEvents.OrderPlaced;
import io.github.geancgbr.orders.domain.order.OrderId;
import io.github.geancgbr.orders.domain.order.OrderRepository;
import io.github.geancgbr.orders.domain.order.OrderStatus;
import io.github.geancgbr.orders.domain.order.ProductOrderingService;
import io.github.geancgbr.orders.domain.product.Product;
import io.github.geancgbr.orders.domain.product.ProductId;
import io.github.geancgbr.orders.domain.product.ProductRepository;
import io.github.geancgbr.orders.domain.shared.DomainEvent;
import io.github.geancgbr.orders.domain.shared.Money;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OrderApplicationServiceTest {

    static class InMemoryOrders implements OrderRepository {
        final Map<OrderId, Order> data = new HashMap<>();

        @Override
        public Order save(Order order) {
            data.put(order.id(), order);
            return order;
        }

        @Override
        public Optional<Order> findById(OrderId id) {
            return Optional.ofNullable(data.get(id));
        }
    }

    static class InMemoryProducts implements ProductRepository {
        final Map<ProductId, Product> data = new HashMap<>();

        @Override
        public Product save(Product product) {
            data.put(product.id(), product);
            return product;
        }

        @Override
        public Optional<Product> findById(ProductId id) {
            return Optional.ofNullable(data.get(id));
        }

        @Override
        public List<Product> findAll() {
            return List.copyOf(data.values());
        }
    }

    static class RecordingPublisher implements DomainEventPublisher {
        final List<DomainEvent> published = new ArrayList<>();

        @Override
        public void publish(List<DomainEvent> events) {
            published.addAll(events);
        }
    }

    private final InMemoryOrders orders = new InMemoryOrders();
    private final InMemoryProducts products = new InMemoryProducts();
    private final RecordingPublisher publisher = new RecordingPublisher();
    private final OrderApplicationService service =
            new OrderApplicationService(orders, products, new ProductOrderingService(), publisher);

    @Test
    void full_flow_saves_the_order_and_publishes_each_event_once() {
        Product mouse = products.save(Product.register("Mouse", Money.of("89.90")));

        OrderId id = service.create("cliente-1");
        service.addItem(id, mouse.id().value(), 2);
        Order placed = service.place(id);

        assertThat(placed.status()).isEqualTo(OrderStatus.PLACED);
        assertThat(placed.total()).isEqualTo(Money.of("179.80"));
        assertThat(publisher.published).hasSize(2);
        assertThat(publisher.published.get(0)).isInstanceOf(OrderCreated.class);
        assertThat(publisher.published.get(1)).isInstanceOf(OrderPlaced.class);
    }

    @Test
    void unknown_order_or_product_is_reported_as_not_found() {
        OrderId id = service.create("cliente-1");

        assertThatThrownBy(() -> service.get(OrderId.newId())).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.addItem(id, UUID.randomUUID(), 1)).isInstanceOf(NotFoundException.class);
    }
}
