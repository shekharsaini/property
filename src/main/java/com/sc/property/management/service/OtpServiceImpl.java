package com.sc.property.management.service;

import com.sc.property.management.datasource.OtpRequest;
import com.sc.property.management.datasource.User;
import com.sc.property.management.exception.CustomValidationException;
import com.sc.property.management.exception.OtpException;
import com.sc.property.management.exception.UserNotFoundException;
import com.sc.property.management.repository.OtpRequestRepository;
import com.sc.property.management.repository.UserRepository;
import com.sc.property.management.util.OtpType;
import com.sc.property.management.util.OtpUtils;
import com.sc.property.management.util.PropertyConstants;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OtpServiceImpl implements OtpService {

  @Autowired private JavaMailSender mailSender;

  @Autowired private OtpRequestRepository otpRequestRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  // Load Twilio configuration from application.properties
  @Value("${twilio.account.sid}")
  private String accountSid;

  @Value("${twilio.auth.token}")
  private String authToken;

  @Value("${twilio.phone.number}")
  private String fromPhoneNumber;

  // Initialize Twilio with account SID and auth token
  @PostConstruct
  public void initTwilio() {
    Twilio.init(accountSid, authToken);
  }

  @Override
  @Async
  public CompletableFuture<Void> generateAndSendEmailOtp(Long userId) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            log.info("generating email otp");
            int attempt = 0;
            User user =
                userRepository
                    .findById(userId)
                    .orElseThrow(
                        () -> new RuntimeException(PropertyConstants.ERROR_USER_NOT_FOUND));
            String otp = OtpUtils.generateOtp();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiresAt = now.plusMinutes(PropertyConstants.OTP_EXPIRY_MINUTES);
            boolean isDefaultPassword =
                passwordEncoder.matches(PropertyConstants.DEFAULT_PASSWORD, user.getPassword());

            OtpRequest otpRequest =
                OtpRequest.builder()
                    .user(user)
                    .otpCode(otp)
                    .otpType(OtpType.EMAIL.getType())
                    .createdAt(now)
                    .expiresAt(expiresAt)
                    .isVerified(false)
                    .resendAttempts(0L)
                    .build();
            log.info("Storing email otp in datasource");
            otpRequestRepository.save(otpRequest);
            while (true) {
              try {
                log.info("Sending email to {}", user.getEmail());
                sendOtpEmail(user.getEmail(), otp, isDefaultPassword, user.getUsername());
                break;
              } catch (Exception e) {
                attempt++;
                log.info("Failed attempt {} to send email. Retrying...", attempt, e);
                if (attempt >= PropertyConstants.MAX_RETRIES) {
                  log.info("Max retries exceeded");
                  throw new RuntimeException(PropertyConstants.ERROR_SENDING_OTP);
                }
                try {
                  TimeUnit.SECONDS.sleep(PropertyConstants.DELAY_IN_SEC);
                } catch (InterruptedException interruptedException) {
                  Thread.currentThread().interrupt();
                  throw new RuntimeException("Thread was interrupted", interruptedException);
                }
              }
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
          return null;
        });
  }

  @Override
  public CompletableFuture<Void> generateAndSendSmsOtp(Long userId) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            log.info("generating sms otp");
            int attempt = 0;
            User user =
                userRepository
                    .findById(userId)
                    .orElseThrow(
                        () -> new RuntimeException(PropertyConstants.ERROR_USER_NOT_FOUND));
            String otp = OtpUtils.generateOtp();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiresAt = now.plusMinutes(PropertyConstants.OTP_EXPIRY_MINUTES);

            OtpRequest otpRequest =
                OtpRequest.builder()
                    .user(user)
                    .otpCode(otp)
                    .otpType(OtpType.PHONE.getType())
                    .createdAt(now)
                    .expiresAt(expiresAt)
                    .isVerified(false)
                    .resendAttempts(0L)
                    .build();
            log.info("Storing sms otp in datasource");
            otpRequestRepository.save(otpRequest);
            while (true) {
              try {
                log.info("Sending sms to {}", user.getPhone());
                sendOtpToPhone(user.getPhone(), otp);
                break;
              } catch (Exception e) {
                attempt++;
                log.info("Failed attempt {} to send sms. Retrying...", attempt, e);
                if (attempt >= PropertyConstants.MAX_RETRIES) {
                  log.info("Max retries exceeded");
                  throw new RuntimeException(PropertyConstants.ERROR_SENDING_OTP);
                }
                try {
                  TimeUnit.SECONDS.sleep(PropertyConstants.DELAY_IN_SEC);
                } catch (InterruptedException interruptedException) {
                  Thread.currentThread().interrupt();
                  throw new RuntimeException("Thread was interrupted", interruptedException);
                }
              }
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
          return null;
        });
  }

  private void sendOtpToPhone(String toPhoneNumber, String otp) {
    try {
      // Compose the message with the OTP
      String messageBody =
          String.format("Your OTP code for mundane service verification is: %s", otp);

      // Create and send the message
      Message message =
          Message.creator(
                  new PhoneNumber(toPhoneNumber), // To number
                  new PhoneNumber(fromPhoneNumber), // From number
                  messageBody // Message body
                  )
              .create();

      log.info("SMS sent successfully to {} with SID: {}", toPhoneNumber, message.getSid());
    } catch (Exception e) {
      log.error("Failed to send SMS to {}: {}", toPhoneNumber, e.getMessage());
      throw new RuntimeException("Failed to send SMS", e);
    }
  }

  @Override
  public void verifyOtp(String email, String otp, String otpType) {
    // Logic for OTP verification
    boolean exists =
        Arrays.stream(OtpType.values()).anyMatch(type -> type.getType().equals(otpType));
    if (!exists) {
      throw new CustomValidationException(PropertyConstants.INVALID_OTP_TYPE);
    }
    Optional<User> optionalUser = userRepository.findByEmail(email);
    if (optionalUser.isEmpty()) {
      throw new CustomValidationException(PropertyConstants.ERROR_EMAIL_NOT_FOUND);
    }
    User user = optionalUser.get();
    OtpRequest otpRequest =
        otpRequestRepository
            .findByUserIdAndOtpCodeAndOtpType(user.getId(), otp, otpType)
            .orElse(null);

    if (otpRequest == null) {
      throw new OtpException(PropertyConstants.ERROR_INVALID_OTP);
    }
    if (otpRequest.isVerified()) {
      throw new OtpException(PropertyConstants.ERROR_OTP_ALREADY_VERIFIED);
    }
    if (otpRequest.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw new OtpException(PropertyConstants.ERROR_OTP_EXPIRED);
    }
    otpRequest.setVerified(true);
    user.setEmailVerified(true);
    userRepository.save(user);
    otpRequestRepository.save(otpRequest);
  }

  @Async
  @Override
  public CompletableFuture<Void> resendOtp(String username) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    try {
      User user =
          userRepository
              .findByUsername(username)
              .orElseThrow(() -> new UserNotFoundException(PropertyConstants.ERROR_USER_NOT_FOUND));

      // Fetch the latest OTP request for the user
      Optional<OtpRequest> existingOtpOpt = otpRequestRepository.findLatestOtpByUser(user.getId());

      if (existingOtpOpt.isEmpty()) {
        throw new OtpException(PropertyConstants.ERROR_NO_EXISTING_OTP);
      }

      OtpRequest existingOtp = existingOtpOpt.get();

      // Check if OTP resend attempts are exceeded
      if (existingOtp.getResendAttempts() >= PropertyConstants.MAX_RESEND_ATTEMPTS) {
        throw new OtpException(PropertyConstants.ERROR_MAX_OTP_RESEND_ATTEMPTS);
      }

      // Check if resend is allowed based on delay
      LocalDateTime now = LocalDateTime.now();
      if (Duration.between(existingOtp.getCreatedAt(), now).getSeconds()
          < PropertyConstants.RESEND_DELAY_SECONDS) {
        throw new OtpException(
            String.format(
                PropertyConstants.ERROR_RESEND_DELAY, PropertyConstants.RESEND_DELAY_SECONDS));
      }

      // Generate new OTP if expired, otherwise use existing one
      if (existingOtp.getExpiresAt().isBefore(now)) {
        log.info("OTP expired, generating new OTP.");
        String newOtp = OtpUtils.generateOtp();
        existingOtp.setOtpCode(newOtp);
      } else {
        log.info("Resending existing OTP.");
      }
      existingOtp.setCreatedAt(now);
      existingOtp.setExpiresAt(now.plusMinutes(PropertyConstants.OTP_EXPIRY_MINUTES));
      existingOtp.setResendAttempts(existingOtp.getResendAttempts() + 1);
      otpRequestRepository.save(existingOtp);

      // Send OTP via email
      sendOtpEmail(user.getEmail(), existingOtp.getOtpCode(), false, "");
      log.info("OTP resent successfully to {}", user.getEmail());
      future.complete(null);
    } catch (OtpException exception) {
      future.completeExceptionally(exception);
    } catch (Exception e) {
      log.info("Exception occurred during OTP resend", e);
      future.completeExceptionally(e);
    }
    return future;
  }

  // Send OTP email
  private void sendOtpEmail(String to, String otp, boolean isDefaultPassword, String username) {
    String emailBody =
        isDefaultPassword
            ? String.format(
                PropertyConstants.USER_INVITE_BODY,
                username,
                PropertyConstants.DEFAULT_PASSWORD,
                otp,
                username,
                PropertyConstants.VERIFY_OTP_URL)
            : String.format("Your OTP code is: %s", otp);

    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(to);
    if (isDefaultPassword) {
      message.setSubject("Welcome onboard");
    } else {
      message.setSubject("Property management OTP");
    }
    message.setText(emailBody);
    mailSender.send(message);
  }

  public void sendResetPasswordLink(String to, String resetPasswordLink) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(to);
    message.setSubject("Reset Password");
    message.setText(
        "Please click on the following link to reset your password: " + resetPasswordLink);

    mailSender.send(message);
  }
}
