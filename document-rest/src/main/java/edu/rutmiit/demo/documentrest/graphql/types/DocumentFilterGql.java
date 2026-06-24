package edu.rutmiit.demo.documentrest.graphql.types;

/**
 * Входной тип для фильтрации документов.
 * Соответствует input DocumentFilter в GraphQL-схеме.
 */
public record DocumentFilterGql(
        String senderId,
        String recipientId,
        String status,
        String titleSearch
) {}
