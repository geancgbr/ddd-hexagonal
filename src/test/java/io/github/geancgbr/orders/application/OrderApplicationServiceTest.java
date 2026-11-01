package io.github.geancgbr.orders.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.geancgbr.orders.application.service.NotFoundException;
import io.github.geancgbr.orders.application.service.OrderApplicationService;
import io.github.geancgbr.orders.domain.order.Coupon;
import io.github.geancgbr.orders.domain.order.Order;
import io.github.geancgbr.orders.domain.order.OrderEvents.OrderCreated;
import io.github.geancgbr.orders.domain.order.OrderEvents.OrderPlaced;
import io.github.geancgbr.orders.domain.order.OrderId;
import io.github.geancgbr.orders.domain.order.OrderStatus;
import io.github.geancgbr.orders.domain.order.ProductOrderingService;
import io.github.geancgbr.orders.domain.product.Product;
import io.github.geancgbr.orders.domain.shared.DomainException;
import io.github.geancgbr.orders.domain.shared.Money;
import io.github.geancgbr.orders.support.InMemoryAdapters;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderApplicationServiceTest {

    private final InMemoryAdapters.Orders orders = new InMemoryAdapters.Orders();
    private final InMemoryAdapters.Products products = new InMemoryAdapters.Products();
    private final InMemoryAdapters.Coupons coupons = new InMemoryAdapters.Coupons();
    private final InMemoryAdapters.RecordingPublisher publisher = new InMemoryAdapters.RecordingPublisher();
    private final OrderApplicationService service =
            new OrderApplicationService(orders, products, new ProductOrderingService(), coupons, publisher);

    @BeforeEach
    void setUp() {
        coupons.add(new Coupon("BEMVINDO10", 10));
    }

    @Test
    void full_flow_saves_the_order_and_publishes_each_event_once() {
        Product mouse = products.save(Product.register("Mouse", Money.of("89.90")));

        OrderId id = service.create("cliente-1");
        service.addItem(id, mouse.id().value(), 2);
        Order placed = service.place(id);

        assertThat(placed.status()).isEqualTo(OrderStatus.PLACED);
        assertThat(placed.total()).isEqualTo(Money.of("179.80"));
        assertThat(publisher.published()).hasSize(2);
        assertThat(publisher.published().get(0)).isInstanceOf(OrderCreated.class);
        assertThat(publisher.published().get(1)).isInstanceOf(OrderPlaced.class);
    }

    @Test
    void unknown_order_or_product_is_reported_as_not_found() {
        OrderId id = service.create("cliente-1");

        assertThatThrownBy(() -> service.get(OrderId.newId())).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.addItem(id, UUID.randomUUID(), 1)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void a_valid_coupon_reduces_the_total() {
        Product mouse = products.save(Product.register("Mouse", Money.of("100.00")));
        OrderId id = service.create("cliente-1");
        service.addItem(id, mouse.id().value(), 1);

        Order order = service.applyCoupon(id, "BEMVINDO10");

        assertThat(order.total()).isEqualTo(Money.of("90.00"));
    }

    @Test
    void an_unknown_coupon_is_a_business_error() {
        Product mouse = products.save(Product.register("Mouse", Money.of("100.00")));
        OrderId id = service.create("cliente-1");
        service.addItem(id, mouse.id().value(), 1);

        assertThatThrownBy(() -> service.applyCoupon(id, "NAOEXISTE"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Cupom inválido");
    }
}
