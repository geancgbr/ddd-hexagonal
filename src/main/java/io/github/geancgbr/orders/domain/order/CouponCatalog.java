package io.github.geancgbr.orders.domain.order;

import java.util.Optional;

public interface CouponCatalog {

    Optional<Coupon> findByCode(String code);
}
