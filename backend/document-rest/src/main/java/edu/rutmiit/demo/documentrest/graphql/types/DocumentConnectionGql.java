package edu.rutmiit.demo.documentrest.graphql.types;

import edu.rutmiit.demo.documentsapicontract.dto.DocumentResponse;

import java.util.List;

/**
 * Тип-обёртка для постраничного ответа со списком документов.
 * Соответствует типу DocumentConnection в GraphQL-схеме.
 */
public record DocumentConnectionGql(
        List<DocumentResponse> content,
        PageInfoGql pageInfo,
        int totalElements
) {}
