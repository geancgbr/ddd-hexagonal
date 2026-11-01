package io.github.geancgbr.orders.adapter.in.web;

import io.github.geancgbr.orders.application.port.in.CatalogUseCases.DeactivateProduct;
import io.github.geancgbr.orders.application.port.in.CatalogUseCases.ListProducts;
import io.github.geancgbr.orders.application.port.in.CatalogUseCases.RegisterProduct;
import io.github.geancgbr.orders.domain.product.Product;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
class ProductController {

    record RegisterProductRequest(
            @NotBlank String name, @NotNull BigDecimal price) {}

    record ProductResponse(UUID id, String name, BigDecimal price, boolean active) {

        static ProductResponse from(Product product) {
            return new ProductResponse(
                    product.id().value(), product.name(), product.price().amount(), product.isActive());
        }
    }

    private final RegisterProduct registerProduct;
    private final DeactivateProduct deactivateProduct;
    private final ListProducts listProducts;

    ProductController(RegisterProduct registerProduct, DeactivateProduct deactivateProduct, ListProducts listProducts) {
        this.registerProduct = registerProduct;
        this.deactivateProduct = deactivateProduct;
        this.listProducts = listProducts;
    }

    @PostMapping
    ResponseEntity<ProductResponse> register(@Valid @RequestBody RegisterProductRequest request) {
        Product product = registerProduct.register(request.name(), request.price());
        return ResponseEntity.created(URI.create("/products/" + product.id().value()))
                .body(ProductResponse.from(product));
    }

    @GetMapping
    List<ProductResponse> list() {
        return listProducts.list().stream().map(ProductResponse::from).toList();
    }

    @PostMapping("/{id}/deactivate")
    ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        deactivateProduct.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
