package edu.rutmiit.demo.documentrest.graphql.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import edu.rutmiit.demo.documentsapicontract.dto.DocumentRequest;
import edu.rutmiit.demo.documentsapicontract.dto.DocumentResponse;
import edu.rutmiit.demo.documentsapicontract.dto.PagedResponse;
import edu.rutmiit.demo.documentsapicontract.dto.UpdateDocumentRequest;
import edu.rutmiit.demo.documentrest.graphql.types.DocumentConnectionGql;
import edu.rutmiit.demo.documentrest.graphql.types.DocumentFilterGql;
import edu.rutmiit.demo.documentrest.graphql.types.CreateDocumentInputGql;
import edu.rutmiit.demo.documentrest.graphql.types.PageInfoGql;
import edu.rutmiit.demo.documentrest.graphql.types.UpdateDocumentInputGql;
import edu.rutmiit.demo.documentrest.service.DocumentService;

/**
 * DataFetcher для операций с документами.
 *
 * Аннотация @DgsComponent регистрирует этот класс как компонент DGS-фреймворка.
 * Каждый метод с @DgsQuery или @DgsMutation привязывается к соответствующему полю
 * в GraphQL-схеме. DGS находит их по имени метода (или по явному параметру field).
 *
 * Этот DataFetcher обрабатывает корневые поля Query и Mutation для документов.
 * Вложенные поля (Document.sender, Document.recipient) обрабатываются в отдельном резолвере.
 */
@DgsComponent
public class DocumentDataFetcher {

    private final DocumentService documentService;

    public DocumentDataFetcher(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * Получение документа по идентификатору.
     * Соответствует полю Query.document(id: ID!) в схеме.
     * Возвращает null если документ не найден (вместо исключения, как принято в GraphQL).
     */
    @DgsQuery
    public DocumentResponse document(@InputArgument String id) {
        return documentService.findDocumentById(Long.parseLong(id));
    }

    /**
     * Список документов с фильтрацией и пагинацией.
     * Соответствует полю Query.documents(filter, page, size) в схеме.
     *
     * @InputArgument автоматически маппит GraphQL-аргументы на Java-параметры.
     * Для сложных типов (input DocumentFilter) DGS сам десериализует JSON в объект.
     */
    @DgsQuery
    public DocumentConnectionGql documents(
            @InputArgument DocumentFilterGql filter,
            @InputArgument Integer page,
            @InputArgument Integer size) {

        // Подставляем значения по умолчанию, если клиент не передал аргументы
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;

        // Извлекаем параметры фильтрации
        Long senderId = null;
        Long recipientId = null;
        String status = null;
        String titleSearch = null;

        if (filter != null) {
            senderId = filter.senderId() != null ? Long.parseLong(filter.senderId()) : null;
            recipientId = filter.recipientId() != null ? Long.parseLong(filter.recipientId()) : null;
            status = filter.status();
            titleSearch = filter.titleSearch();
        }

        // Переиспользуем существующий сервисный слой — GraphQL не дублирует бизнес-логику
        PagedResponse<DocumentResponse> paged = documentService.findAllDocuments(
                senderId, recipientId, status, titleSearch, pageNum, pageSize);

        return new DocumentConnectionGql(
                paged.content(),
                new PageInfoGql(paged.pageNumber(), paged.pageSize(), paged.totalPages(), paged.last()),
                (int) paged.totalElements());
    }

    /**
     * Создание документа.
     * Соответствует полю Mutation.createDocument(input: CreateDocumentInput!) в схеме.
     */
    @DgsMutation
    public DocumentResponse createDocument(@InputArgument CreateDocumentInputGql input) {
        DocumentRequest request = new DocumentRequest(
                input.title(),
                input.documentNumber(),
                Long.parseLong(input.senderId()),
                Long.parseLong(input.recipientId()),
                input.description()
        );
        return documentService.createDocument(request);
    }

    /**
     * Обновление документа.
     * Соответствует полю Mutation.updateDocument(id, input) в схеме.
     */
    @DgsMutation
    public DocumentResponse updateDocument(@InputArgument String id, @InputArgument UpdateDocumentInputGql input) {
        UpdateDocumentRequest request = new UpdateDocumentRequest(
                input.title(),
                input.documentNumber(),
                input.description()
        );
        return documentService.updateDocument(Long.parseLong(id), request);
    }

    /**
     * Удаление документа.
     * Соответствует полю Mutation.deleteDocument(id) в схеме.
     * Возвращает true при успешном удалении.
     */
    @DgsMutation
    public boolean deleteDocument(@InputArgument String id) {
        documentService.deleteDocument(Long.parseLong(id));
        return true;
    }
}
