package io.github.geancgbr.orders.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataOrderRepository extends JpaRepository<OrderJpaEntity, UUID> {}

interface SpringDataProductRepository extends JpaRepository<ProductJpaEntity, UUID> {}
