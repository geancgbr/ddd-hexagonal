package io.github.geancgbr.orders.adapter.in.web;

import io.github.geancgbr.orders.application.port.in.OrderUseCases.AddItemToOrder;
import io.github.geancgbr.orders.application.port.in.OrderUseCases.ApplyCoupon;
import io.github.geancgbr.orders.application.port.in.OrderUseCases.CancelOrder;
import io.github.geancgbr.orders.application.port.in.OrderUseCases.CreateOrder;
import io.github.geancgbr.orders.application.port.in.OrderUseCases.GetOrder;
import io.github.geancgbr.orders.application.port.in.OrderUseCases.PlaceOrder;
import io.github.geancgbr.orders.domain.order.OrderId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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

    record CreateOrderRequest(
            @NotBlank @Size(max = 100) @Pattern(regexp = InputRules.CODE, message = InputRules.CODE_MESSAGE)
            String customerId) {}

    record AddItemRequest(@NotNull UUID productId, @NotNull Integer quantity) {}

    record CancelRequest(
            @NotBlank @Size(max = 500) @Pattern(regexp = InputRules.FREE_TEXT, message = InputRules.FREE_TEXT_MESSAGE)
            String reason) {}

    record ApplyCouponRequest(
            @NotBlank @Size(max = 30) @Pattern(regexp = InputRules.CODE, message = InputRules.CODE_MESSAGE)
            String code) {}

    private final CreateOrder createOrder;
    private final AddItemToOrder addItem;
    private final ApplyCoupon applyCoupon;
    private final PlaceOrder placeOrder;
    private final CancelOrder cancelOrder;
    private final GetOrder getOrder;

    OrderController(
            CreateOrder createOrder,
            AddItemToOrder addItem,
            ApplyCoupon applyCoupon,
            PlaceOrder placeOrder,
            CancelOrder cancelOrder,
            GetOrder getOrder) {
        this.createOrder = createOrder;
        this.addItem = addItem;
        this.applyCoupon = applyCoupon;
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

    @PostMapping("/{id}/coupon")
    OrderResponse applyCoupon(@PathVariable UUID id, @Valid @RequestBody ApplyCouponRequest request) {
        return OrderResponse.from(applyCoupon.applyCoupon(new OrderId(id), request.code()));
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
