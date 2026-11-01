package io.github.geancgbr.orders.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.geancgbr.orders.domain.order.Coupon;
import io.github.geancgbr.orders.domain.shared.DomainException;
import org.junit.jupiter.api.Test;

class CouponTest {

    @Test
    void code_is_normalized_to_upper_case() {
        Coupon coupon = new Coupon(" bemvindo10 ", 10);

        assertThat(coupon.code()).isEqualTo("BEMVINDO10");
    }

    @Test
    void code_is_required() {
        assertThatThrownBy(() -> new Coupon(" ", 10)).isInstanceOf(DomainException.class);
    }

    @Test
    void percentage_must_be_between_1_and_50() {
        assertThatThrownBy(() -> new Coupon("ZERO", 0)).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> new Coupon("METADE_E_UM", 51)).isInstanceOf(DomainException.class);
        assertThat(new Coupon("MAXIMO", 50).percentage()).isEqualTo(50);
    }
}
