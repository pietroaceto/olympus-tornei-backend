package com.olympustornei.backend.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record TeamCreateRequest(
        @NotBlank String name,
        List<@NotBlank String> players
) {
}
