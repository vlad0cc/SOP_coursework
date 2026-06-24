package edu.rutmiit.demo.documentsapicontract.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * Запрос для частичного обновления сотрудника (PATCH).
 */
@Schema(description = "Частичное обновление сотрудника (PATCH). Передайте только те поля, которые нужно изменить.")
public record PatchEmployeeRequest(

        @Schema(description = "Новое полное ФИО сотрудника", example = "Петров Пётр Петрович")
        @Size(max = 255, message = "ФИО не может превышать 255 символов")
        String fullName,

        @Schema(description = "Новая должность сотрудника", example = "Руководитель отдела")
        @Size(max = 255, message = "Должность не может превышать 255 символов")
        String position
) {}
