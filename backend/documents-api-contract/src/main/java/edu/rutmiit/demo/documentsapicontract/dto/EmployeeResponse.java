package edu.rutmiit.demo.documentsapicontract.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

/**
 * Данные сотрудника в ответе API.
 */
@Getter
@Builder
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Relation(collectionRelation = "employees", itemRelation = "employee")
@Schema(description = "Информация о сотруднике")
public class EmployeeResponse extends RepresentationModel<EmployeeResponse> {

    @Schema(description = "Уникальный идентификатор сотрудника", example = "1")
    private final Long id;

    @Schema(description = "Полное ФИО сотрудника", example = "Иванов Иван Иванович")
    private final String fullName;

    @Schema(description = "Должность сотрудника", example = "Главный бухгалтер")
    private final String position;

    @Schema(description = "Общее количество документов сотрудника", example = "3")
    private final Integer documentsCount;
}
