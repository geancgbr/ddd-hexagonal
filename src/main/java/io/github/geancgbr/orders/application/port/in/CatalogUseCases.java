package io.github.geancgbr.orders.application.port.in;

import io.github.geancgbr.orders.domain.product.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class CatalogUseCases {

    private CatalogUseCases() {}

    public interface RegisterProduct {
        Product register(String name, BigDecimal price);
    }

    public interface DeactivateProduct {
        void deactivate(UUID productId);
    }

    public interface ListProducts {
        List<Product> list();
    }
}
