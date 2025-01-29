package com.sc.property.management.service;

import java.util.concurrent.CompletableFuture;

public interface OtpService {
  CompletableFuture<Void> generateAndSendEmailOtp(Long userId);

  CompletableFuture<Void> generateAndSendSmsOtp(Long userId);

  void verifyOtp(String email, String otp, String otpType);

  CompletableFuture<Void> resendOtp(String username);

  void sendResetPasswordLink(String to, String resetPasswordLink);
}
