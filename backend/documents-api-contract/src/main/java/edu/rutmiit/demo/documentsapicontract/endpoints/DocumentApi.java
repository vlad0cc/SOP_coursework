package edu.rutmiit.demo.documentsapicontract.endpoints;

import edu.rutmiit.demo.documentsapicontract.config.DocumentsApiContractConfig;
import edu.rutmiit.demo.documentsapicontract.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Контракт API для управления документами.
 * Реализующий контроллер в сервисе должен имплементировать этот интерфейс.
 */
@Tag(name = "Documents", description = "Управление документами")
@RequestMapping(
        value = "/api/documents",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public interface DocumentApi {

    @Operation(
            summary = "Получить документ по ID",
            security = @SecurityRequirement(name = DocumentsApiContractConfig.SECURITY_SCHEME_BEARER)
    )
    @ApiResponse(responseCode = "200", description = "Документ найден")
    @ApiResponse(responseCode = "404", description = "Документ не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}")
    EntityModel<DocumentResponse> getDocumentById(
            @Parameter(description = "ID документа", required = true, example = "1") @PathVariable Long id
    );

    @Operation(
            summary = "Список документов",
            description = """
                    Возвращает постраничный список документов с HATEOAS-ссылками.
                    Поддерживает комбинирование фильтров: senderId, recipientId, status и titleSearch
                    можно передавать одновременно.
                    """,
            security = @SecurityRequirement(name = DocumentsApiContractConfig.SECURITY_SCHEME_BEARER)
    )
    @ApiResponse(responseCode = "200", description = "Постраничный список документов")
    @GetMapping
    PagedModel<EntityModel<DocumentResponse>> getAllDocuments(
            @Parameter(description = "Фильтр по ID отправителя") @RequestParam(required = false) Long senderId,
            @Parameter(description = "Фильтр по ID получателя") @RequestParam(required = false) Long recipientId,
            @Parameter(description = "Фильтр по состоянию", example = "SENT") @RequestParam(required = false) String status,
            @Parameter(description = "Поиск по заголовку (substring, case-insensitive)", example = "Служебная") @RequestParam(required = false) String titleSearch,
            @Parameter(description = "Номер страницы (0..N)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы", example = "20") @RequestParam(defaultValue = "20") int size
    );

    @Operation(
            summary = "Создать документ",
            security = @SecurityRequirement(name = DocumentsApiContractConfig.SECURITY_SCHEME_BEARER)
    )
    @ApiResponse(responseCode = "201", description = "Документ создан. Location header содержит URI нового ресурса.")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Сотрудник с указанным senderId или recipientId не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Документ с таким номером уже существует",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<EntityModel<DocumentResponse>> createDocument(@Valid @RequestBody DocumentRequest request);

    @Operation(
            summary = "Полное обновление документа (PUT)",
            description = "Заменяет все поля документа. Отправителя и получателя изменить нельзя. "
                    + "Для обновления отдельных полей используйте PATCH.",
            security = @SecurityRequirement(name = DocumentsApiContractConfig.SECURITY_SCHEME_BEARER)
    )
    @ApiResponse(responseCode = "200", description = "Документ обновлён")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Документ не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Документ с таким номером уже существует",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    EntityModel<DocumentResponse> updateDocument(
            @Parameter(description = "ID документа", required = true, example = "1") @PathVariable Long id,
            @Valid @RequestBody UpdateDocumentRequest request
    );

    @Operation(
            summary = "Частичное обновление документа (PATCH)",
            description = """
                    Обновляет только переданные поля (семантика JSON Merge Patch, RFC 7396).
                    Непереданные поля остаются без изменений. Отправителя и получателя изменить нельзя.
                    """,
            security = @SecurityRequirement(name = DocumentsApiContractConfig.SECURITY_SCHEME_BEARER)
    )
    @ApiResponse(responseCode = "200", description = "Документ обновлён")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Документ не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Документ с таким номером уже существует",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    EntityModel<DocumentResponse> patchDocument(
            @Parameter(description = "ID документа", required = true, example = "1") @PathVariable Long id,
            @Valid @RequestBody PatchDocumentRequest request
    );

    @Operation(
            summary = "Удалить документ",
            security = @SecurityRequirement(name = DocumentsApiContractConfig.SECURITY_SCHEME_BEARER)
    )
    @ApiResponse(responseCode = "204", description = "Документ удалён")
    @ApiResponse(responseCode = "404", description = "Документ не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteDocument(
            @Parameter(description = "ID документа", required = true, example = "1") @PathVariable Long id
    );
}
