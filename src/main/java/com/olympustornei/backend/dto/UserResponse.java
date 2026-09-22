package com.olympustornei.backend.dto;

public record UserResponse(
        Long id,
        String username,
        String role
) {
}
