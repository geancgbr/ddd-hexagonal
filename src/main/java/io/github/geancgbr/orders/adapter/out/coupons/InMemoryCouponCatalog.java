package io.github.geancgbr.orders.adapter.out.coupons;

import io.github.geancgbr.orders.domain.order.Coupon;
import io.github.geancgbr.orders.domain.order.CouponCatalog;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
class InMemoryCouponCatalog implements CouponCatalog {

    private final Map<String, Coupon> coupons = Stream.of(new Coupon("BEMVINDO10", 10), new Coupon("BLACKFRIDAY25", 25))
            .collect(Collectors.toUnmodifiableMap(Coupon::code, Function.identity()));

    @Override
    public Optional<Coupon> findByCode(String code) {
        return Optional.ofNullable(code).map(value -> coupons.get(value.strip().toUpperCase(Locale.ROOT)));
    }
}
