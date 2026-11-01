package io.github.geancgbr.orders.support;

import io.github.geancgbr.orders.application.port.out.DomainEventPublisher;
import io.github.geancgbr.orders.domain.order.Coupon;
import io.github.geancgbr.orders.domain.order.CouponCatalog;
import io.github.geancgbr.orders.domain.order.Order;
import io.github.geancgbr.orders.domain.order.OrderId;
import io.github.geancgbr.orders.domain.order.OrderRepository;
import io.github.geancgbr.orders.domain.product.Product;
import io.github.geancgbr.orders.domain.product.ProductId;
import io.github.geancgbr.orders.domain.product.ProductRepository;
import io.github.geancgbr.orders.domain.shared.DomainEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class InMemoryAdapters {

    private InMemoryAdapters() {}

    public static final class Orders implements OrderRepository {

        private final Map<OrderId, Order> data = new HashMap<>();

        @Override
        public Order save(Order order) {
            data.put(order.id(), order);
            return order;
        }

        @Override
        public Optional<Order> findById(OrderId id) {
            return Optional.ofNullable(data.get(id));
        }
    }

    public static final class Products implements ProductRepository {

        private final Map<ProductId, Product> data = new HashMap<>();

        @Override
        public Product save(Product product) {
            data.put(product.id(), product);
            return product;
        }

        @Override
        public Optional<Product> findById(ProductId id) {
            return Optional.ofNullable(data.get(id));
        }

        @Override
        public List<Product> findAll() {
            return List.copyOf(data.values());
        }
    }

    public static final class Coupons implements CouponCatalog {

        private final Map<String, Coupon> data = new HashMap<>();

        public void add(Coupon coupon) {
            data.put(coupon.code(), coupon);
        }

        @Override
        public Optional<Coupon> findByCode(String code) {
            return Optional.ofNullable(data.get(code));
        }
    }

    public static final class RecordingPublisher implements DomainEventPublisher {

        private final List<DomainEvent> published = new ArrayList<>();

        @Override
        public void publish(List<DomainEvent> events) {
            published.addAll(events);
        }

        public List<DomainEvent> published() {
            return List.copyOf(published);
        }
    }
}
