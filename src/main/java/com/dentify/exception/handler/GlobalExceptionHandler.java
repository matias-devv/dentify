package com.dentify.domain.exception.handler;

import com.dentify.domain.exception.agenda.*;
import com.dentify.domain.exception.appointment.AppointmentConflictException;
import com.dentify.domain.exception.appointment.AppointmentNotFoundException;
import com.dentify.domain.exception.appointment.AppointmentStateException;
import com.dentify.domain.exception.dentist.DentistNotFoundException;
import com.dentify.domain.exception.dto.AppException;
import com.dentify.domain.exception.dto.ErrorResponse;
import com.dentify.domain.exception.invitation.*;
import com.dentify.domain.exception.patient.PatientNotFoundException;
import com.dentify.domain.exception.pay.PaymentRequiredException;
import com.dentify.domain.exception.product.ProductNotFoundException;
import com.dentify.domain.exception.role.RoleNotAllowedException;
import com.dentify.domain.exception.schedule.InvalidScheduleException;
import com.dentify.domain.exception.user.AuthUserNotFoundException;
import com.dentify.domain.exception.user.UserAlreadyExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ── Generic fallback ──────────────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"));
    }

    // ── Spring Security ───────────────────────────────────────────────────────
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabled(DisabledException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("ACCOUNT_DISABLED", ex.getMessage()));
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> handleLocked(LockedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("ACCOUNT_LOCKED", ex.getMessage()));
    }

    // ── 404 Not Found ─────────────────────────────────────────────────────────
    @ExceptionHandler({
            InvitationNotFoundException.class,
            AppointmentNotFoundException.class,
            PatientNotFoundException.class,
            DentistNotFoundException.class,
            AgendaNotFoundException.class,
            ProductNotFoundException.class,
            AuthUserNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFound(AppException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
    }

    // ── 409 Conflict ──────────────────────────────────────────────────────────
    @ExceptionHandler({
            UserAlreadyExistsException.class,
            PendingInvitationException.class,
            AppointmentConflictException.class,
            InvalidInvitationStatusException.class
    })
    public ResponseEntity<ErrorResponse> handleConflict(AppException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
    }

    // ── 403 Forbidden ─────────────────────────────────────────────────────────
    @ExceptionHandler({
            RoleNotAllowedException.class,
            AgendaOwnershipException.class
    })
    public ResponseEntity<ErrorResponse> handleForbidden(AppException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
    }

    // ── 400 Bad Request ───────────────────────────────────────────────────────
    @ExceptionHandler({
            EmailMismatchException.class,
            MissingParameterException.class,
            InvalidScheduleException.class,
            InvalidAgendaNameException.class,
            InvalidAgendaDateException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(AppException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
    }

    // ── 410 Gone ──────────────────────────────────────────────────────────────
    @ExceptionHandler(InvitationExpiredException.class)
    public ResponseEntity<ErrorResponse> handleGone(AppException ex) {
        return ResponseEntity.status(HttpStatus.GONE)
                .body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
    }

    // ── 402 Payment Required ──────────────────────────────────────────────────
    @ExceptionHandler(PaymentRequiredException.class)
    public ResponseEntity<ErrorResponse> handlePaymentRequired(AppException ex) {
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
    }

    // ── 422 Unprocessable Entity ──────────────────────────────────────────────
    @ExceptionHandler({
            AppointmentStateException.class,
            AgendaNotActiveException.class,
            AgendaDateOutOfRangeException.class
    })
    public ResponseEntity<ErrorResponse> handleUnprocessable(AppException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
    }
}