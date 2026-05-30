package com.dmaiti.eventledger.account.exception;

import com.dmaiti.eventledger.account.dto.ErrorResponse;
import com.dmaiti.eventledger.account.model.ServiceConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.toList());

        return ResponseEntity.badRequest().body(
                new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                        ServiceConstants.ERROR_BAD_REQUEST,
                        ServiceConstants.ERROR_VALIDATION_FAILED,
                        fieldErrors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(
                new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                        ServiceConstants.ERROR_BAD_REQUEST,
                        ServiceConstants.ERROR_MALFORMED_REQUEST,
                        null));
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(AccountNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorResponse(HttpStatus.NOT_FOUND.value(),
                        ServiceConstants.ERROR_NOT_FOUND,
                        ex.getMessage(),
                        null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        ServiceConstants.ERROR_INTERNAL,
                        ex.getMessage(),
                        null));
    }
}
