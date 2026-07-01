package com.dentify.exception.toothrecord;


import com.dentify.exception.dto.AppException;

public class InvalidPieceNumberException extends RuntimeException implements AppException {

    private final String errorCode = "INVALID_PIECE_NUMBER";

    public InvalidPieceNumberException(String message) {
        super(message);
    }

    public String getErrorCode() {
        return errorCode;
    }

}