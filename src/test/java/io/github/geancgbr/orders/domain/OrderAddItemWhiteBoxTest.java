package io.github.geancgbr.orders.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.geancgbr.orders.domain.order.CustomerId;
import io.github.geancgbr.orders.domain.order.Order;
import io.github.geancgbr.orders.domain.product.ProductId;
import io.github.geancgbr.orders.domain.shared.DomainException;
import io.github.geancgbr.orders.domain.shared.Money;
import io.github.geancgbr.orders.domain.shared.Quantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Order.addItem: um teste por caminho do código")
class OrderAddItemWhiteBoxTest {

    private final ProductId product = ProductId.newId();
    private final Money price = Money.of("10.00");
    private final Quantity one = new Quantity(1);

    private Order draft() {
        return Order.create(new CustomerId("cliente-1"));
    }

    @Nested
    @DisplayName("caminho 1: status diferente de DRAFT")
    class NotDraft {

        @Test
        void throws_before_touching_the_items() {
            Order order = draft();
            order.addItem(product, price, one);
            order.place();

            assertThatThrownBy(() -> order.addItem(ProductId.newId(), price, one))
                    .isInstanceOf(DomainException.class);
            assertThat(order.items()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("caminho 2: produto já existe no pedido")
    class ExistingProduct {

        @Test
        void increases_the_quantity_and_returns_early() {
            Order order = draft();
            order.addItem(product, price, one);

            order.addItem(product, Money.of("99.00"), one);

            assertThat(order.items()).singleElement().satisfies(item -> {
                assertThat(item.quantity()).isEqualTo(new Quantity(2));
                assertThat(item.unitPrice()).isEqualTo(price);
            });
        }
    }

    @Nested
    @DisplayName("caminho 3: produto novo com o limite atingido")
    class LimitReached {

        @Test
        void throws_when_the_eleventh_distinct_product_arrives() {
            Order order = draft();
            for (int i = 0; i < Order.MAX_DISTINCT_PRODUCTS; i++) {
                order.addItem(ProductId.newId(), price, one);
            }

            assertThatThrownBy(() -> order.addItem(ProductId.newId(), price, one))
                    .isInstanceOf(DomainException.class);
        }
    }

    @Nested
    @DisplayName("caminho 4: produto novo abaixo do limite")
    class NewProduct {

        @Test
        void appends_a_new_item() {
            Order order = draft();

            order.addItem(product, price, one);

            assertThat(order.items())
                    .singleElement()
                    .satisfies(item -> assertThat(item.productId()).isEqualTo(product));
        }
    }
}
