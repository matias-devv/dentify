package com.dentify.exception.handler;

import com.dentify.exception.agenda.*;
import com.dentify.exception.allergycatalog.AllergiesCatalogNotFoundException;
import com.dentify.exception.appointment.AppointmentConflictException;
import com.dentify.exception.appointment.AppointmentNotFoundException;
import com.dentify.exception.appointment.AppointmentStateException;
import com.dentify.exception.auth.RefreshTokenException;
import com.dentify.exception.clinic.ClinicConflictException;
import com.dentify.exception.clinic.ClinicCuitAlreadyExistsException;
import com.dentify.exception.clinic.ClinicEmailAlreadyExistsException;
import com.dentify.exception.dentist.DentistIdMismatchException;
import com.dentify.exception.dentist.DentistNotFoundException;
import com.dentify.exception.diagnosistypecatalog.DiagnosisTypeNotFoundException;
import com.dentify.exception.dto.AppException;
import com.dentify.exception.dto.ErrorResponse;
import com.dentify.exception.general.InvalidRequestDateException;
import com.dentify.exception.general.InvalidRequestMonthException;
import com.dentify.exception.general.NoFieldsToUpdateException;
import com.dentify.exception.invitation.*;
import com.dentify.exception.medicalhistory.MedicalHistoryNotFoundException;
import com.dentify.exception.medicalhistory.OdontogramTypeConflictException;
import com.dentify.exception.patient.*;
import com.dentify.exception.patientallergy.AllergyInconsistencyException;
import com.dentify.exception.pay.*;
import com.dentify.exception.product.*;
import com.dentify.exception.receipt.ReceiptNotFoundException;
import com.dentify.exception.responsibleAdult.ResponsibleAdultDniAlreadyExistsException;
import com.dentify.exception.responsibleAdult.ResponsibleAdultEmailAlreadyExistsException;
import com.dentify.exception.responsibleAdult.ResponsibleAdultPhoneNumberAlreadyExistsException;
import com.dentify.exception.responsibleAdult.ResponsibleAdultsRequiredException;
import com.dentify.exception.role.RoleNotAllowedException;
import com.dentify.exception.schedule.InvalidScheduleException;
import com.dentify.exception.schedule.InvalidScheduleTimeException;
import com.dentify.exception.schedule.ScheduleOverlapException;
import com.dentify.exception.speciality.SpecialitiesRequiredException;
import com.dentify.exception.speciality.SpecialityNotFoundException;
import com.dentify.exception.tenant.TenantResourceNotFoundException;
import com.dentify.exception.toothrecord.DuplicateToothRecordException;
import com.dentify.exception.toothrecord.InvalidPieceNumberException;
import com.dentify.exception.toothrecord.MissingOdontogramTypeException;
import com.dentify.exception.toothrecord.ToothRecordFaceConflictException;
import com.dentify.exception.treatment.TreatmentNotFoundException;
import com.dentify.exception.user.AuthUserNotFoundException;
import com.dentify.exception.user.UserAlreadyExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {

        Map<String, Object> body = new HashMap<>();

        var fieldError = ex.getBindingResult().getFieldErrors().stream()
                                                                .findFirst()
                                                                .orElse(null);

        if (fieldError != null) {
            body.put("field", fieldError.getField());
            body.put("message", fieldError.getDefaultMessage());
        } else {
            body.put("message", "Validation failed");
        }

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleJsonParse(HttpMessageNotReadableException ex) {

        Map<String, Object> body = new HashMap<>();

        String field = "request";
        String message = "Invalid request body";

        Throwable cause = ex.getMostSpecificCause();
        if (cause instanceof InvalidFormatException ife) {

            if (ife.getPath() != null && !ife.getPath().isEmpty()) {
                field = ife.getPath().get(0).getFieldName();
            }
            message = "Invalid value for field '" + field + "'";
        }

        body.put("field", field);
        body.put("message", message);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleHandlerMethodValidation(HandlerMethodValidationException ex) {

        Map<String, Object> body = new HashMap<>();

        ex.getParameterValidationResults().stream()
                                        .filter(result -> !result.getResolvableErrors().isEmpty())
                                        .findFirst()
                                        .ifPresent(paramResult -> {

                                            String paramName = paramResult.getMethodParameter().getParameterName();
                                            String message = paramResult.getResolvableErrors().get(0).getDefaultMessage();

                                            body.put("field", paramName != null ? paramName : "unknown");
                                            body.put("message", message);
                                        });
        if (body.isEmpty()) {
            body.put("message", "Validation failed");
        }

        return ResponseEntity.badRequest().body(body);
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
            AmountInvalidException.class,
            PatientDniRequiredException.class,
            InvalidRequestDateException.class,
            InvalidRequestMonthException.class,
            MethodArgumentTypeMismatchException.class,
            NoFieldsToUpdateException.class,
            ProductNameEmptyException.class,
            InvalidProductPriceException.class,
            AllergyInconsistencyException.class,
            InvalidPieceNumberException.class,
            MissingOdontogramTypeException.class
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
            AgendaOwnershipException.class,
            DentistIdMismatchException.class,
    })
    public ResponseEntity<ErrorResponse> handleForbidden(AppException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
    }

    // ── 404 Not Found ─────────────────────────────────────────────────────────
    @ExceptionHandler({
            UsernameNotFoundException.class,
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
            SpecialityNotFoundException.class,
            DayOfAgendaNotFoundException.class,
            TreatmentNotFoundException.class,
            ReceiptNotFoundException.class,
            AllergiesCatalogNotFoundException.class,
            MedicalHistoryNotFoundException.class,
            DiagnosisTypeNotFoundException.class
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
            ScheduleOverlapException.class,
            ClinicCuitAlreadyExistsException.class,
            ClinicEmailAlreadyExistsException.class,
            ProductNameAlreadyExistsException.class,
            ClinicConflictException.class,
            OdontogramTypeConflictException.class,
            ToothRecordFaceConflictException.class
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
            AgendaMonthOutOfRangeException.class,
            PaymentStatusNotAllowedException.class,
            PaymentMethodNotAllowedException.class,
            InactiveProductException.class,
            InvalidScheduleTimeException.class,
            InvalidAgendaDurationException.class,
            DuplicateToothRecordException.class,

    })
    public ResponseEntity<ErrorResponse> handleUnprocessable(AppException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
    }
}