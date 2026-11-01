package io.github.geancgbr.orders.application.port.in;

import io.github.geancgbr.orders.domain.order.Order;
import io.github.geancgbr.orders.domain.order.OrderId;
import java.util.UUID;

public final class OrderUseCases {

    private OrderUseCases() {}

    public interface CreateOrder {
        OrderId create(String customerId);
    }

    public interface AddItemToOrder {
        Order addItem(OrderId orderId, UUID productId, int quantity);
    }

    public interface PlaceOrder {
        Order place(OrderId orderId);
    }

    public interface CancelOrder {
        Order cancel(OrderId orderId, String reason);
    }

    public interface GetOrder {
        Order get(OrderId orderId);
    }
}
