package edu.rutmiit.demo.documentsapicontract.dto;

import edu.rutmiit.demo.documentsapicontract.validation.ValidDocumentNumber;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO для создания нового документа.
 */
@Schema(description = "Запрос на создание документа")
public record DocumentRequest(

        @Schema(description = "Заголовок документа", example = "Служебная записка", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Заголовок не может быть пустым")
        @Size(max = 500, message = "Заголовок не может превышать 500 символов")
        String title,

        @Schema(description = "Номер документа в формате X-XXX-XXX", example = "A-123-456", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Номер документа не может быть пустым")
        @ValidDocumentNumber
        String documentNumber,

        @Schema(description = "ID отправителя", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "ID отправителя не может быть пустым")
        Long senderId,

        @Schema(description = "ID получателя", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "ID получателя не может быть пустым")
        Long recipientId,

        @Schema(description = "Описание документа", example = "Документ для согласования условий поставки.")
        @Size(max = 5000, message = "Описание не может превышать 5000 символов")
        String description
) {}
