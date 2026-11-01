package io.github.geancgbr.orders.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.geancgbr.orders.domain.order.Coupon;
import io.github.geancgbr.orders.domain.order.CustomerId;
import io.github.geancgbr.orders.domain.order.Order;
import io.github.geancgbr.orders.domain.order.OrderEvents.OrderPlaced;
import io.github.geancgbr.orders.domain.product.ProductId;
import io.github.geancgbr.orders.domain.shared.DomainException;
import io.github.geancgbr.orders.domain.shared.Money;
import io.github.geancgbr.orders.domain.shared.Quantity;
import org.junit.jupiter.api.Test;

class OrderCouponTest {

    private Order orderWith(String unitPrice, int quantity) {
        Order order = Order.create(new CustomerId("cliente-1"));
        order.pullEvents();
        order.addItem(ProductId.newId(), Money.of(unitPrice), new Quantity(quantity));
        return order;
    }

    @Test
    void without_coupon_the_total_is_the_subtotal() {
        Order order = orderWith("100.00", 2);

        assertThat(order.subtotal()).isEqualTo(Money.of("200.00"));
        assertThat(order.discount()).isEqualTo(Money.ZERO);
        assertThat(order.total()).isEqualTo(Money.of("200.00"));
    }

    @Test
    void the_coupon_discounts_a_percentage_of_the_subtotal() {
        Order order = orderWith("100.00", 2);

        order.applyCoupon(new Coupon("DESC10", 10));

        assertThat(order.coupon()).contains(new Coupon("DESC10", 10));
        assertThat(order.discount()).isEqualTo(Money.of("20.00"));
        assertThat(order.total()).isEqualTo(Money.of("180.00"));
    }

    @Test
    void the_discount_is_rounded_to_cents() {
        Order order = orderWith("33.33", 3);

        order.applyCoupon(new Coupon("DESC15", 15));

        assertThat(order.discount()).isEqualTo(Money.of("15.00"));
        assertThat(order.total()).isEqualTo(Money.of("84.99"));
    }

    @Test
    void the_placed_event_carries_the_total_with_discount() {
        Order order = orderWith("100.00", 2);
        order.applyCoupon(new Coupon("DESC10", 10));

        order.place();

        assertThat(order.pullEvents())
                .singleElement()
                .isInstanceOfSatisfying(
                        OrderPlaced.class, event -> assertThat(event.total()).isEqualTo(Money.of("180.00")));
    }

    @Test
    void only_one_coupon_per_order() {
        Order order = orderWith("100.00", 2);
        order.applyCoupon(new Coupon("DESC10", 10));

        assertThatThrownBy(() -> order.applyCoupon(new Coupon("DESC20", 20)))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("já tem um cupom");
        assertThat(order.discount()).isEqualTo(Money.of("20.00"));
    }

    @Test
    void a_placed_order_does_not_accept_coupons() {
        Order order = orderWith("100.00", 2);
        order.place();

        assertThatThrownBy(() -> order.applyCoupon(new Coupon("DESC10", 10))).isInstanceOf(DomainException.class);
    }

    @Test
    void an_empty_order_does_not_accept_coupons() {
        Order order = Order.create(new CustomerId("cliente-1"));

        assertThatThrownBy(() -> order.applyCoupon(new Coupon("DESC10", 10)))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("sem itens");
    }
}
