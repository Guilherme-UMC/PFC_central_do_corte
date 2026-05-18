package com.centraldocorte.api.exception;

public class EmailAlreadyExistsException extends BusinessException {

    public EmailAlreadyExistsException(String email) {
        super("Email já cadastrado: " + email);
    }
}