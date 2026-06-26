package edu.rutmiit.demo.documentrest.event;

import edu.rutmiit.demo.documentrest.service.DocumentService;
import edu.rutmiit.demo.events.DocumentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Применяет результаты асинхронного аудита к документам в document-rest.
 */
@Component
public class DocumentAuditedListener {

    private static final Logger log = LoggerFactory.getLogger(DocumentAuditedListener.class);

    private final DocumentService documentService;
    private final JsonMapper jsonMapper;

    public DocumentAuditedListener(DocumentService documentService, JsonMapper jsonMapper) {
        this.documentService = documentService;
        this.jsonMapper = jsonMapper;
    }

    @RabbitListener(queues = "q.document-rest.document-audited", messageConverter = "")
    public void handleAudited(Message message) {
        try {
            JsonNode root = jsonMapper.readTree(message.getBody());
            DocumentEvent.Audited event = jsonMapper.treeToValue(root.get("payload"), DocumentEvent.Audited.class);

            documentService.applyAuditResult(event.documentId(), event.status(), event.reason());
            log.info("Применён результат аудита: documentId={}, status={}", event.documentId(), event.status());
        } catch (Exception e) {
            log.error("Ошибка обработки document.audited: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось обработать событие document.audited", e);
        }
    }
}
