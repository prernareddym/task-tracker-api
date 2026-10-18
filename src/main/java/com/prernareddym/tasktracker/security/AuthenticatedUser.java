package com.prernareddym.tasktracker.security;

/** Principal placed in the SecurityContext after a valid JWT is presented. */
public record AuthenticatedUser(Long id, String email) {
}
