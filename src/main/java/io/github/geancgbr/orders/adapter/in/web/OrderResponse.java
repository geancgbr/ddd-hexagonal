package io.github.geancgbr.orders.adapter.in.web;

import io.github.geancgbr.orders.domain.order.Order;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

record OrderResponse(UUID id, String customerId, String status, List<Item> items, BigDecimal total) {

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
                order.total().amount());
    }
}
