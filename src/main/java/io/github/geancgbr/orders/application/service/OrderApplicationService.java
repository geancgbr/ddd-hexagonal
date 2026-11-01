package io.github.geancgbr.orders.application.service;

import io.github.geancgbr.orders.application.port.in.OrderUseCases.AddItemToOrder;
import io.github.geancgbr.orders.application.port.in.OrderUseCases.CancelOrder;
import io.github.geancgbr.orders.application.port.in.OrderUseCases.CreateOrder;
import io.github.geancgbr.orders.application.port.in.OrderUseCases.GetOrder;
import io.github.geancgbr.orders.application.port.in.OrderUseCases.PlaceOrder;
import io.github.geancgbr.orders.application.port.out.DomainEventPublisher;
import io.github.geancgbr.orders.domain.order.CustomerId;
import io.github.geancgbr.orders.domain.order.Order;
import io.github.geancgbr.orders.domain.order.OrderId;
import io.github.geancgbr.orders.domain.order.OrderRepository;
import io.github.geancgbr.orders.domain.order.ProductOrderingService;
import io.github.geancgbr.orders.domain.product.Product;
import io.github.geancgbr.orders.domain.product.ProductId;
import io.github.geancgbr.orders.domain.product.ProductRepository;
import io.github.geancgbr.orders.domain.shared.Quantity;
import java.util.UUID;

public class OrderApplicationService implements CreateOrder, AddItemToOrder, PlaceOrder, CancelOrder, GetOrder {

    private final OrderRepository orders;
    private final ProductRepository products;
    private final ProductOrderingService ordering;
    private final DomainEventPublisher events;

    public OrderApplicationService(
            OrderRepository orders,
            ProductRepository products,
            ProductOrderingService ordering,
            DomainEventPublisher events) {
        this.orders = orders;
        this.products = products;
        this.ordering = ordering;
        this.events = events;
    }

    @Override
    public OrderId create(String customerId) {
        Order order = Order.create(new CustomerId(customerId));
        saveAndPublish(order);
        return order.id();
    }

    @Override
    public Order addItem(OrderId orderId, UUID productId, int quantity) {
        Order order = load(orderId);
        Product product = products.findById(new ProductId(productId))
                .orElseThrow(() -> new NotFoundException("Produto não encontrado: " + productId));
        ordering.addToOrder(order, product, new Quantity(quantity));
        return saveAndPublish(order);
    }

    @Override
    public Order place(OrderId orderId) {
        Order order = load(orderId);
        order.place();
        return saveAndPublish(order);
    }

    @Override
    public Order cancel(OrderId orderId, String reason) {
        Order order = load(orderId);
        order.cancel(reason);
        return saveAndPublish(order);
    }

    @Override
    public Order get(OrderId orderId) {
        return load(orderId);
    }

    private Order load(OrderId orderId) {
        return orders.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Pedido não encontrado: " + orderId.value()));
    }

    private Order saveAndPublish(Order order) {
        orders.save(order);
        events.publish(order.pullEvents());
        return order;
    }
}
