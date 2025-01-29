package com.sc.property.management.exception;

import com.sc.property.management.dto.ApiResponse;
import com.sc.property.management.util.Status;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@ControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(OtpException.class)
  public ResponseEntity<ApiResponse<Object>> handleOtpResendException(
      OtpException ex, WebRequest request) {
    ApiResponse<Object> response =
        new ApiResponse<>(
            HttpStatus.BAD_REQUEST.value(), Status.FAIL.getMessage(), ex.getMessage(), null);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(AuthorizationException.class)
  public ResponseEntity<ApiResponse<Object>> handleOtpResendException(
      AuthorizationException ex, WebRequest request) {
    ApiResponse<Object> response =
        new ApiResponse<>(
            HttpStatus.BAD_REQUEST.value(), Status.FAIL.getMessage(), ex.getMessage(), null);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(CustomValidationException.class)
  public ResponseEntity<ApiResponse<Object>> handleOtpResendException(
      CustomValidationException ex, WebRequest request) {
    ApiResponse<Object> response =
        new ApiResponse<>(
            HttpStatus.BAD_REQUEST.value(), Status.FAIL.getMessage(), ex.getMessage(), null);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiResponse<Object>> handleConstraintViolationException(
      ConstraintViolationException ex, WebRequest request) {
    ApiResponse<Object> response =
        new ApiResponse<>(
            HttpStatus.BAD_REQUEST.value(), Status.FAIL.getMessage(), ex.getMessage(), null);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolationException(
      DataIntegrityViolationException ex) {
    String detail = extractConstraintViolationDetail(ex);
    ApiResponse<Object> response =
        new ApiResponse<>(HttpStatus.CONFLICT.value(), Status.FAIL.getMessage(), detail, null);
    return new ResponseEntity<>(response, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(WebClientResponseException.class)
  public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolationException(
      WebClientResponseException ex) {
    String detail = extractWebclientExceptionResponseDetails(ex);
    ApiResponse<Object> response =
        new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), Status.FAIL.getMessage(), detail, null);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  // Helper method to extract detailed information from the webclient exception
  private String extractWebclientExceptionResponseDetails(WebClientResponseException ex) {
    String responseBody = ex.getResponseBodyAsString();
    String statusCode = ex.getStatusText();
    return statusCode + " :: " + responseBody;
  }

  // Helper method to extract detailed information from the exception
  private String extractConstraintViolationDetail(DataIntegrityViolationException ex) {
    Throwable rootCause = ex.getRootCause();
    if (rootCause != null) {
      return rootCause.getMessage(); // Extract specific SQL error details
    }
    return "Constraint violation occurred.";
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Object>> handleGlobalException(
      Exception ex, WebRequest request) {
    ApiResponse<Object> response =
        new ApiResponse<>(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            Status.FAIL.getMessage(),
            "An unexpected error occurred.",
            null);
    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ApiResponse<Object>> handleUserNotFoundException(
      UserNotFoundException ex, WebRequest request) {
    ApiResponse<Object> response =
        new ApiResponse<>(
            HttpStatus.NOT_FOUND.value(), Status.FAIL.getMessage(), ex.getMessage(), null);
    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex, WebRequest request) {

    // Collect validation errors
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(
            error -> {
              errors.put(error.getField(), error.getDefaultMessage());
            });

    // Wrap errors in an errorDetails object
    Map<String, Object> errorDetails = new HashMap<>();
    errorDetails.put("errorDetails", errors);

    ApiResponse<Object> response =
        new ApiResponse<>(
            HttpStatus.BAD_REQUEST.value(),
            Status.FAIL.getMessage(),
            "Validation failed",
            errorDetails // Send error details in the nested object
            );

    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<String> handleMaxSizeException(MaxUploadSizeExceededException ex) {
    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
        .body("File size exceeds the maximum limit!");
  }

  @ExceptionHandler(RecordExistException.class)
  public ResponseEntity<ApiResponse<Object>> handleRecordExistException(
      RecordExistException ex, WebRequest request) {
    ApiResponse<Object> response =
        new ApiResponse<>(
            HttpStatus.NOT_ACCEPTABLE.value(), Status.FAIL.getMessage(), ex.getMessage(), null);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ApiResponse<Object>> handleEntityNotFoundException(
      EntityNotFoundException ex, WebRequest request) {
    ApiResponse<Object> response =
        new ApiResponse<>(
            HttpStatus.NO_CONTENT.value(), Status.FAIL.getMessage(), ex.getMessage(), null);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiResponse<Object>> handleEntityNotFoundException(
      MethodArgumentTypeMismatchException ex, WebRequest request) {
    ApiResponse<Object> response =
        new ApiResponse<>(
            HttpStatus.BAD_REQUEST.value(), Status.FAIL.getMessage(), ex.getMessage(), null);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException ex) {
    return new ResponseEntity<>("Access denied: " + ex.getMessage(), HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<String> handleAuthenticationException(AuthenticationException ex) {
    return new ResponseEntity<>(
        "Authentication failed: " + ex.getMessage(), HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(InvalidTokenException.class)
  public ResponseEntity<ApiResponse<Object>> invalidTokenException(
      InvalidTokenException ex, WebRequest request) {
    ApiResponse<Object> response =
        new ApiResponse<>(
            HttpStatus.UNAUTHORIZED.value(), Status.FAIL.getMessage(), ex.getMessage(), null);
    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(AuthorizationDeniedException.class)
  public ResponseEntity<ApiResponse<Object>> authorizationDeniedException(
      AuthorizationDeniedException ex, WebRequest request) {
    ApiResponse<Object> response =
        new ApiResponse<>(
            HttpStatus.UNAUTHORIZED.value(), Status.FAIL.getMessage(), ex.getMessage(), null);
    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
  }
}
