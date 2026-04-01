package com.dentify.exception.handler;

import com.dentify.exception.agenda.*;
import com.dentify.exception.appointment.AppointmentConflictException;
import com.dentify.exception.appointment.AppointmentNotFoundException;
import com.dentify.exception.appointment.AppointmentStateException;
import com.dentify.exception.auth.RefreshTokenException;
import com.dentify.exception.dentist.DentistNotFoundException;
import com.dentify.exception.dto.AppException;
import com.dentify.exception.dto.ErrorResponse;
import com.dentify.exception.invitation.*;
import com.dentify.exception.patient.CoverageTypeNotFoundException;
import com.dentify.exception.patient.PatientAlreadyExistsException;
import com.dentify.exception.patient.PatientEmailRequiredException;
import com.dentify.exception.patient.PatientNotFoundException;
import com.dentify.exception.pay.*;
import com.dentify.exception.product.InactiveProductException;
import com.dentify.exception.product.ProductAlreadyExistsException;
import com.dentify.exception.product.ProductNotFoundException;
import com.dentify.exception.responsibleAdult.ResponsibleAdultDniAlreadyExistsException;
import com.dentify.exception.responsibleAdult.ResponsibleAdultEmailAlreadyExistsException;
import com.dentify.exception.responsibleAdult.ResponsibleAdultPhoneNumberAlreadyExistsException;
import com.dentify.exception.responsibleAdult.ResponsibleAdultsRequiredException;
import com.dentify.exception.role.RoleNotAllowedException;
import com.dentify.exception.schedule.InvalidScheduleException;
import com.dentify.exception.schedule.ScheduleOverlapException;
import com.dentify.exception.speciality.SpecialitiesRequiredException;
import com.dentify.exception.speciality.SpecialityNotFoundException;
import com.dentify.exception.tenant.TenantResourceNotFoundException;
import com.dentify.exception.user.AuthUserNotFoundException;
import com.dentify.exception.user.UserAlreadyExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                            .getFieldErrors()
                            .stream()
                            .findFirst()
                            .map(FieldError::getDefaultMessage)
                            .orElse("Validation error");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(new ErrorResponse("VALIDATION_ERROR", message));
    }

    // ── Generic fallback ──────────────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"));
    }

    // ── Spring Security ───────────────────────────────────────────────────────
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabled(DisabledException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse("ACCOUNT_DISABLED", ex.getMessage()));
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> handleLocked(LockedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse("ACCOUNT_LOCKED", ex.getMessage()));
    }


    // ── 400 Bad Request ───────────────────────────────────────────────────────
    @ExceptionHandler({
            EmailMismatchException.class,
            MissingParameterException.class,
            InvalidScheduleException.class,
            InvalidAgendaNameException.class,
            InvalidAgendaDateException.class,
            SpecialitiesRequiredException.class,
            ResponsibleAdultsRequiredException.class,
            AmountQuantityException.class,
            AmountInvalidException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(AppException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
    }

    // ── 401 Unauthorized ─────────────────────────────────────────────────────
    @ExceptionHandler(RefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(AppException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
    }

    // ── 402 Payment Required ──────────────────────────────────────────────────
    @ExceptionHandler(PaymentRequiredException.class)
    public ResponseEntity<ErrorResponse> handlePaymentRequired(AppException ex) {
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
    }

    // ── 403 Forbidden ─────────────────────────────────────────────────────────
    @ExceptionHandler({
            RoleNotAllowedException.class,
            AgendaOwnershipException.class
    })
    public ResponseEntity<ErrorResponse> handleForbidden(AppException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
    }

    // ── 404 Not Found ─────────────────────────────────────────────────────────
    @ExceptionHandler({
            InvitationNotFoundException.class,
            AppointmentNotFoundException.class,
            PatientNotFoundException.class,
            DentistNotFoundException.class,
            AgendaNotFoundException.class,
            ProductNotFoundException.class,
            AuthUserNotFoundException.class,
            TenantResourceNotFoundException.class,
            PaymentNotFoundException.class,
            CoverageTypeNotFoundException.class,
            SpecialityNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFound(AppException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
    }

    // ── 409 Conflict ──────────────────────────────────────────────────────────
    @ExceptionHandler({
            UserAlreadyExistsException.class,
            PendingInvitationException.class,
            AppointmentConflictException.class,
            InvalidInvitationStatusException.class,
            ProductAlreadyExistsException.class,
            PatientAlreadyExistsException.class,
            PatientEmailRequiredException.class,
            ResponsibleAdultDniAlreadyExistsException.class,
            ResponsibleAdultPhoneNumberAlreadyExistsException.class,
            ResponsibleAdultEmailAlreadyExistsException.class,
            ScheduleOverlapException.class
    })
    public ResponseEntity<ErrorResponse> handleConflict(AppException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
    }


    // ── 410 Gone ──────────────────────────────────────────────────────────────
    @ExceptionHandler(InvitationExpiredException.class)
    public ResponseEntity<ErrorResponse> handleGone(AppException ex) {
        return ResponseEntity.status(HttpStatus.GONE).body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
    }

    // ── 422 Unprocessable Entity ──────────────────────────────────────────────
    @ExceptionHandler({
            AppointmentStateException.class,
            AgendaNotActiveException.class,
            AgendaDateOutOfRangeException.class,
            PaymentStatusNotAllowedException.class,
            PaymentMethodNotAllowedException.class,
            InactiveProductException.class

    })
    public ResponseEntity<ErrorResponse> handleUnprocessable(AppException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
    }
}