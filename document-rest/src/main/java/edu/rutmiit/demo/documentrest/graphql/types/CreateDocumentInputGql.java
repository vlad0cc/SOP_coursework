package edu.rutmiit.demo.documentrest.graphql.types;

/**
 * Входной тип для создания документа.
 * Соответствует input CreateDocumentInput в GraphQL-схеме.
 */
public record CreateDocumentInputGql(
        String title,
        String documentNumber,
        String senderId,
        String recipientId,
        String description
) {}
