package edu.rutmiit.demo.documentrest.graphql.types;

import edu.rutmiit.demo.documentsapicontract.dto.EmployeeResponse;

import java.util.List;

/**
 * Тип-обёртка для постраничного ответа со списком сотрудников.
 * Соответствует типу EmployeeConnection в GraphQL-схеме.
 */
public record EmployeeConnectionGql(
        List<EmployeeResponse> content,
        PageInfoGql pageInfo,
        int totalElements
) {}
