package com.prernareddym.tasktracker.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(min = 8, max = 72, message = "password must be 8-72 characters") String password,
        @NotBlank @Size(max = 100) String displayName) {
}
