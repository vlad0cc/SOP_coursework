package edu.rutmiit.demo.notificationservice.listener;

import edu.rutmiit.demo.events.*;
import edu.rutmiit.demo.notificationservice.websocket.NotificationWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Слушатель всех доменных событий из RabbitMQ.
 */
@Component
public class EventNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(EventNotificationListener.class);

    private final NotificationWebSocketHandler webSocketHandler;
    private final JsonMapper jsonMapper;

    /** Набор обработанных eventId для дедупликации. */
    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();

    public EventNotificationListener(NotificationWebSocketHandler webSocketHandler,
                                     JsonMapper jsonMapper) {
        this.webSocketHandler = webSocketHandler;
        this.jsonMapper = jsonMapper;
    }

    @RabbitListener(queues = "q.notifications.all", messageConverter = "")
    public void handleEvent(Message message) {
        try {
            byte[] body = message.getBody();
            JsonNode root = jsonMapper.readTree(body);

            JsonNode metaNode = root.get("metadata");
            EventMetadata metadata = jsonMapper.treeToValue(metaNode, EventMetadata.class);

            if (!processedEventIds.add(metadata.eventId())) {
                log.warn("Дубликат уведомления пропущен: eventId={}", metadata.eventId());
                return;
            }

            JsonNode payloadNode = root.get("payload");
            String title = buildTitle(metadata.eventType());
            String description = buildDescription(metadata.eventType(), payloadNode);
            String icon = resolveIcon(metadata.eventType());
            String level = resolveLevel(metadata.eventType());

            String notificationJson = jsonMapper.writeValueAsString(
                    new NotificationPayload(
                            "NOTIFICATION",
                            metadata.eventId(),
                            metadata.eventType(),
                            title,
                            description,
                            icon,
                            level,
                            metadata.source(),
                            metadata.timestamp().toString(),
                            Instant.now().toString()
                    )
            );

            webSocketHandler.broadcast(notificationJson);

            log.info("[NOTIFY] {} | {} (клиентов: {})",
                    metadata.eventType(), description, webSocketHandler.getActiveConnectionCount());

        } catch (Exception e) {
            log.error("Ошибка обработки события для уведомлений: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось обработать событие", e);
        }
    }

    private String buildTitle(String eventType) {
        return switch (eventType) {
            case "document.created"   -> "Новый документ";
            case "document.updated"   -> "Документ обновлён";
            case "document.deleted"   -> "Документ удалён";
            case "document.audited"   -> "Аудит документа";
            case "employee.created"   -> "Новый сотрудник";
            case "employee.deleted"   -> "Сотрудник удалён";
            default                   -> "Событие: " + eventType;
        };
    }

    private String buildDescription(String eventType, JsonNode payload) {
        try {
            return switch (eventType) {
                case "document.created" -> {
                    DocumentEvent.Created e = jsonMapper.treeToValue(payload, DocumentEvent.Created.class);
                    yield "Создан документ «%s» (номер: %s), отправитель: %s, получатель: %s".formatted(
                            e.title(), e.documentNumber(), e.senderFullName(), e.recipientFullName());
                }
                case "document.updated" -> {
                    DocumentEvent.Updated e = jsonMapper.treeToValue(payload, DocumentEvent.Updated.class);
                    yield "Обновлён документ id=%d «%s» (состояние: %s)".formatted(
                            e.documentId(), e.title(), e.status());
                }
                case "document.deleted" -> {
                    DocumentEvent.Deleted e = jsonMapper.treeToValue(payload, DocumentEvent.Deleted.class);
                    yield "Удалён документ id=%d «%s»".formatted(e.documentId(), e.title());
                }
                case "document.audited" -> {
                    DocumentEvent.Audited e = jsonMapper.treeToValue(payload, DocumentEvent.Audited.class);
                    yield "Документ «%s» — состояние: %s, причина: %s".formatted(
                            e.title(), e.status(), e.reason());
                }
                case "employee.created" -> {
                    EmployeeEvent.Created e = jsonMapper.treeToValue(payload, EmployeeEvent.Created.class);
                    yield "Создан сотрудник «%s» (должность: %s)".formatted(
                            e.fullName(), e.position());
                }
                case "employee.deleted" -> {
                    EmployeeEvent.Deleted e = jsonMapper.treeToValue(payload, EmployeeEvent.Deleted.class);
                    yield "Удалён сотрудник «%s» (удалено документов: %d)".formatted(
                            e.fullName(), e.deletedDocumentsCount());
                }
                default -> "Неизвестное событие: " + eventType;
            };
        } catch (Exception e) {
            return "Событие " + eventType + " (ошибка парсинга)";
        }
    }

    private String resolveIcon(String eventType) {
        return switch (eventType) {
            case "document.created"   -> "document-plus";
            case "document.updated"   -> "document-edit";
            case "document.deleted"   -> "document-remove";
            case "document.audited"   -> "audit";
            case "employee.created"   -> "user-plus";
            case "employee.deleted"   -> "user-remove";
            default                   -> "bell";
        };
    }

    private String resolveLevel(String eventType) {
        return switch (eventType) {
            case "document.deleted", "employee.deleted", "document.audited" -> "warning";
            default -> "success";
        };
    }

    record NotificationPayload(
            String type,
            String eventId,
            String eventType,
            String title,
            String description,
            String icon,
            String level,
            String source,
            String eventTimestamp,
            String receivedAt
    ) {}
}
