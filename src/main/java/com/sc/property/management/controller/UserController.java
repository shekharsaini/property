package com.sc.property.management.controller;

import com.sc.property.management.dto.*;
import com.sc.property.management.exception.CustomValidationException;
import com.sc.property.management.exception.OtpException;
import com.sc.property.management.exception.UserNotFoundException;
import com.sc.property.management.service.AuthService;
import com.sc.property.management.service.OtpService;
import com.sc.property.management.service.UserService;
import com.sc.property.management.util.PropertyConstants;
import com.sc.property.management.util.Status;
import com.sc.property.management.util.UserSortField;
import com.sc.property.management.util.UserType;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@Slf4j
public class UserController {

  @Autowired private UserService userService;

  @Autowired private OtpService otpService;

  @Autowired private AuthService authService;

  @PostMapping("/signup")
  public ResponseEntity<?> signUp(@Valid @RequestBody UserDto userDto) {
    userService.registerUser(userDto);
    return ResponseEntity.ok(
        new ApiResponse<>(
            HttpStatus.CREATED.value(),
            Status.SUCCESS.getMessage(),
            PropertyConstants.SUCCESS_USER_REGISTERED,
            null));
  }

  @PostMapping("/verify-otp")
  public ResponseEntity<?> verifyOtp(
      @RequestParam String email, @RequestParam String otp, @RequestParam String otpType) {
    otpService.verifyOtp(email, otp, otpType);
    return ResponseEntity.ok(
        new ApiResponse<>(
            HttpStatus.OK.value(),
            Status.SUCCESS.getMessage(),
            PropertyConstants.SUCCESS_OTP_VERIFIED,
            null));
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
    AuthResponse authResponse = authService.validateUser(authRequest);
    return ResponseEntity.ok(
        new ApiResponse<>(
            HttpStatus.OK.value(),
            Status.SUCCESS.getMessage(),
            PropertyConstants.SUCCESS_LOGIN,
            authResponse));
  }

  @PostMapping("/resend-otp/{username}")
  public ResponseEntity<?> resendOtp(@PathVariable("username") String username) {
    CompletableFuture<Void> future =
        otpService.resendOtp(username); // Call the service method returning CompletableFuture
    try {
      // Use join() to wait for the async method to complete and handle any exceptions
      future.join();
      return ResponseEntity.ok(
          new ApiResponse<>(
              HttpStatus.OK.value(),
              Status.SUCCESS.getMessage(),
              PropertyConstants.SUCCESS_OTP_RESENT,
              null));
    } catch (CompletionException e) {
      Throwable cause = e.getCause(); // Get the original exception thrown inside CompletableFuture
      // Handle specific exceptions
      if (cause instanceof UserNotFoundException) {
        log.info("User not found: {}", cause.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(
                new ApiResponse<>(
                    HttpStatus.NOT_FOUND.value(),
                    Status.FAIL.getMessage(),
                    cause.getMessage(),
                    null));
      } else if (cause instanceof OtpException) {
        log.info("OTP related error: {}", cause.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    Status.FAIL.getMessage(),
                    cause.getMessage(),
                    null));
      } else {
        log.info("Unexpected error during OTP resend: {}", cause.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                new ApiResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    Status.FAIL.getMessage(),
                    "An unexpected error occurred.",
                    null));
      }
    } catch (Exception e) {
      // Handle other exceptions if necessary
      log.info("Unexpected error during OTP resend: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ApiResponse<>(
                  HttpStatus.INTERNAL_SERVER_ERROR.value(),
                  Status.FAIL.getMessage(),
                  "An unexpected error occurred.",
                  null));
    }
  }

  @PutMapping("/update")
  public ResponseEntity<?> resendOtp(@Valid @RequestBody UpdateUserDto userDto) {
    UpdateUserDto updateUserDto = userService.updateUser(userDto);
    return ResponseEntity.ok(
        new ApiResponse<>(
            HttpStatus.OK.value(),
            Status.SUCCESS.getMessage(),
            PropertyConstants.SUCCESS_USER_UPDATED,
            updateUserDto));
  }

  @PostMapping("/forget-password")
  public ResponseEntity<?> forgetPassword(@Valid @RequestBody ForgetPasswordDto forgetPasswordDto) {
    userService.processForgetPassword(forgetPasswordDto.getUsernameOrEmail());
    return ResponseEntity.ok(
        new ApiResponse<>(
            HttpStatus.OK.value(),
            Status.SUCCESS.getMessage(),
            PropertyConstants.FORGET_PASSOWRD_LINK_SENT,
            null));
  }

  @PostMapping("/reset-password")
  public ResponseEntity<?> resetPassword(
      @RequestParam("token") String token, @RequestBody ResetPasswordDto resetPasswordDto) {
    userService.resetPassword(token, resetPasswordDto);
    return ResponseEntity.ok(
        new ApiResponse<>(
            HttpStatus.OK.value(),
            Status.SUCCESS.getMessage(),
            PropertyConstants.RESET_PASSWORD_SUCCESS,
            null));
  }

  @PreAuthorize("hasRole('ROLE_LANDLORD') || hasRole('ROLE_AGENT')")
  @GetMapping("/fetch-all-users")
  public ResponseEntity<?> getAllUsers(
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size,
      @RequestParam(value = "sortField", defaultValue = "ID") UserSortField sortField,
      @RequestParam(value = "sortOrder", defaultValue = "ASC") Sort.Direction sortOrder,
      @RequestParam(value = "usertype", required = false) String usertype,
      @RequestParam(value = "username", required = false) String username,
      @RequestParam(value = "userId", required = false) Long userId,
      @RequestParam(value = "email", required = false) String email) {

    // Validate usertype if provided
    if (usertype != null && !usertype.isEmpty() && !isValidUserType(usertype)) {
      throw new CustomValidationException(
          "Invalid usertype. Valid values are: " + Arrays.toString(UserType.values()));
    }

    Pageable pageable = PageRequest.of(page, size, Sort.by(sortOrder, sortField.getField()));
    Page<UpdateUserDto> userPage =
        userService.getAllUsers(username, email, userId, usertype, pageable);

    if (userPage.isEmpty()) {
      throw new EntityNotFoundException("No users found matching the given criteria.");
    }

    return ResponseEntity.ok(
        new ApiResponse<>(
            HttpStatus.OK.value(),
            Status.SUCCESS.getMessage(),
            PropertyConstants.SUCCESS_USERS_FETCHED,
            userPage));
  }

  private boolean isValidUserType(String value) {
    return Arrays.stream(UserType.values()).anyMatch(type -> type.name().equalsIgnoreCase(value));
  }

  @GetMapping("/details")
  public ResponseEntity<?> getUserDetails(@RequestParam String username) {
    UpdateUserDto userDto = userService.getUserDetailsByUsername(username);
    return ResponseEntity.ok(
        new ApiResponse<>(
            HttpStatus.OK.value(),
            Status.SUCCESS.getMessage(),
            PropertyConstants.SUCCESS_USER_FETCHED,
            userDto));
  }

  @PreAuthorize("hasRole('ROLE_LANDLORD') || hasRole('ROLE_ADMIN')")
  @PostMapping("/invite")
  public ResponseEntity<?> inviteUser(@Valid @RequestBody InviteRequest inviteRequest) {
    UpdateUserDto invitedUser = userService.inviteUser(inviteRequest);
    return ResponseEntity.ok(
        new ApiResponse<>(
            HttpStatus.CREATED.value(),
            "SUCCESS",
            "User invited successfully. Default password has been set.",
            invitedUser));
  }
}
