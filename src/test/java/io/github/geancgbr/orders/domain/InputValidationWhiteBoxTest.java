package io.github.geancgbr.orders.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.geancgbr.orders.domain.order.Coupon;
import io.github.geancgbr.orders.domain.order.CustomerId;
import io.github.geancgbr.orders.domain.order.Order;
import io.github.geancgbr.orders.domain.product.Product;
import io.github.geancgbr.orders.domain.shared.DomainException;
import io.github.geancgbr.orders.domain.shared.Money;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class InputValidationWhiteBoxTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void customer_id_rejects_null_and_blank(String value) {
        assertThatThrownBy(() -> new CustomerId(value)).isInstanceOf(DomainException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void product_name_rejects_null_and_blank(String name) {
        assertThatThrownBy(() -> Product.register(name, Money.of("1.00"))).isInstanceOf(DomainException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void coupon_code_rejects_null_and_empty(String code) {
        assertThatThrownBy(() -> new Coupon(code, 10)).isInstanceOf(DomainException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void cancel_reason_rejects_null_and_empty(String reason) {
        Order order = Order.create(new CustomerId("cliente-1"));

        assertThatThrownBy(() -> order.cancel(reason)).isInstanceOf(DomainException.class);
    }
}
