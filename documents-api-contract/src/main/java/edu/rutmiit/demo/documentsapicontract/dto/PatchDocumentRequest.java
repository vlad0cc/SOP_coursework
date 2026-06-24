package edu.rutmiit.demo.documentsapicontract.dto;

import edu.rutmiit.demo.documentsapicontract.validation.ValidDocumentNumber;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * Запрос для частичного обновления документа (PATCH).
 */
@Schema(description = "Частичное обновление документа (PATCH). Передайте только те поля, которые нужно изменить.")
public record PatchDocumentRequest(

        @Schema(description = "Новый заголовок документа", example = "Служебная записка")
        @Size(max = 500, message = "Заголовок не может превышать 500 символов")
        String title,

        @Schema(description = "Новый номер документа в формате X-XXX-XXX", example = "A-123-456")
        @ValidDocumentNumber
        String documentNumber,

        @Schema(description = "Новое описание документа")
        @Size(max = 5000, message = "Описание не может превышать 5000 символов")
        String description
) {}
