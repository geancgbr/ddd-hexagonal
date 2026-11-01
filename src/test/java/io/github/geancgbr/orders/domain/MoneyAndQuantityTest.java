package io.github.geancgbr.orders.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.geancgbr.orders.domain.shared.DomainException;
import io.github.geancgbr.orders.domain.shared.Money;
import io.github.geancgbr.orders.domain.shared.Quantity;
import org.junit.jupiter.api.Test;

class MoneyAndQuantityTest {

    @Test
    void money_is_compared_by_value_not_by_reference() {
        assertThat(Money.of("10.5")).isEqualTo(Money.of("10.50"));
    }

    @Test
    void money_operations_return_new_values() {
        Money ten = Money.of("10.00");
        Money twenty = ten.times(2);

        assertThat(twenty).isEqualTo(Money.of("20.00"));
        assertThat(ten).isEqualTo(Money.of("10.00"));
        assertThat(ten.plus(twenty)).isEqualTo(Money.of("30.00"));
    }

    @Test
    void money_cannot_be_negative() {
        assertThatThrownBy(() -> Money.of("-1")).isInstanceOf(DomainException.class);
    }

    @Test
    void quantity_must_be_between_1_and_99() {
        assertThatThrownBy(() -> new Quantity(0)).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> new Quantity(100)).isInstanceOf(DomainException.class);
        assertThat(new Quantity(1).plus(new Quantity(2))).isEqualTo(new Quantity(3));
    }
}
