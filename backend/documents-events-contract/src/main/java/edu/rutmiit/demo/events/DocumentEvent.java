package edu.rutmiit.demo.events;

/**
 * Семейство событий, связанных с документами.
 */
public sealed interface DocumentEvent {

    /**
     * Документ создан и отправлен на аудит.
     */
    record Created(
            Long documentId,
            String title,
            String documentNumber,
            Long senderId,
            String senderFullName,
            Long recipientId,
            String recipientFullName,
            String description
    ) implements DocumentEvent {}

    /**
     * Документ обновлён. Содержит актуальное состояние после обновления.
     */
    record Updated(
            Long documentId,
            String title,
            String documentNumber,
            String status
    ) implements DocumentEvent {}

    /**
     * Документ удалён. Достаточно идентификатора — после удаления данных нет.
     */
    record Deleted(
            Long documentId,
            String title
    ) implements DocumentEvent {}

    /**
     * Документ прошёл или не прошёл gRPC-аудит.
     */
    record Audited(
            Long documentId,
            String title,
            String documentNumber,
            boolean approved,
            String status,
            String reason
    ) implements DocumentEvent {}
}
