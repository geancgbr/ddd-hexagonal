package io.github.geancgbr.orders.adapter.out.persistence;

import io.github.geancgbr.orders.domain.product.Product;
import io.github.geancgbr.orders.domain.product.ProductId;
import io.github.geancgbr.orders.domain.product.ProductRepository;
import io.github.geancgbr.orders.domain.shared.Money;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
class JpaProductRepositoryAdapter implements ProductRepository {

    private final SpringDataProductRepository jpa;

    JpaProductRepositoryAdapter(SpringDataProductRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Product save(Product product) {
        jpa.save(new ProductJpaEntity(
                product.id().value(), product.name(), product.price().amount(), product.isActive()));
        return product;
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return jpa.findById(id.value()).map(JpaProductRepositoryAdapter::toDomain);
    }

    @Override
    public List<Product> findAll() {
        return jpa.findAll(Sort.by("name")).stream()
                .map(JpaProductRepositoryAdapter::toDomain)
                .toList();
    }

    private static Product toDomain(ProductJpaEntity entity) {
        return Product.restore(new ProductId(entity.id), entity.name, new Money(entity.price), entity.active);
    }
}
