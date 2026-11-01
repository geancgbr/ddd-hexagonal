package io.github.geancgbr.orders.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataOrderRepository extends JpaRepository<OrderJpaEntity, UUID> {}

interface SpringDataProductRepository extends JpaRepository<ProductJpaEntity, UUID> {

    List<ProductJpaEntity> findByNameContainingIgnoreCaseOrderByName(String name);
}
