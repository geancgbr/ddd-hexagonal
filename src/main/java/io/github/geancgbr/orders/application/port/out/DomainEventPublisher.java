package io.github.geancgbr.orders.application.port.out;

import io.github.geancgbr.orders.domain.shared.DomainEvent;
import java.util.List;

public interface DomainEventPublisher {

    void publish(List<DomainEvent> events);
}
