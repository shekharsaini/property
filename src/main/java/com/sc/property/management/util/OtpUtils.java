package com.sc.property.management.util;

import java.security.SecureRandom;

public class OtpUtils {
  private static final SecureRandom random = new SecureRandom();
  private static final int OTP_LENGTH = 6; // Length of OTP

  // Generates a new OTP
  public static String generateOtp() {
    return String.format("%0" + OTP_LENGTH + "d", random.nextInt((int) Math.pow(10, OTP_LENGTH)));
  }
}
