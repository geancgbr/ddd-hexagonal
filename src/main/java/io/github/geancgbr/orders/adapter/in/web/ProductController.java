package io.github.geancgbr.orders.adapter.in.web;

import io.github.geancgbr.orders.application.port.in.CatalogUseCases.DeactivateProduct;
import io.github.geancgbr.orders.application.port.in.CatalogUseCases.ListProducts;
import io.github.geancgbr.orders.application.port.in.CatalogUseCases.RegisterProduct;
import io.github.geancgbr.orders.application.port.in.CatalogUseCases.SearchProducts;
import io.github.geancgbr.orders.domain.product.Product;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
class ProductController {

    record RegisterProductRequest(
            @NotBlank @Size(max = 200) @Pattern(regexp = InputRules.FREE_TEXT, message = InputRules.FREE_TEXT_MESSAGE)
            String name,

            @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2)
            BigDecimal price) {}

    record ProductResponse(UUID id, String name, BigDecimal price, boolean active) {

        static ProductResponse from(Product product) {
            return new ProductResponse(
                    product.id().value(), product.name(), product.price().amount(), product.isActive());
        }
    }

    private final RegisterProduct registerProduct;
    private final DeactivateProduct deactivateProduct;
    private final ListProducts listProducts;
    private final SearchProducts searchProducts;

    ProductController(
            RegisterProduct registerProduct,
            DeactivateProduct deactivateProduct,
            ListProducts listProducts,
            SearchProducts searchProducts) {
        this.registerProduct = registerProduct;
        this.deactivateProduct = deactivateProduct;
        this.listProducts = listProducts;
        this.searchProducts = searchProducts;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<ProductResponse> register(@Valid @RequestBody RegisterProductRequest request) {
        Product product = registerProduct.register(request.name(), request.price());
        return ResponseEntity.created(URI.create("/products/" + product.id().value()))
                .body(ProductResponse.from(product));
    }

    @GetMapping
    List<ProductResponse> list(
            @RequestParam(required = false)
                    @Size(max = 100)
                    @Pattern(regexp = InputRules.FREE_TEXT, message = InputRules.FREE_TEXT_MESSAGE)
                    String name) {
        var products = name == null ? listProducts.list() : searchProducts.search(name);
        return products.stream().map(ProductResponse::from).toList();
    }

    @PostMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        deactivateProduct.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
