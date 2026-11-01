package io.github.geancgbr.orders.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.geancgbr.orders.domain.order.CustomerId;
import io.github.geancgbr.orders.domain.order.Order;
import io.github.geancgbr.orders.domain.order.OrderEvents.OrderCancelled;
import io.github.geancgbr.orders.domain.order.OrderEvents.OrderCreated;
import io.github.geancgbr.orders.domain.order.OrderEvents.OrderPlaced;
import io.github.geancgbr.orders.domain.order.OrderStatus;
import io.github.geancgbr.orders.domain.product.ProductId;
import io.github.geancgbr.orders.domain.shared.DomainException;
import io.github.geancgbr.orders.domain.shared.Money;
import io.github.geancgbr.orders.domain.shared.Quantity;
import org.junit.jupiter.api.Test;

class OrderTest {

    private final ProductId book = ProductId.newId();
    private final ProductId pen = ProductId.newId();

    private Order newOrder() {
        Order order = Order.create(new CustomerId("cliente-1"));
        order.pullEvents();
        return order;
    }

    @Test
    void a_new_order_starts_as_draft_and_announces_its_creation() {
        Order order = Order.create(new CustomerId("cliente-1"));

        assertThat(order.status()).isEqualTo(OrderStatus.DRAFT);
        assertThat(order.pullEvents()).singleElement().isInstanceOf(OrderCreated.class);
        assertThat(order.pullEvents()).isEmpty();
    }

    @Test
    void total_is_the_sum_of_the_items() {
        Order order = newOrder();
        order.addItem(book, Money.of("50.00"), new Quantity(2));
        order.addItem(pen, Money.of("3.50"), new Quantity(4));

        assertThat(order.total()).isEqualTo(Money.of("114.00"));
    }

    @Test
    void adding_the_same_product_again_increases_quantity_and_keeps_the_original_price() {
        Order order = newOrder();
        order.addItem(book, Money.of("50.00"), new Quantity(1));
        order.addItem(book, Money.of("80.00"), new Quantity(2));

        assertThat(order.items()).singleElement().satisfies(item -> {
            assertThat(item.quantity()).isEqualTo(new Quantity(3));
            assertThat(item.unitPrice()).isEqualTo(Money.of("50.00"));
        });
    }

    @Test
    void an_order_accepts_at_most_ten_distinct_products() {
        Order order = newOrder();
        for (int i = 0; i < Order.MAX_DISTINCT_PRODUCTS; i++) {
            order.addItem(ProductId.newId(), Money.of("1.00"), new Quantity(1));
        }

        assertThatThrownBy(() -> order.addItem(ProductId.newId(), Money.of("1.00"), new Quantity(1)))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void an_empty_order_cannot_be_placed() {
        assertThatThrownBy(() -> newOrder().place()).isInstanceOf(DomainException.class);
    }

    @Test
    void placing_an_order_freezes_its_items_and_publishes_the_total() {
        Order order = newOrder();
        order.addItem(book, Money.of("50.00"), new Quantity(2));

        order.place();

        assertThat(order.status()).isEqualTo(OrderStatus.PLACED);
        assertThat(order.pullEvents())
                .singleElement()
                .isInstanceOfSatisfying(
                        OrderPlaced.class, event -> assertThat(event.total()).isEqualTo(Money.of("100.00")));
        assertThatThrownBy(() -> order.addItem(pen, Money.of("1.00"), new Quantity(1)))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void cancelling_requires_a_reason_and_happens_only_once() {
        Order order = newOrder();

        assertThatThrownBy(() -> order.cancel(" ")).isInstanceOf(DomainException.class);
        order.cancel("cliente desistiu");

        assertThat(order.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.pullEvents()).singleElement().isInstanceOf(OrderCancelled.class);
        assertThatThrownBy(() -> order.cancel("de novo")).isInstanceOf(DomainException.class);
    }

    @Test
    void the_items_list_cannot_be_changed_from_outside_the_aggregate() {
        Order order = newOrder();
        order.addItem(book, Money.of("10.00"), new Quantity(1));

        assertThatThrownBy(() -> order.items().clear()).isInstanceOf(UnsupportedOperationException.class);
    }
}
