package com.sc.property.management.dto;

import lombok.Data;

@Data
public class ApiResponse<T> {
  private Integer code;
  private String status;
  private String message;
  private T data;

  public ApiResponse(Integer code, String status, String message, T data) {
    this.code = code;
    this.status = status;
    this.message = message;
    this.data = data;
  }
}
