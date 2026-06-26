package edu.rutmiit.demo.documentsapicontract.dto;

public record AuthResponse(
        Long userId,
        String login,
        String fullName,
        String position
) {}
