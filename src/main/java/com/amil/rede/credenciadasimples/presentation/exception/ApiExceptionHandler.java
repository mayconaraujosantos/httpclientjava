package com.amil.rede.credenciadasimples.presentation.exception;

import com.amil.rede.credenciadasimples.presentation.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * Global exception handler for the REST API. Provides consistent error responses across all
 * endpoints.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

  /** Handles IllegalArgumentException from business logic. */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponse<String>> handleIllegalArgumentException(
      IllegalArgumentException ex, WebRequest request) {

    ApiResponse<String> response = ApiResponse.error(ex.getMessage());
    response.setPath(request.getDescription(false));

    return ResponseEntity.badRequest().body(response);
  }

  /** Handles general runtime exceptions. */
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ApiResponse<String>> handleRuntimeException(
      RuntimeException ex, WebRequest request) {

    String message = "Erro interno do servidor: " + ex.getMessage();
    ApiResponse<String> response = ApiResponse.error(message);
    response.setPath(request.getDescription(false));

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }

  /** Handles any other unexpected exceptions. */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<String>> handleGenericException(
      Exception ex, WebRequest request) {

    String message = "Erro inesperado: " + ex.getMessage();
    ApiResponse<String> response = ApiResponse.error(message);
    response.setPath(request.getDescription(false));

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }
}
