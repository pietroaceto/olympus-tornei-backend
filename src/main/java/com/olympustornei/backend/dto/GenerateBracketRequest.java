package com.olympustornei.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record GenerateBracketRequest(
        @NotNull @Min(2) Integer qualifiedCount
) {
}
