package com.flickstream.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record SignUpRequest(
            @NotBlank @Size(min = 2, max = 100) String name,
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(min = 8, max = 72) String password) {
    }

    public record SignInRequest(
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(min = 8, max = 72) String password) {
    }

    public record UserResponse(Long id, String name, String email) {
        static UserResponse from(AccountPrincipal user) {
            return new UserResponse(user.id(), user.name(), user.email());
        }

        static UserResponse from(AppUser user) {
            return new UserResponse(user.getId(), user.getName(), user.getEmail());
        }
    }

    public record AuthResponse(UserResponse user) {
    }

    public record CsrfResponse(String token) {
    }

    public record ErrorResponse(String error) {
    }
}
