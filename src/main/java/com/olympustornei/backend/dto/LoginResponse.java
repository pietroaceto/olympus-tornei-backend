package com.olympustornei.backend.dto;

public record LoginResponse(
        String token,
        String username,
        String role
) {
}
