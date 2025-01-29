package com.sc.property.management.util;

public class PropertyConstants {

  // Regex Patterns
  public static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
  public static final String PASSWORD_REGEX =
      "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

  // Error Messages
  public static final String ERROR_EMAIL_IN_USE = "Email already in use";
  public static final String ERROR_PHONE_IN_USE = "Phone number already in use";
  public static final String ERROR_INVALID_EMAIL_FORMAT = "Invalid email format";
  public static final String ERROR_WEAK_PASSWORD =
      "Password must be at least 8 characters long, contain at least one uppercase letter, one digit, and one special character";
  public static final String ERROR_USER_NOT_FOUND = "User not found";
  public static final String ERROR_NO_EXISTING_OTP = "No existing OTP found for user.";
  public static final String ERROR_MAX_OTP_RESEND_ATTEMPTS =
      "Maximum OTP resend attempts exceeded.";
  public static final String ERROR_RESEND_DELAY = "Resend OTP is allowed only after %d seconds.";
  public static final String ERROR_OTP_EXPIRED = "OTP expired, kindly generate new OTP.";
  public static final String ERROR_SENDING_OTP = "Failed to send OTP after maximum retries.";

  // OTP Settings
  public static final int OTP_EXPIRY_MINUTES = 5;
  public static final int MAX_RESEND_ATTEMPTS = 3;
  public static final int RESEND_DELAY_SECONDS = 60; // Delay before allowing another OTP resend
  public static final int MAX_RETRIES = 5; // Max retries for sending email
  public static final int DELAY_IN_SEC = 10; // Delay between retries for sending email

  // Miscellaneous
  public static final String DEFAULT_OTP_TYPE = "email";
  public static final String SUCCESS_USER_REGISTERED =
      "User registered successfully. Please verify your email and phone.";
  public static final String SUCCESS_PROPERTY_REGISTERED = "Property registered successfully.";
  public static final String SUCCESS_OTP_RESENT = "OTP resent successfully.";
  public static final String SUCCESS_OTP_VERIFIED = "OTP verified successfully.";
  public static final String SUCCESS_LOGIN = "Login successful.";

  // JWT and Security
  public static final String JWT_PREFIX = "Bearer ";
  public static final String ERROR_EMAIL_NOT_FOUND =
      "Email not found/registered, kindly provide correct email";
  public static final String ERROR_INVALID_OTP = "Invalid Otp";
  public static final String ERROR_OTP_ALREADY_VERIFIED =
      "Otp is already verified, please proceed ahead";
  public static final String INVALID_OTP_TYPE = "Invalid otp type";
  public static final String ADDRESS_VALIDATION_PATH = "/addresses/v3/address";
  public static final String TOKEN_GENERATION_PATH = "/oauth2/v3/token";
  public static final String ACTIVE = "ACTIVE";
  public static final String SUCCESS_PROPERTY_FETCHED = "Property details fetched successfully";
  public static final String SUCCESS_PROPERTY_DELETED = "Property deleted successfully";
  public static final String ERROR_PROPERTY_NOT_FOUND = "Property not found.";
  public static final String SUCCESS_INSURANCE_ADDED = "Insurance details added successfully";
  public static final String SUCCESS_INSURANCE_FETCHED = "Insurance details fetched successfully";
  public static final String SUCCESS_INSURANCE_DELETED = "Insurance details deleted successfully";
  public static final String SUCCESS_USER_UPDATED = "User details updated successfully";
  public static final String FORGET_PASSOWRD_LINK_SENT =
      "Reset password link has been sent to your email.";
  public static final String RESET_PASSWORD_SUCCESS = "Reset password successful";
  public static final String RESET_PASSWORD_LINK_ALREADY_SENT =
      "Reset password link already sent on your email.";
  public static final long RESET_TOKEN_EXPIRY_TIME = 5;
  public static final String INVALID_RESET_TOKEN = "Invalid or expired reset token";
  public static final String SUCCESS_USERS_FETCHED = "Users retrieved successfully";
  public static final String SUCCESS_USER_FETCHED = "User details fetched successfully";
  public static final String PROPERTY_COUNTS_FETCHED = "Property counts fetched successfully";
  public static final String DUPLICATE_PROPERTY_NAME =
      "Property name already exist, please provide different property name";
  public static final String INVALID_PROPERTY_AVAILABILTY_VALUES =
      "Invalid property availability value. Valid values: OPEN, OCCUPIED, IN NOTICE, UNDER MAINTENANCE.";
  public static final String INVALID_PROPERTY_STATUS_VALUE =
      "Invalid status value. Valid values: ACTIVE, INACTIVE.";
  public static final String DEFAULT_PASSWORD = "Password@123";
  public static final String USER_INVITE_BODY =
      "Dear %s,\n\nYou have been invited to join our platform.\n\nYour default password is: %s\nYour OTP for verification is: %s\n\nPlease verify Otp and proceed to login with username: %s and your default password by clicking on link %s.\n\nBest Regards,\nProperty Management Team";
  public static final Object VERIFY_OTP_URL = "http://concierge.com/api/v1/users/verify-otp";
  public static final String INVALID_INVITE_USER_TYPES =
      "Only user with type TENANT and AGENT can be invited";

  private PropertyConstants() {
    // Private constructor to prevent instantiation
  }
}
