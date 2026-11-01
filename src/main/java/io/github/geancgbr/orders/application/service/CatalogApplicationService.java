package io.github.geancgbr.orders.application.service;

import io.github.geancgbr.orders.application.port.in.CatalogUseCases.DeactivateProduct;
import io.github.geancgbr.orders.application.port.in.CatalogUseCases.ListProducts;
import io.github.geancgbr.orders.application.port.in.CatalogUseCases.RegisterProduct;
import io.github.geancgbr.orders.domain.product.Product;
import io.github.geancgbr.orders.domain.product.ProductId;
import io.github.geancgbr.orders.domain.product.ProductRepository;
import io.github.geancgbr.orders.domain.shared.Money;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class CatalogApplicationService implements RegisterProduct, DeactivateProduct, ListProducts {

    private final ProductRepository products;

    public CatalogApplicationService(ProductRepository products) {
        this.products = products;
    }

    @Override
    public Product register(String name, BigDecimal price) {
        return products.save(Product.register(name, new Money(price)));
    }

    @Override
    public void deactivate(UUID productId) {
        Product product = products.findById(new ProductId(productId))
                .orElseThrow(() -> new NotFoundException("Produto não encontrado: " + productId));
        product.deactivate();
        products.save(product);
    }

    @Override
    public List<Product> list() {
        return products.findAll();
    }
}
