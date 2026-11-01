package io.github.geancgbr.orders.adapter.in.web;

import io.github.geancgbr.orders.domain.order.Coupon;
import io.github.geancgbr.orders.domain.order.Order;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

record OrderResponse(
        UUID id,
        String customerId,
        String status,
        List<Item> items,
        String couponCode,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal total) {

    record Item(UUID productId, BigDecimal unitPrice, int quantity, BigDecimal subtotal) {}

    static OrderResponse from(Order order) {
        var items = order.items().stream()
                .map(item -> new Item(
                        item.productId().value(),
                        item.unitPrice().amount(),
                        item.quantity().value(),
                        item.subtotal().amount()))
                .toList();
        return new OrderResponse(
                order.id().value(),
                order.customerId().value(),
                order.status().name(),
                items,
                order.coupon().map(Coupon::code).orElse(null),
                order.subtotal().amount(),
                order.discount().amount(),
                order.total().amount());
    }
}
