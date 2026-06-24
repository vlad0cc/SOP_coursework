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
 * Контракт API для управления сотрудниками.
 * Реализующий контроллер в сервисе должен имплементировать этот интерфейс.
 */
@Tag(name = "Employees", description = "Управление сотрудниками")
@RequestMapping(
        value = "/api/employees",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public interface EmployeeApi {

    @Operation(
            summary = "Список сотрудников",
            description = "Возвращает постраничный список сотрудников с HATEOAS-ссылками.",
            security = @SecurityRequirement(name = DocumentsApiContractConfig.SECURITY_SCHEME_BEARER)
    )
    @ApiResponse(responseCode = "200", description = "Список сотрудников")
    @GetMapping
    PagedModel<EntityModel<EmployeeResponse>> getAllEmployees(
            @Parameter(description = "Номер страницы (0..N)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы", example = "20")
            @RequestParam(defaultValue = "20") int size
    );

    @Operation(
            summary = "Получить сотрудника по ID",
            security = @SecurityRequirement(name = DocumentsApiContractConfig.SECURITY_SCHEME_BEARER)
    )
    @ApiResponse(responseCode = "200", description = "Сотрудник найден")
    @ApiResponse(responseCode = "404", description = "Сотрудник не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}")
    EntityModel<EmployeeResponse> getEmployeeById(
            @Parameter(description = "ID сотрудника", required = true, example = "1") @PathVariable Long id
    );

    @Operation(
            summary = "Создать сотрудника",
            security = @SecurityRequirement(name = DocumentsApiContractConfig.SECURITY_SCHEME_BEARER)
    )
    @ApiResponse(responseCode = "201", description = "Сотрудник создан. Location header содержит URI нового ресурса.")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<EntityModel<EmployeeResponse>> createEmployee(@Valid @RequestBody EmployeeRequest request);

    @Operation(
            summary = "Полное обновление сотрудника (PUT)",
            description = "Заменяет все поля сотрудника. Для обновления отдельных полей используйте PATCH.",
            security = @SecurityRequirement(name = DocumentsApiContractConfig.SECURITY_SCHEME_BEARER)
    )
    @ApiResponse(responseCode = "200", description = "Сотрудник обновлён")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Сотрудник не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    EntityModel<EmployeeResponse> updateEmployee(
            @Parameter(description = "ID сотрудника", required = true, example = "1") @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request
    );

    @Operation(
            summary = "Частичное обновление сотрудника (PATCH)",
            description = """
                    Обновляет только переданные поля (семантика JSON Merge Patch, RFC 7396).
                    Непереданные поля остаются без изменений.
                    """,
            security = @SecurityRequirement(name = DocumentsApiContractConfig.SECURITY_SCHEME_BEARER)
    )
    @ApiResponse(responseCode = "200", description = "Сотрудник обновлён")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Сотрудник не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    EntityModel<EmployeeResponse> patchEmployee(
            @Parameter(description = "ID сотрудника", required = true, example = "1") @PathVariable Long id,
            @Valid @RequestBody PatchEmployeeRequest request
    );

    @Operation(
            summary = "Удалить сотрудника",
            description = "Удаляет сотрудника и все связанные с ним документы.",
            security = @SecurityRequirement(name = DocumentsApiContractConfig.SECURITY_SCHEME_BEARER)
    )
    @ApiResponse(responseCode = "204", description = "Сотрудник удалён")
    @ApiResponse(responseCode = "404", description = "Сотрудник не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteEmployee(
            @Parameter(description = "ID сотрудника", required = true, example = "1") @PathVariable Long id
    );

    @Operation(
            summary = "Документы сотрудника (суб-ресурс)",
            description = """
                    Возвращает постраничный список документов указанного сотрудника.
                    Это суб-ресурс (концепция REST): /employees/{id}/documents.
                    Эквивалентен GET /documents?senderId={id}, но точнее отражает иерархию.
                    """,
            security = @SecurityRequirement(name = DocumentsApiContractConfig.SECURITY_SCHEME_BEARER)
    )
    @ApiResponse(responseCode = "200", description = "Список документов сотрудника")
    @ApiResponse(responseCode = "404", description = "Сотрудник не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}/documents")
    PagedModel<EntityModel<DocumentResponse>> getDocumentsByEmployee(
            @Parameter(description = "ID сотрудника", required = true, example = "1") @PathVariable Long id,
            @Parameter(description = "Номер страницы (0..N)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы", example = "20") @RequestParam(defaultValue = "20") int size
    );
}
