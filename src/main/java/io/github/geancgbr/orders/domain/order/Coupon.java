package io.github.geancgbr.orders.domain.order;

import io.github.geancgbr.orders.domain.shared.DomainException;
import java.util.Locale;

public record Coupon(String code, int percentage) {

    public static final int MIN_PERCENTAGE = 1;
    public static final int MAX_PERCENTAGE = 50;

    public Coupon {
        if (code == null || code.isBlank()) {
            throw new DomainException("Cupom precisa de um código");
        }
        if (percentage < MIN_PERCENTAGE || percentage > MAX_PERCENTAGE) {
            throw new DomainException("Desconto do cupom deve ficar entre %d%% e %d%%: %d"
                    .formatted(MIN_PERCENTAGE, MAX_PERCENTAGE, percentage));
        }
        code = code.strip().toUpperCase(Locale.ROOT);
    }
}
