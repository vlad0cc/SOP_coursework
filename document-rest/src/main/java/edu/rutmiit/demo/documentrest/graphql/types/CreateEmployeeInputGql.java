package edu.rutmiit.demo.documentrest.graphql.types;

/**
 * Входной тип для создания сотрудника.
 * Соответствует input CreateEmployeeInput в GraphQL-схеме.
 */
public record CreateEmployeeInputGql(
        String fullName,
        String position
) {}
