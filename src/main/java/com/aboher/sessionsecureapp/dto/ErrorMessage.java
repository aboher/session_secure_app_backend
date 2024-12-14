package com.aboher.sessionsecureapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ErrorMessage(
        @Schema(description = "Error message", example = "Something went wrong.")
        String errorMessage) {
}
