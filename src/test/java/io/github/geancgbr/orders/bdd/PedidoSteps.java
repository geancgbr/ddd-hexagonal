package io.github.geancgbr.orders.bdd;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.github.geancgbr.orders.application.service.OrderApplicationService;
import io.github.geancgbr.orders.domain.order.Coupon;
import io.github.geancgbr.orders.domain.order.Order;
import io.github.geancgbr.orders.domain.order.OrderId;
import io.github.geancgbr.orders.domain.order.ProductOrderingService;
import io.github.geancgbr.orders.domain.product.Product;
import io.github.geancgbr.orders.domain.shared.DomainException;
import io.github.geancgbr.orders.domain.shared.Money;
import io.github.geancgbr.orders.support.InMemoryAdapters;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class PedidoSteps {

    private final InMemoryAdapters.Products products = new InMemoryAdapters.Products();
    private final InMemoryAdapters.Coupons coupons = new InMemoryAdapters.Coupons();
    private final OrderApplicationService service = new OrderApplicationService(
            new InMemoryAdapters.Orders(),
            products,
            new ProductOrderingService(),
            coupons,
            new InMemoryAdapters.RecordingPublisher());

    private final Map<String, Product> catalog = new HashMap<>();
    private OrderId orderId;
    private DomainException rejection;

    @Dado("que o catálogo tem os produtos:")
    public void catalogoComProdutos(DataTable table) {
        table.asMaps().forEach(row -> {
            Product product = Product.register(
                    row.get("produto"), Money.of(row.get("preço").replace(",", ".")));
            products.save(product);
            catalog.put(product.name(), product);
        });
    }

    @Dado("o cupom {string} dá {int}% de desconto")
    public void cupomDisponivel(String code, int percentage) {
        coupons.add(new Coupon(code, percentage));
    }

    @Dado("que o produto {string} saiu de linha")
    public void produtoForaDeLinha(String name) {
        Product product = catalog.get(name);
        product.deactivate();
        products.save(product);
    }

    @Dado("que o cliente {string} abriu um pedido")
    public void clienteAbriuPedido(String customer) {
        orderId = service.create(customer);
    }

    @Quando("adiciona {int} {string} ao pedido")
    public void adicionaItem(int quantity, String name) {
        attempt(() -> service.addItem(orderId, catalog.get(name).id().value(), quantity));
    }

    @Quando("aplica o cupom {string}")
    public void aplicaCupom(String code) {
        attempt(() -> service.applyCoupon(orderId, code));
    }

    @Quando("fecha o pedido")
    public void fechaPedido() {
        attempt(() -> service.place(orderId));
    }

    @Entao("o pedido fica com status {string}")
    public void statusDoPedido(String status) {
        assertThat(order().status().name()).isEqualTo(status);
    }

    @Entao("o subtotal é R$ {bigdecimal}")
    public void subtotal(BigDecimal value) {
        assertThat(order().subtotal()).isEqualTo(new Money(value));
    }

    @Entao("o desconto é R$ {bigdecimal}")
    public void desconto(BigDecimal value) {
        assertThat(order().discount()).isEqualTo(new Money(value));
    }

    @Entao("o total é R$ {bigdecimal}")
    public void total(BigDecimal value) {
        assertThat(order().total()).isEqualTo(new Money(value));
    }

    @Entao("o pedido é recusado com a mensagem {string}")
    public void recusado(String message) {
        assertThat(rejection).isNotNull().hasMessage(message);
    }

    @Entao("o resultado é {string}")
    public void resultado(String expected) {
        assertThat(rejection == null ? "aceito" : "recusado").isEqualTo(expected);
    }

    private Order order() {
        return service.get(orderId);
    }

    private void attempt(Runnable action) {
        try {
            action.run();
            rejection = null;
        } catch (DomainException exception) {
            rejection = exception;
        }
    }
}
