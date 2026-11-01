package io.github.geancgbr.orders.adapter.in.web;

import io.github.geancgbr.orders.application.port.in.OrderUseCases.AddItemToOrder;
import io.github.geancgbr.orders.application.port.in.OrderUseCases.CancelOrder;
import io.github.geancgbr.orders.application.port.in.OrderUseCases.CreateOrder;
import io.github.geancgbr.orders.application.port.in.OrderUseCases.GetOrder;
import io.github.geancgbr.orders.application.port.in.OrderUseCases.PlaceOrder;
import io.github.geancgbr.orders.domain.order.OrderId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
class OrderController {

    record CreateOrderRequest(@NotBlank String customerId) {}

    record AddItemRequest(@NotNull UUID productId, @NotNull Integer quantity) {}

    record CancelRequest(@NotBlank String reason) {}

    private final CreateOrder createOrder;
    private final AddItemToOrder addItem;
    private final PlaceOrder placeOrder;
    private final CancelOrder cancelOrder;
    private final GetOrder getOrder;

    OrderController(
            CreateOrder createOrder,
            AddItemToOrder addItem,
            PlaceOrder placeOrder,
            CancelOrder cancelOrder,
            GetOrder getOrder) {
        this.createOrder = createOrder;
        this.addItem = addItem;
        this.placeOrder = placeOrder;
        this.cancelOrder = cancelOrder;
        this.getOrder = getOrder;
    }

    @PostMapping
    ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        OrderId id = createOrder.create(request.customerId());
        return ResponseEntity.created(URI.create("/orders/" + id.value())).body(OrderResponse.from(getOrder.get(id)));
    }

    @GetMapping("/{id}")
    OrderResponse get(@PathVariable UUID id) {
        return OrderResponse.from(getOrder.get(new OrderId(id)));
    }

    @PostMapping("/{id}/items")
    OrderResponse addItem(@PathVariable UUID id, @Valid @RequestBody AddItemRequest request) {
        return OrderResponse.from(addItem.addItem(new OrderId(id), request.productId(), request.quantity()));
    }

    @PostMapping("/{id}/place")
    OrderResponse place(@PathVariable UUID id) {
        return OrderResponse.from(placeOrder.place(new OrderId(id)));
    }

    @PostMapping("/{id}/cancel")
    OrderResponse cancel(@PathVariable UUID id, @Valid @RequestBody CancelRequest request) {
        return OrderResponse.from(cancelOrder.cancel(new OrderId(id), request.reason()));
    }
}
