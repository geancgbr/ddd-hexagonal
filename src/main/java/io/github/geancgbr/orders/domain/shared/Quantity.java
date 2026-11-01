package io.github.geancgbr.orders.domain.shared;

public record Quantity(int value) {

    public static final int MAX = 99;

    public Quantity {
        if (value < 1 || value > MAX) {
            throw new DomainException("Quantidade deve ficar entre 1 e " + MAX + ": " + value);
        }
    }

    public Quantity plus(Quantity other) {
        return new Quantity(value + other.value);
    }
}
