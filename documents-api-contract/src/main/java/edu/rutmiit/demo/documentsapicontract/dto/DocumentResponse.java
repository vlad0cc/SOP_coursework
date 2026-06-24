package edu.rutmiit.demo.documentsapicontract.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

/**
 * Данные документа в ответе API.
 */
@Getter
@Builder
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Relation(collectionRelation = "documents", itemRelation = "document")
@Schema(description = "Информация о документе")
public class DocumentResponse extends RepresentationModel<DocumentResponse> {

    @Schema(description = "Уникальный идентификатор документа", example = "1")
    private final Long id;

    @Schema(description = "Заголовок документа", example = "Служебная записка")
    private final String title;

    @Schema(description = "Номер документа", example = "A-123-456")
    private final String documentNumber;

    @Schema(description = "Отправитель документа")
    private final EmployeeResponse sender;

    @Schema(description = "Получатель документа")
    private final EmployeeResponse recipient;

    @Schema(description = "Описание документа")
    private final String description;

    @Schema(description = "Состояние документа", example = "SENT")
    private final String status;

    @Schema(description = "Причина возврата аудитом")
    private final String auditComment;

    @Schema(description = "Подпись получателя")
    private final String signature;

}
