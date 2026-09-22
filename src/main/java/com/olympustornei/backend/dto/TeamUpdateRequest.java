package com.olympustornei.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record TeamUpdateRequest(
        @NotBlank String name
) {
}
