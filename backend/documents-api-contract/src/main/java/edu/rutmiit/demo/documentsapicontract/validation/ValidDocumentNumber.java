package edu.rutmiit.demo.documentsapicontract.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Аннотация для валидации номера документа.
 *
 * Допустимый формат: X-XXX-XXX, где X — заглавная латинская буква,
 * а XXX — группы из трёх цифр.
 */
@Documented
@Constraint(validatedBy = DocumentNumberValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDocumentNumber {

    String message() default "Некорректный номер документа. Допустимый формат: X-XXX-XXX";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
