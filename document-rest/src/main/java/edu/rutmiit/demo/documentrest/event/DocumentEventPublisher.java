package edu.rutmiit.demo.documentrest.event;

import edu.rutmiit.demo.documentsapicontract.dto.DocumentResponse;
import edu.rutmiit.demo.events.DocumentEvent;
import edu.rutmiit.demo.events.EventEnvelope;
import edu.rutmiit.demo.events.RoutingKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Публикация доменных событий документов в RabbitMQ.
 */
@Component
public class DocumentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(DocumentEventPublisher.class);
    private static final String SOURCE = "document-rest";

    private final RabbitTemplate rabbitTemplate;

    public DocumentEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishCreated(DocumentResponse document) {
        var event = new DocumentEvent.Created(
                document.getId(),
                document.getTitle(),
                document.getDocumentNumber(),
                document.getSender() != null ? document.getSender().getId() : null,
                document.getSender() != null ? document.getSender().getFullName() : "Неизвестен",
                document.getRecipient() != null ? document.getRecipient().getId() : null,
                document.getRecipient() != null ? document.getRecipient().getFullName() : "Неизвестен",
                document.getDescription()
        );
        send(RoutingKeys.DOCUMENT_CREATED, event);
    }

    public void publishUpdated(DocumentResponse document) {
        var event = new DocumentEvent.Updated(
                document.getId(),
                document.getTitle(),
                document.getDocumentNumber(),
                document.getStatus()
        );
        send(RoutingKeys.DOCUMENT_UPDATED, event);
    }

    public void publishDeleted(Long documentId, String title) {
        var event = new DocumentEvent.Deleted(documentId, title);
        send(RoutingKeys.DOCUMENT_DELETED, event);
    }

    public void publishAudited(DocumentResponse document, boolean approved, String reason) {
        var event = new DocumentEvent.Audited(
                document.getId(),
                document.getTitle(),
                document.getDocumentNumber(),
                approved,
                document.getStatus(),
                reason
        );
        send(RoutingKeys.DOCUMENT_AUDITED, event);
    }

    private void send(String routingKey, DocumentEvent event) {
        try {
            EventEnvelope<DocumentEvent> envelope = EventEnvelope.wrap(event, SOURCE, routingKey);
            rabbitTemplate.convertAndSend(RoutingKeys.EXCHANGE, routingKey, envelope);
            log.info("Событие отправлено: {} [eventId={}]", routingKey, envelope.metadata().eventId());
        } catch (Exception e) {
            log.error("Не удалось отправить событие {}: {}", routingKey, e.getMessage());
        }
    }
}
