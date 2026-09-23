package com.flickstream.auth;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException() {
        super("An account with this email already exists. Sign in instead.");
    }
}
