package com.amil.rede.credenciadasimples.presentation.dto;

import java.time.LocalDateTime;

/** Standardized API response wrapper. Provides consistent response format across all endpoints. */
public class ApiResponse<T> {

  private boolean success;
  private String message;
  private T data;
  private LocalDateTime timestamp;
  private String path;

  public ApiResponse() {
    this.timestamp = LocalDateTime.now();
  }

  public ApiResponse(boolean success, String message, T data) {
    this();
    this.success = success;
    this.message = message;
    this.data = data;
  }

  // Static factory methods for common responses
  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(true, "Success", data);
  }

  public static <T> ApiResponse<T> success(String message, T data) {
    return new ApiResponse<>(true, message, data);
  }

  public static <T> ApiResponse<T> error(String message) {
    return new ApiResponse<>(false, message, null);
  }

  // Getters and setters
  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }
}
