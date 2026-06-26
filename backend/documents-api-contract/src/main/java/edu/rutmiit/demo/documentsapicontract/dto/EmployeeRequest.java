package edu.rutmiit.demo.documentsapicontract.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO для создания или полного обновления сотрудника (POST / PUT).
 */
@Schema(description = "Запрос на создание или полное обновление сотрудника")
public record EmployeeRequest(

        @Schema(description = "Полное ФИО сотрудника", example = "Иванов Иван Иванович", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "ФИО сотрудника не может быть пустым")
        @Size(max = 255, message = "ФИО не может превышать 255 символов")
        String fullName,

        @Schema(description = "Должность сотрудника", example = "Главный бухгалтер", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Должность не может быть пустой")
        @Size(max = 255, message = "Должность не может превышать 255 символов")
        String position
) {}
