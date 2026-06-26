package edu.rutmiit.demo.documentsapicontract.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeclineDocumentRequest(
        @NotBlank(message = "Причина отказа обязательна")
        @Size(max = 1000, message = "Причина отказа не может превышать 1000 символов")
        String reason
) {}
