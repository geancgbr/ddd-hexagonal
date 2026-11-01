package io.github.geancgbr.orders.config;

import io.github.geancgbr.orders.application.port.out.DomainEventPublisher;
import io.github.geancgbr.orders.application.service.CatalogApplicationService;
import io.github.geancgbr.orders.application.service.OrderApplicationService;
import io.github.geancgbr.orders.domain.order.OrderRepository;
import io.github.geancgbr.orders.domain.order.ProductOrderingService;
import io.github.geancgbr.orders.domain.product.ProductRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class UseCaseConfig {

    @Bean
    ProductOrderingService productOrderingService() {
        return new ProductOrderingService();
    }

    @Bean
    OrderApplicationService orderApplicationService(
            OrderRepository orders,
            ProductRepository products,
            ProductOrderingService ordering,
            DomainEventPublisher events) {
        return new OrderApplicationService(orders, products, ordering, events);
    }

    @Bean
    CatalogApplicationService catalogApplicationService(ProductRepository products) {
        return new CatalogApplicationService(products);
    }
}
