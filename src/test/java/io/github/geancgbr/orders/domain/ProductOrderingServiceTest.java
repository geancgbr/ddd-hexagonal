package io.github.geancgbr.orders.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.geancgbr.orders.domain.order.CustomerId;
import io.github.geancgbr.orders.domain.order.Order;
import io.github.geancgbr.orders.domain.order.ProductOrderingService;
import io.github.geancgbr.orders.domain.product.Product;
import io.github.geancgbr.orders.domain.shared.DomainException;
import io.github.geancgbr.orders.domain.shared.Money;
import io.github.geancgbr.orders.domain.shared.Quantity;
import org.junit.jupiter.api.Test;

class ProductOrderingServiceTest {

    private final ProductOrderingService service = new ProductOrderingService();

    @Test
    void the_item_enters_the_order_with_the_catalog_price() {
        Product keyboard = Product.register("Teclado", Money.of("199.90"));
        Order order = Order.create(new CustomerId("cliente-1"));

        service.addToOrder(order, keyboard, new Quantity(1));

        assertThat(order.items())
                .singleElement()
                .satisfies(item -> assertThat(item.unitPrice()).isEqualTo(Money.of("199.90")));
    }

    @Test
    void an_inactive_product_cannot_be_ordered() {
        Product keyboard = Product.register("Teclado", Money.of("199.90"));
        keyboard.deactivate();
        Order order = Order.create(new CustomerId("cliente-1"));

        assertThatThrownBy(() -> service.addToOrder(order, keyboard, new Quantity(1)))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("indisponível");
        assertThat(order.items()).isEmpty();
    }
}
