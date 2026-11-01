package io.github.geancgbr.orders.adapter.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.geancgbr.orders.TestcontainersConfiguration;
import io.github.geancgbr.orders.domain.order.Coupon;
import io.github.geancgbr.orders.domain.order.CustomerId;
import io.github.geancgbr.orders.domain.order.Order;
import io.github.geancgbr.orders.domain.order.OrderId;
import io.github.geancgbr.orders.domain.order.OrderRepository;
import io.github.geancgbr.orders.domain.order.OrderStatus;
import io.github.geancgbr.orders.domain.product.ProductId;
import io.github.geancgbr.orders.domain.shared.Money;
import io.github.geancgbr.orders.domain.shared.Quantity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class JpaOrderRepositoryAdapterTest {

    @Autowired
    private OrderRepository repository;

    @Test
    void an_order_survives_the_round_trip_to_postgresql() {
        ProductId keyboard = ProductId.newId();
        ProductId mouse = ProductId.newId();
        Order order = Order.create(new CustomerId("cliente-9"));
        order.addItem(keyboard, Money.of("300.00"), new Quantity(1));
        order.addItem(mouse, Money.of("50.00"), new Quantity(3));
        order.applyCoupon(new Coupon("BEMVINDO10", 10));
        order.place();

        repository.save(order);
        Order loaded = repository.findById(order.id()).orElseThrow();

        assertThat(loaded.status()).isEqualTo(OrderStatus.PLACED);
        assertThat(loaded.customerId()).isEqualTo(new CustomerId("cliente-9"));
        assertThat(loaded.coupon()).contains(new Coupon("BEMVINDO10", 10));
        assertThat(loaded.items())
                .extracting(item -> item.productId(), item -> item.quantity().value())
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(keyboard, 1),
                        org.assertj.core.groups.Tuple.tuple(mouse, 3));
        assertThat(loaded.total()).isEqualTo(Money.of("405.00"));
    }

    @Test
    void an_unknown_id_returns_empty() {
        assertThat(repository.findById(OrderId.newId())).isEmpty();
    }
}
