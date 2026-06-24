package edu.rutmiit.demo.grpcenrichment.publisher;

import edu.rutmiit.demo.events.DocumentEvent;
import edu.rutmiit.demo.events.EventEnvelope;
import edu.rutmiit.demo.events.RoutingKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Публикация событий аудита документов в RabbitMQ.
 */
@Component
public class EnrichmentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EnrichmentEventPublisher.class);
    private static final String SOURCE = "grpc-enrichment-client";

    private final RabbitTemplate rabbitTemplate;

    public EnrichmentEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishAudited(DocumentEvent.Audited auditedEvent) {
        try {
            EventEnvelope<DocumentEvent> envelope = EventEnvelope.wrap(
                    auditedEvent, SOURCE, RoutingKeys.DOCUMENT_AUDITED);

            rabbitTemplate.convertAndSend(
                    RoutingKeys.EXCHANGE,
                    RoutingKeys.DOCUMENT_AUDITED,
                    envelope);

            log.info("Событие отправлено: {} [documentId={}, eventId={}]",
                    RoutingKeys.DOCUMENT_AUDITED,
                    auditedEvent.documentId(),
                    envelope.metadata().eventId());

        } catch (Exception e) {
            log.error("Не удалось отправить событие {}: {}",
                    RoutingKeys.DOCUMENT_AUDITED, e.getMessage());
        }
    }
}
