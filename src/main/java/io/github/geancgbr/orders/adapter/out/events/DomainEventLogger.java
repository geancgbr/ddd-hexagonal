package io.github.geancgbr.orders.adapter.out.events;

import io.github.geancgbr.orders.domain.shared.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class DomainEventLogger {

    private static final Logger log = LoggerFactory.getLogger(DomainEventLogger.class);

    @EventListener
    void on(DomainEvent event) {
        log.info("Evento de domínio: {}", event);
    }
}
