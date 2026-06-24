package edu.rutmiit.demo.documentsapicontract.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Проверяет, что строка является корректным номером документа.
 */
public class DocumentNumberValidator implements ConstraintValidator<ValidDocumentNumber, String> {

    private static final Pattern DOCUMENT_NUMBER = Pattern.compile("^[A-Z]-\\d{3}-\\d{3}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null и пустую строку пропускаем — за них отвечает @NotBlank
        if (value == null || value.isBlank()) {
            return true;
        }
        return DOCUMENT_NUMBER.matcher(value).matches();
    }
}
