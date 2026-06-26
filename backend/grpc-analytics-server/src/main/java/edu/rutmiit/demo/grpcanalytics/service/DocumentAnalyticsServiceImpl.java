package edu.rutmiit.demo.grpcanalytics.service;

import edu.rutmiit.demo.grpc.AnalyzeDocumentRequest;
import edu.rutmiit.demo.grpc.DocumentAnalysisResponse;
import edu.rutmiit.demo.grpc.DocumentAnalyticsGrpc;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * Реализация gRPC-сервиса DocumentAnalytics.
 */
public class DocumentAnalyticsServiceImpl extends DocumentAnalyticsGrpc.DocumentAnalyticsImplBase {

    private static final Logger log = LoggerFactory.getLogger(DocumentAnalyticsServiceImpl.class);
    private static final Pattern DOCUMENT_NUMBER = Pattern.compile("^[A-Z]-\\d{3}-\\d{3}$");

    @Override
    public void analyzeDocument(AnalyzeDocumentRequest request,
                                StreamObserver<DocumentAnalysisResponse> responseObserver) {

        log.info("gRPC запрос: аудит документа id={} «{}» (номер: {}, отправитель: {}, получатель: {})",
                request.getDocumentId(), request.getTitle(), request.getDocumentNumber(),
                request.getSenderName(), request.getRecipientName());

        String reason = validate(request);
        boolean approved = reason == null;

        DocumentAnalysisResponse response = DocumentAnalysisResponse.newBuilder()
                .setDocumentId(request.getDocumentId())
                .setApproved(approved)
                .setStatus(approved ? "SENT" : "RETURNED_BY_AUDIT")
                .setReason(approved ? "Документ прошёл аудит" : reason)
                .build();

        log.info("gRPC ответ: документ id={}, approved={}, status={}, reason={}",
                response.getDocumentId(), response.getApproved(), response.getStatus(), response.getReason());

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private String validate(AnalyzeDocumentRequest request) {
        if (!DOCUMENT_NUMBER.matcher(request.getDocumentNumber()).matches()) {
            return "Номер документа должен быть в формате X-XXX-XXX";
        }
        if (request.getRecipientId() <= 0 || request.getRecipientName() == null || request.getRecipientName().isBlank()) {
            return "Не указан корректный получатель документа";
        }
        if (request.getSenderId() == request.getRecipientId()) {
            return "Нельзя отправить документ самому себе";
        }
        return null;
    }
}
