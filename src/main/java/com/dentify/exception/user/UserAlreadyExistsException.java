package com.dentify.exception.user;

import com.dentify.exception.dto.AppException;
import lombok.Getter;

@Getter
public class UserAlreadyExistsException extends RuntimeException implements AppException {

    private final String errorCode;

    public UserAlreadyExistsException(String message) {
        super(message);
        this.errorCode = "USER_ALREADY_EXISTS";
    }

}