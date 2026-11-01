package io.github.geancgbr.orders.domain.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Money(BigDecimal amount) {

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    public Money {
        Objects.requireNonNull(amount, "amount");
        if (amount.signum() < 0) {
            throw new DomainException("Valor monetário não pode ser negativo: " + amount);
        }
        amount = amount.setScale(2, RoundingMode.HALF_EVEN);
    }

    public static Money of(String amount) {
        return new Money(new BigDecimal(amount));
    }

    public Money plus(Money other) {
        return new Money(amount.add(other.amount));
    }

    public Money times(int factor) {
        return new Money(amount.multiply(BigDecimal.valueOf(factor)));
    }

    public Money minus(Money other) {
        return new Money(amount.subtract(other.amount));
    }

    public Money percentage(int percent) {
        return new Money(amount.multiply(BigDecimal.valueOf(percent)).movePointLeft(2));
    }
}
