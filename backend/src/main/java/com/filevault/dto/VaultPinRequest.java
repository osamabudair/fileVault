package com.filevault.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class VaultPinRequest {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PinPayload {
        @NotBlank(message = "Vault PIN is required")
        @Pattern(regexp = "^\\d{4,6}$", message = "Vault PIN must be a 4 to 6 digit number")
        private String pin;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangePinPayload {
        @NotBlank(message = "Current PIN is required")
        @Pattern(regexp = "^\\d{4,6}$", message = "Current PIN must be a 4 to 6 digit number")
        private String oldPin;

        @NotBlank(message = "New PIN is required")
        @Pattern(regexp = "^\\d{4,6}$", message = "New PIN must be a 4 to 6 digit number")
        private String newPin;
    }
}
