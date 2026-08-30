package com.wealthops.exception;

/**
 * Thrown on login when email/password don't match. Deliberately generic
 * ("Invalid email or password") both when the email doesn't exist and
 * when the password is wrong — never reveal which one failed, or the
 * login endpoint becomes a way to enumerate registered emails.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
