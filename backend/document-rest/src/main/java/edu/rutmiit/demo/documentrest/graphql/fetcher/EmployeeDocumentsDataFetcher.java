package edu.rutmiit.demo.documentrest.graphql.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.InputArgument;
import edu.rutmiit.demo.documentsapicontract.dto.EmployeeResponse;
import edu.rutmiit.demo.documentsapicontract.dto.DocumentResponse;
import edu.rutmiit.demo.documentsapicontract.dto.PagedResponse;
import edu.rutmiit.demo.documentrest.graphql.types.DocumentConnectionGql;
import edu.rutmiit.demo.documentrest.graphql.types.PageInfoGql;
import edu.rutmiit.demo.documentrest.service.DocumentService;

/**
 * Вложенный резолвер для поля Employee.documents.
 *
 * Срабатывает когда клиент запрашивает документы сотрудника:
 *
 *   query {
 *     employee(id: "1") {
 *       fullName
 *       documents(page: 0, size: 5) {    ← этот резолвер
 *         content {
 *           title
 *         }
 *         totalElements
 *       }
 *     }
 *   }
 *
 * Демонстрирует работу с аргументами вложенного поля (page, size)
 * и доступ к родительскому объекту (Employee).
 */
@DgsComponent
public class EmployeeDocumentsDataFetcher {

    private final DocumentService documentService;

    public EmployeeDocumentsDataFetcher(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * Загружает документы указанного сотрудника с пагинацией.
     *
     * Аргументы (page, size) берутся из GraphQL-запроса через @InputArgument.
     * Родительский объект (Employee) берётся из DgsDataFetchingEnvironment.
     */
    @DgsData(parentType = "Employee", field = "documents")
    public DocumentConnectionGql documents(
            DgsDataFetchingEnvironment dfe,
            @InputArgument Integer page,
            @InputArgument Integer size) {

        EmployeeResponse employee = dfe.getSource();

        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;

        // Фильтруем документы по ID отправителя — переиспользуем сервис
        PagedResponse<DocumentResponse> paged = documentService.findAllDocuments(
                employee.getId(), null, null, null, pageNum, pageSize);

        return new DocumentConnectionGql(
                paged.content(),
                new PageInfoGql(paged.pageNumber(), paged.pageSize(), paged.totalPages(), paged.last()),
                (int) paged.totalElements());
    }
}
