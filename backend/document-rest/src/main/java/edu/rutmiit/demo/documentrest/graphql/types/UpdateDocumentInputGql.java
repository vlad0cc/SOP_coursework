package edu.rutmiit.demo.documentrest.graphql.types;

/**
 * Входной тип для обновления документа.
 * Соответствует input UpdateDocumentInput в GraphQL-схеме.
 */
public record UpdateDocumentInputGql(
        String title,
        String documentNumber,
        String description
) {}
