package io.github.geancgbr.orders.domain.order;

import io.github.geancgbr.orders.domain.shared.DomainException;
import java.util.Locale;

public record Coupon(String code, int percentage) {

    public Coupon {
        if (code == null || code.isBlank()) {
            throw new DomainException("Cupom precisa de um código");
        }
        if (percentage < 1 || percentage > 50) {
            throw new DomainException("Desconto do cupom deve ficar entre 1% e 50%: " + percentage);
        }
        code = code.strip().toUpperCase(Locale.ROOT);
    }
}
