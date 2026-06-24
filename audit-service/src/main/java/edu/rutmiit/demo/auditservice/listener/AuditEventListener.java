package edu.rutmiit.demo.auditservice.listener;

import edu.rutmiit.demo.auditservice.model.AuditEntry;
import edu.rutmiit.demo.auditservice.storage.AuditStorage;
import edu.rutmiit.demo.events.EmployeeEvent;
import edu.rutmiit.demo.events.DocumentEvent;
import edu.rutmiit.demo.events.EventMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;

/**
 * Единый слушатель всех доменных событий из RabbitMQ.
 *
 * Принимает «сырое» AMQP-сообщение (Message) и десериализует его вручную.
 * Это необходимо, потому что EventEnvelope<T> — generic тип, и Jackson
 * не может определить конкретный подтип T при автоматической десериализации.
 *
 * Промышленная альтернатива:
 * - отдельные очереди для разных типов событий (не generic listener),
 * - Spring Cloud Stream с content-type routing,
 * - Apache Avro/Protobuf с Schema Registry.
 */
@Component
public class AuditEventListener {

    private static final Logger log = LoggerFactory.getLogger(AuditEventListener.class);

    private final AuditStorage auditStorage;
    private final JsonMapper jsonMapper;

    public AuditEventListener(AuditStorage auditStorage, JsonMapper jsonMapper) {
        this.auditStorage = auditStorage;
        this.jsonMapper = jsonMapper;
    }

    /**
     * Принимает все события из очереди q.audit.events.
     *
     * Десериализация выполняется в два этапа:
     * 1. Парсим JSON в дерево узлов (JsonNode) — быстро и безопасно.
     * 2. Извлекаем metadata и определяем тип payload по полю eventType.
     * 3. Десериализуем payload в конкретный record по выявленному типу.
     */
    @RabbitListener(queues = "q.audit.events", messageConverter = "")
    public void handleEvent(Message message) {
        try {
            byte[] body = message.getBody();
            JsonNode root = jsonMapper.readTree(body);

            // Извлекаем метаданные из JSON-конверта
            JsonNode metaNode = root.get("metadata");
            EventMetadata metadata = jsonMapper.treeToValue(metaNode, EventMetadata.class);

            // Дедупликация — если событие уже обработано, пропускаем
            if (auditStorage.isDuplicate(metadata.eventId())) {
                log.warn("Дубликат события пропущен: eventId={}", metadata.eventId());
                return;
            }

            // Определяем тип события и формируем описание
            JsonNode payloadNode = root.get("payload");
            String description = buildDescription(metadata.eventType(), payloadNode);

            AuditEntry entry = auditStorage.save(new AuditEntry(
                    0,
                    metadata.eventId(),
                    metadata.eventType(),
                    metadata.source(),
                    metadata.timestamp(),
                    Instant.now(),
                    description
            ));

            log.info("[AUDIT #{}] {} | {}", entry.sequenceNumber(), metadata.eventType(), description);

        } catch (Exception e) {
            log.error("Ошибка обработки события: {}", e.getMessage(), e);
            // Исключение пробросится, сообщение уйдёт в DLQ после исчерпания retries
            throw new RuntimeException("Не удалось обработать событие", e);
        }
    }

    /**
     * Формирует человекочитаемое описание события для аудит-лога.
     *
     * Десериализует payload в конкретный тип на основе eventType,
     * затем формирует описание через pattern matching по sealed interface.
     */
    private String buildDescription(String eventType, JsonNode payloadNode) throws Exception {
        return switch (eventType) {
            case "document.created" -> {
                DocumentEvent.Created e = jsonMapper.treeToValue(payloadNode, DocumentEvent.Created.class);
                yield String.format("Создан документ «%s» (номер: %s), отправитель: %s, получатель: %s",
                        e.title(), e.documentNumber(), e.senderFullName(), e.recipientFullName());
            }
            case "document.updated" -> {
                DocumentEvent.Updated e = jsonMapper.treeToValue(payloadNode, DocumentEvent.Updated.class);
                yield String.format("Обновлён документ id=%d «%s» (состояние: %s)",
                        e.documentId(), e.title(), e.status());
            }
            case "document.deleted" -> {
                DocumentEvent.Deleted e = jsonMapper.treeToValue(payloadNode, DocumentEvent.Deleted.class);
                yield String.format("Удалён документ id=%d «%s»", e.documentId(), e.title());
            }
            case "employee.created" -> {
                EmployeeEvent.Created e = jsonMapper.treeToValue(payloadNode, EmployeeEvent.Created.class);
                yield String.format("Создан сотрудник «%s» (должность: %s)",
                        e.fullName(), e.position());
            }
            case "employee.deleted" -> {
                EmployeeEvent.Deleted e = jsonMapper.treeToValue(payloadNode, EmployeeEvent.Deleted.class);
                yield String.format("Удалён сотрудник «%s» (удалено документов: %d)",
                        e.fullName(), e.deletedDocumentsCount());
            }
            case "document.audited" -> {
                DocumentEvent.Audited e = jsonMapper.treeToValue(payloadNode, DocumentEvent.Audited.class);
                yield String.format("Документ проверен аудитом id=%d «%s» (номер: %s, состояние: %s, причина: %s)",
                        e.documentId(), e.title(), e.documentNumber(), e.status(), e.reason());
            }
            default -> "Неизвестное событие: " + eventType;
        };
    }
}
