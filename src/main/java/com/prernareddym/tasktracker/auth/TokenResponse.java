package com.prernareddym.tasktracker.auth;

public record TokenResponse(String accessToken, String tokenType, long expiresInSeconds, String email, String displayName) {
}
