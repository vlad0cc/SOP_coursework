package edu.rutmiit.demo.grpcenrichment.listener;

import edu.rutmiit.demo.events.DocumentEvent;
import edu.rutmiit.demo.events.EventMetadata;
import edu.rutmiit.demo.grpc.AnalyzeDocumentRequest;
import edu.rutmiit.demo.grpc.DocumentAnalysisResponse;
import edu.rutmiit.demo.grpc.DocumentAnalyticsGrpc;
import edu.rutmiit.demo.grpcenrichment.publisher.EnrichmentEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Слушатель событий document.created из RabbitMQ.
 */
@Component
public class DocumentCreatedListener {

    private static final Logger log = LoggerFactory.getLogger(DocumentCreatedListener.class);

    private final DocumentAnalyticsGrpc.DocumentAnalyticsBlockingStub analyticsStub;
    private final EnrichmentEventPublisher enrichmentPublisher;
    private final JsonMapper jsonMapper;

    public DocumentCreatedListener(DocumentAnalyticsGrpc.DocumentAnalyticsBlockingStub analyticsStub,
                                   EnrichmentEventPublisher enrichmentPublisher,
                                   JsonMapper jsonMapper) {
        this.analyticsStub = analyticsStub;
        this.enrichmentPublisher = enrichmentPublisher;
        this.jsonMapper = jsonMapper;
    }

    @RabbitListener(queues = "q.enrichment.document-created", messageConverter = "")
    public void handleDocumentCreated(Message message) {
        try {
            byte[] body = message.getBody();
            JsonNode root = jsonMapper.readTree(body);

            JsonNode metaNode = root.get("metadata");
            EventMetadata metadata = jsonMapper.treeToValue(metaNode, EventMetadata.class);

            JsonNode payloadNode = root.get("payload");
            DocumentEvent.Created documentCreated = jsonMapper.treeToValue(payloadNode, DocumentEvent.Created.class);

            log.info("Получено событие document.created: documentId={}, «{}» [eventId={}]",
                    documentCreated.documentId(), documentCreated.title(), metadata.eventId());

            AnalyzeDocumentRequest grpcRequest = AnalyzeDocumentRequest.newBuilder()
                    .setDocumentId(documentCreated.documentId())
                    .setTitle(documentCreated.title())
                    .setDocumentNumber(documentCreated.documentNumber())
                    .setSenderId(documentCreated.senderId() != null ? documentCreated.senderId() : 0)
                    .setSenderName(documentCreated.senderFullName() != null ? documentCreated.senderFullName() : "")
                    .setRecipientId(documentCreated.recipientId() != null ? documentCreated.recipientId() : 0)
                    .setRecipientName(documentCreated.recipientFullName() != null ? documentCreated.recipientFullName() : "")
                    .build();

            log.info("Вызов gRPC: DocumentAnalytics.AnalyzeDocument(documentId={})", documentCreated.documentId());
            DocumentAnalysisResponse grpcResponse = analyticsStub.analyzeDocument(grpcRequest);

            log.info("gRPC ответ получен: documentId={}, approved={}, status={}, reason={}",
                    grpcResponse.getDocumentId(),
                    grpcResponse.getApproved(),
                    grpcResponse.getStatus(),
                    grpcResponse.getReason());

            DocumentEvent.Audited auditedEvent = new DocumentEvent.Audited(
                    grpcResponse.getDocumentId(),
                    documentCreated.title(),
                    documentCreated.documentNumber(),
                    grpcResponse.getApproved(),
                    grpcResponse.getStatus(),
                    grpcResponse.getReason()
            );

            enrichmentPublisher.publishAudited(auditedEvent);

            log.info("Документ проверен аудитом: documentId={}, «{}» → document.audited отправлено",
                    documentCreated.documentId(), documentCreated.title());

        } catch (io.grpc.StatusRuntimeException e) {
            log.error("gRPC ошибка при аудите документа: {} ({})",
                    e.getStatus().getDescription(), e.getStatus().getCode());
            throw new RuntimeException("gRPC-вызов завершился ошибкой", e);

        } catch (Exception e) {
            log.error("Ошибка обработки события document.created: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось обработать событие document.created", e);
        }
    }
}
