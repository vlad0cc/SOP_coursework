package edu.rutmiit.demo.documentrest.service;

import edu.rutmiit.demo.documentsapicontract.dto.*;
import edu.rutmiit.demo.documentsapicontract.exception.DocumentNumberAlreadyExistsException;
import edu.rutmiit.demo.documentsapicontract.exception.ResourceNotFoundException;
import edu.rutmiit.demo.documentrest.event.DocumentEventPublisher;
import edu.rutmiit.demo.documentrest.storage.InMemoryStorage;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@Service
public class DocumentService {

    private static final String STATUS_SENT = "SENT";
    private static final String STATUS_SIGNED = "SIGNED";
    private static final String STATUS_DECLINED = "DECLINED";
    private static final String STATUS_RETURNED_BY_AUDIT = "RETURNED_BY_AUDIT";
    private static final String AUDIT_TITLE_REQUIRED = "Возвращено аудитом - заполните заголовок";
    private static final String AUDIT_DESCRIPTION_REQUIRED = "Возвращено аудитом - заполните документ";
    private static final String AUDIT_NUMBER_INVALID = "Возвращено аудитом - укажите корректный номер документа";
    private static final Pattern DOCUMENT_NUMBER = Pattern.compile("^[A-Z]-\\d{3}-\\d{3}$");

    private final InMemoryStorage storage;
    private final EmployeeService employeeService;
    private final DocumentEventPublisher eventPublisher;

    public DocumentService(InMemoryStorage storage,
                           @Lazy EmployeeService employeeService,
                           DocumentEventPublisher eventPublisher) {
        this.storage = storage;
        this.employeeService = employeeService;
        this.eventPublisher = eventPublisher;
    }

    public DocumentResponse findDocumentById(Long id) {
        return Optional.ofNullable(storage.documents.get(id))
                .orElseThrow(() -> new ResourceNotFoundException("Document", id));
    }

    public PagedResponse<DocumentResponse> findAllDocuments(Long senderId, Long recipientId, String status,
                                                            String titleSearch, int page, int size) {
        Stream<DocumentResponse> stream = storage.documents.values().stream()
                .sorted((d1, d2) -> d1.getId().compareTo(d2.getId()));

        if (senderId != null) {
            stream = stream.filter(d -> d.getSender() != null && d.getSender().getId().equals(senderId));
        }
        if (recipientId != null) {
            stream = stream.filter(d -> d.getRecipient() != null && d.getRecipient().getId().equals(recipientId));
        }
        if (status != null && !status.isBlank()) {
            stream = stream.filter(d -> status.equalsIgnoreCase(d.getStatus()));
        }
        if (titleSearch != null && !titleSearch.isBlank()) {
            String q = titleSearch.toLowerCase();
            stream = stream.filter(d -> d.getTitle() != null && d.getTitle().toLowerCase().contains(q));
        }

        List<DocumentResponse> allDocuments = stream.toList();
        int totalElements = allDocuments.size();
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;
        int from = page * size;
        int to = Math.min(from + size, totalElements);
        List<DocumentResponse> content = (from >= totalElements) ? List.of() : allDocuments.subList(from, to);
        return new PagedResponse<>(content, page, size, totalElements, totalPages, page >= totalPages - 1);
    }

    public List<DocumentResponse> findMyDocuments(Long employeeId) {
        return storage.documents.values().stream()
                .filter(d -> isParticipant(d, employeeId))
                .sorted((d1, d2) -> d2.getId().compareTo(d1.getId()))
                .toList();
    }

    public DocumentResponse createDocument(DocumentRequest request) {
        validateDocumentNumber(request.documentNumber(), null);
        EmployeeResponse sender = employeeService.findById(request.senderId());
        return createDocumentForUser(sender.getId(), request);
    }

    public DocumentResponse createDocumentForUser(Long senderId, DocumentRequest request) {
        validateDocumentNumber(request.documentNumber(), null);
        EmployeeResponse sender = employeeService.findById(senderId);
        EmployeeResponse recipient = employeeService.findById(request.recipientId());

        if (sender.getId().equals(recipient.getId())) {
            throw new ResponseStatusException(BAD_REQUEST, "Нельзя отправить документ самому себе");
        }

        long id = storage.documentSequence.incrementAndGet();
        DocumentResponse document = DocumentResponse.builder()
                .id(id)
                .title(request.title())
                .documentNumber(request.documentNumber().toUpperCase())
                .type(valueOrDefault(request.type(), "Документ"))
                .sender(sender)
                .recipient(recipient)
                .createdAt(LocalDate.now())
                .dueDate(request.dueDate())
                .priority(valueOrDefault(request.priority(), "Обычный"))
                .description(request.description())
                .status(STATUS_SENT)
                .build();

        String auditReason = validateForAudit(request.title(), request.documentNumber(), request.description());
        if (auditReason != null) {
            DocumentResponse rejected = withAuditState(document, STATUS_RETURNED_BY_AUDIT, auditReason);
            storage.documents.put(id, rejected);
            eventPublisher.publishAudited(rejected, false, auditReason);
            return rejected;
        }

        storage.documents.put(id, document);
        eventPublisher.publishCreated(document);

        return document;
    }

    public DocumentResponse signDocument(Long documentId, Long employeeId) {
        DocumentResponse existing = findDocumentById(documentId);
        assertRecipient(existing, employeeId);
        assertCanAnswer(existing);

        DocumentResponse signed = copy(existing)
                .status(STATUS_SIGNED)
                .signature(existing.getRecipient().getFullName())
                .declineReason(null)
                .build();

        storage.documents.put(documentId, signed);
        eventPublisher.publishUpdated(signed);
        return signed;
    }

    public DocumentResponse declineDocument(Long documentId, Long employeeId, String reason) {
        DocumentResponse existing = findDocumentById(documentId);
        assertRecipient(existing, employeeId);
        assertCanAnswer(existing);

        if (reason == null || reason.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "Укажите причину отказа");
        }

        DocumentResponse declined = copy(existing)
                .status(STATUS_DECLINED)
                .declineReason(reason.trim())
                .signature(null)
                .build();

        storage.documents.put(documentId, declined);
        eventPublisher.publishUpdated(declined);
        return declined;
    }

    public DocumentResponse updateDocument(Long id, UpdateDocumentRequest request) {
        DocumentResponse existing = findDocumentById(id);
        validateDocumentNumber(request.documentNumber(), id);

        DocumentResponse updated = DocumentResponse.builder()
                .id(id)
                .title(request.title())
                .documentNumber(request.documentNumber())
                .type(existing.getType())
                .sender(existing.getSender())
                .recipient(existing.getRecipient())
                .createdAt(existing.getCreatedAt())
                .dueDate(existing.getDueDate())
                .priority(existing.getPriority())
                .description(request.description())
                .status(STATUS_SENT)
                .auditComment(null)
                .declineReason(null)
                .signature(existing.getSignature())
                .build();

        String auditReason = validateForAudit(request.title(), request.documentNumber(), request.description());
        if (auditReason != null) {
            DocumentResponse rejected = withAuditState(updated, STATUS_RETURNED_BY_AUDIT, auditReason);
            storage.documents.put(id, rejected);
            eventPublisher.publishAudited(rejected, false, auditReason);
            return rejected;
        }

        storage.documents.put(id, updated);
        eventPublisher.publishUpdated(updated);
        return updated;
    }

    public DocumentResponse patchDocument(Long id, PatchDocumentRequest request) {
        DocumentResponse existing = findDocumentById(id);

        if (request.documentNumber() != null && !request.documentNumber().equalsIgnoreCase(existing.getDocumentNumber())) {
            validateDocumentNumber(request.documentNumber(), id);
        }

        DocumentResponse updated = DocumentResponse.builder()
                .id(id)
                .title(request.title() != null ? request.title() : existing.getTitle())
                .documentNumber(request.documentNumber() != null ? request.documentNumber() : existing.getDocumentNumber())
                .type(existing.getType())
                .sender(existing.getSender())
                .recipient(existing.getRecipient())
                .createdAt(existing.getCreatedAt())
                .dueDate(existing.getDueDate())
                .priority(existing.getPriority())
                .description(request.description() != null ? request.description() : existing.getDescription())
                .status(STATUS_SENT)
                .auditComment(null)
                .declineReason(null)
                .signature(existing.getSignature())
                .build();

        String auditReason = validateForAudit(updated.getTitle(), updated.getDocumentNumber(), updated.getDescription());
        if (auditReason != null) {
            DocumentResponse rejected = withAuditState(updated, STATUS_RETURNED_BY_AUDIT, auditReason);
            storage.documents.put(id, rejected);
            eventPublisher.publishAudited(rejected, false, auditReason);
            return rejected;
        }

        storage.documents.put(id, updated);
        eventPublisher.publishUpdated(updated);
        return updated;
    }

    public void deleteDocument(Long id) {
        DocumentResponse document = findDocumentById(id);
        storage.documents.remove(id);
        eventPublisher.publishDeleted(id, document.getTitle());
    }

    public void deleteDocumentsByEmployeeId(Long employeeId) {
        List<Long> toDelete = storage.documents.values().stream()
                .filter(d -> (d.getSender() != null && d.getSender().getId().equals(employeeId))
                        || (d.getRecipient() != null && d.getRecipient().getId().equals(employeeId)))
                .map(DocumentResponse::getId)
                .toList();
        toDelete.forEach(storage.documents::remove);
    }

    public void applyAuditResult(Long documentId, String status, String reason) {
        DocumentResponse existing = storage.documents.get(documentId);
        if (existing == null) {
            return;
        }
        if (!STATUS_SENT.equals(existing.getStatus())) {
            return;
        }

        DocumentResponse audited = withAuditState(
                existing,
                status,
                STATUS_RETURNED_BY_AUDIT.equals(status) ? reason : null
        );
        storage.documents.put(documentId, audited);
    }

    private void validateDocumentNumber(String documentNumber, Long currentDocumentId) {
        storage.documents.values().stream()
                .filter(d -> d.getDocumentNumber().equalsIgnoreCase(documentNumber))
                .filter(d -> !d.getId().equals(currentDocumentId))
                .findAny()
                .ifPresent(d -> { throw new DocumentNumberAlreadyExistsException(documentNumber); });
    }

    private String validateForAudit(String title, String documentNumber, String description) {
        if (title == null || title.isBlank()) {
            return AUDIT_TITLE_REQUIRED;
        }
        if (description == null || description.isBlank()) {
            return AUDIT_DESCRIPTION_REQUIRED;
        }
        if (documentNumber == null || !DOCUMENT_NUMBER.matcher(documentNumber).matches()) {
            return AUDIT_NUMBER_INVALID;
        }
        return null;
    }

    private DocumentResponse withAuditState(DocumentResponse source, String status, String auditComment) {
        return DocumentResponse.builder()
                .id(source.getId())
                .title(source.getTitle())
                .documentNumber(source.getDocumentNumber())
                .type(source.getType())
                .sender(source.getSender())
                .recipient(source.getRecipient())
                .createdAt(source.getCreatedAt())
                .dueDate(source.getDueDate())
                .priority(source.getPriority())
                .description(source.getDescription())
                .status(status)
                .auditComment(auditComment)
                .declineReason(source.getDeclineReason())
                .signature(source.getSignature())
                .build();
    }

    private boolean isParticipant(DocumentResponse document, Long employeeId) {
        return (document.getSender() != null && document.getSender().getId().equals(employeeId))
                || (document.getRecipient() != null && document.getRecipient().getId().equals(employeeId));
    }

    private void assertRecipient(DocumentResponse document, Long employeeId) {
        if (document.getRecipient() == null || !document.getRecipient().getId().equals(employeeId)) {
            throw new ResponseStatusException(FORBIDDEN, "Подписать или отклонить документ может только получатель");
        }
    }

    private void assertCanAnswer(DocumentResponse document) {
        if (!STATUS_SENT.equals(document.getStatus())) {
            throw new ResponseStatusException(BAD_REQUEST, "Документ уже обработан");
        }
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private DocumentResponse.DocumentResponseBuilder copy(DocumentResponse source) {
        return DocumentResponse.builder()
                .id(source.getId())
                .title(source.getTitle())
                .documentNumber(source.getDocumentNumber())
                .type(source.getType())
                .sender(source.getSender())
                .recipient(source.getRecipient())
                .createdAt(source.getCreatedAt())
                .dueDate(source.getDueDate())
                .priority(source.getPriority())
                .description(source.getDescription())
                .status(source.getStatus())
                .auditComment(source.getAuditComment())
                .declineReason(source.getDeclineReason())
                .signature(source.getSignature());
    }
}
