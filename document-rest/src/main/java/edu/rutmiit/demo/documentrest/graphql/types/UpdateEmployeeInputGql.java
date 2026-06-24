package edu.rutmiit.demo.documentrest.graphql.types;

/**
 * Входной тип для обновления сотрудника.
 * Соответствует input UpdateEmployeeInput в GraphQL-схеме.
 */
public record UpdateEmployeeInputGql(
        String fullName,
        String position
) {}
