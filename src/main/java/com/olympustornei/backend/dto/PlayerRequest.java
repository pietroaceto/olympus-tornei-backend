package com.olympustornei.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record PlayerRequest(
        @NotBlank String name
) {
}
