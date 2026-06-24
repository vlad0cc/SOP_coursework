package edu.rutmiit.demo.documentsapicontract.dto;

import edu.rutmiit.demo.documentsapicontract.validation.ValidDocumentNumber;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Запрос для полного обновления документа (PUT).
 */
@Schema(description = "Полное обновление документа (PUT). Отправитель и получатель не меняются.")
public record UpdateDocumentRequest(

        @Schema(description = "Заголовок документа", example = "Служебная записка", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Заголовок не может быть пустым")
        @Size(max = 500, message = "Заголовок не может превышать 500 символов")
        String title,

        @Schema(description = "Номер документа в формате X-XXX-XXX", example = "A-123-456", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Номер документа не может быть пустым")
        @ValidDocumentNumber
        String documentNumber,

        @Schema(description = "Описание документа")
        @Size(max = 5000, message = "Описание не может превышать 5000 символов")
        String description
) {}
