package com.sc.property.management.service;

import com.sc.property.management.datasource.Address;
import com.sc.property.management.datasource.OtpRequest;
import com.sc.property.management.datasource.User;
import com.sc.property.management.datasource.Vehicle;
import com.sc.property.management.dto.*;
import com.sc.property.management.exception.CustomValidationException;
import com.sc.property.management.exception.InvalidTokenException;
import com.sc.property.management.exception.UserNotFoundException;
import com.sc.property.management.repository.OtpRequestRepository;
import com.sc.property.management.repository.UserRepository;
import com.sc.property.management.util.JwtUtil;
import com.sc.property.management.util.PropertyConstants;
import com.sc.property.management.util.UserType;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

  @Autowired private UserRepository userRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private OtpService otpService;

  @Autowired private OtpRequestRepository otpRequestRepository;

  @Autowired private AddressValidationService addressValidationService;

  @Autowired private JwtUtil jwtUtil;

  @Value("${reset.password.url}")
  private String resetPasswordUrl;

  @Override
  public User registerUser(UserDto userDto) {
    if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
      throw new CustomValidationException(PropertyConstants.ERROR_EMAIL_IN_USE);
    }
    if (userRepository.findByPhone(userDto.getPhone()).isPresent()) {
      throw new CustomValidationException(PropertyConstants.ERROR_PHONE_IN_USE);
    }
    // Validate email format
    if (!isValidEmail(userDto.getEmail())) {
      throw new CustomValidationException(PropertyConstants.ERROR_INVALID_EMAIL_FORMAT);
    }
    // Validate password strength
    if (!isStrongPassword(userDto.getPassword())) {
      throw new CustomValidationException(PropertyConstants.ERROR_WEAK_PASSWORD);
    }
    // Valid user types TENANT and AGENT
    if (!isValidUserType(userDto.getUserType())) {
      throw new CustomValidationException(
          "Invalid userType: "
              + userDto.getUserType()
              + ". Only TENANT, LANDLORD, ADMIN, AGENT user type are allowed.");
    }
    User user =
        User.builder()
            .email(userDto.getEmail())
            .username(
                Objects.isNull(userDto.getUsername()) ? userDto.getEmail() : userDto.getUsername())
            .password(passwordEncoder.encode(userDto.getPassword()))
            .phone(userDto.getPhone())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .userType(userDto.getUserType())
            .isEmailVerified(false)
            .isPhoneVerified(false)
            .build();

    User registeredUser = userRepository.save(user);
    log.info("generating otp after adding data in to DB " + registeredUser.getId());
    otpService.generateAndSendEmailOtp(registeredUser.getId());
    //    otpService.generateAndSendSmsOtp(registeredUser.getId());
    return registeredUser;
  }

  public boolean isValidUserType(String value) {
    try {
      UserType.valueOf(value.toUpperCase());
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  // Method to validate email format using regex
  private boolean isValidEmail(String email) {
    String emailRegex = PropertyConstants.EMAIL_REGEX;
    Pattern pattern = Pattern.compile(emailRegex);
    return pattern.matcher(email).matches();
  }

  // Method to validate password strength
  private boolean isStrongPassword(String password) {
    // Minimum 8 characters, at least one uppercase letter, one digit, and one special character
    String passwordRegex = PropertyConstants.PASSWORD_REGEX;
    Pattern pattern = Pattern.compile(passwordRegex);
    return pattern.matcher(password).matches();
  }

  @Override
  public void verifyEmailOtp(String email, String otp, String otpType) {
    // Logic for OTP verification
    Optional<User> optionalUser = userRepository.findByEmail(email);
    if (optionalUser.isEmpty()) {
      throw new CustomValidationException(PropertyConstants.ERROR_EMAIL_NOT_FOUND);
    }
    OtpRequest otpRequest =
        otpRequestRepository
            .findByUserIdAndOtpCodeAndOtpType(optionalUser.get().getId(), otp, otpType)
            .orElse(null);

    if (otpRequest == null
        || otpRequest.isVerified()
        || otpRequest.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw new CustomValidationException(PropertyConstants.ERROR_INVALID_OTP);
    }

    otpRequest.setVerified(true);
    otpRequestRepository.save(otpRequest);
  }

  @Override
  public void verifyPhoneOtp(String phone, String otp) {
    // Logic for OTP verification
  }

  @Override
  public UpdateUserDto updateUser(UpdateUserDto userDto) {
    Optional<User> optionalUser = userRepository.findByUsername(userDto.getUserName());
    if (optionalUser.isEmpty()) {
      throw new UserNotFoundException(PropertyConstants.ERROR_USER_NOT_FOUND);
    }
    User user = optionalUser.get();
    user =
        user.toBuilder()
            .emergencyContactNumber(userDto.getEmergencyContactNumber())
            .emergencyContactName(userDto.getEmergencyContactName())
            .emergencyContactEmail(userDto.getEmergencyContactEmail())
            .emergencyContactAddress(mapToAddressDatasource(userDto))
            .userType(userDto.getUserType().name())
            .vehicle(
                Objects.nonNull(userDto.getVehicleDto().getLicenseNumber())
                    ? mapToVehicleDatasource(userDto.getVehicleDto())
                    : null)
            .build();
    User updatedUser = userRepository.save(user);

    return mapToUserDto(updatedUser);
  }

  Address mapToAddressDatasource(UpdateUserDto userDto) {

    return Address.builder()
        .city(userDto.getEmergencyContactAddress().getCity())
        .country(userDto.getEmergencyContactAddress().getCountry())
        .postalCode(userDto.getEmergencyContactAddress().getZipcode())
        .state(userDto.getEmergencyContactAddress().getState())
        .street(userDto.getEmergencyContactAddress().getStreet())
        .build();
  }

  Vehicle mapToVehicleDatasource(VehicleDto vehicleDto) {
    Vehicle vehicle = new Vehicle();
    vehicle.setColor(vehicleDto.getColor());
    vehicle.setMake(vehicleDto.getMake());
    vehicle.setModel(vehicleDto.getModel());
    vehicle.setLicenseNumber(vehicleDto.getLicenseNumber());
    vehicle.setModelYear(vehicleDto.getModelYear());
    return vehicle;
  }

  UpdateUserDto mapToUserDto(User user) {
    UpdateUserDto updateUserDto = new UpdateUserDto();
    updateUserDto.setUserId(String.valueOf(user.getId()));
    updateUserDto.setUserType(UserType.valueOf(user.getUserType()));
    updateUserDto.setUserName(user.getUsername());
    updateUserDto.setEmail(user.getEmail());
    updateUserDto.setEmergencyContactAddress(mapToAddressDto(user.getEmergencyContactAddress()));
    updateUserDto.setEmergencyContactEmail(user.getEmergencyContactEmail());
    updateUserDto.setEmergencyContactName(user.getEmergencyContactName());
    updateUserDto.setEmergencyContactNumber(user.getEmergencyContactNumber());
    updateUserDto.setVehicleDto(mapToVehicleDto(user.getVehicle()));
    return updateUserDto;
  }

  VehicleDto mapToVehicleDto(Vehicle vehicle) {
    if (Objects.isNull(vehicle)) {
      return new VehicleDto();
    }
    VehicleDto vehicleDto = new VehicleDto();
    vehicleDto.setColor(vehicle.getColor());
    vehicleDto.setMake(vehicle.getMake());
    vehicleDto.setLicenseNumber(vehicle.getLicenseNumber());
    vehicleDto.setModelYear(vehicle.getModelYear());
    vehicleDto.setModel(vehicle.getModel());
    return vehicleDto;
  }

  AddressValidationRequest mapToAddressDto(Address address) {
    if (Objects.isNull(address)) {
      return AddressValidationRequest.builder().build();
    }
    return AddressValidationRequest.builder()
        .zipcode(address.getPostalCode())
        .street(address.getStreet())
        .city(address.getCity())
        .state(address.getState())
        .country(address.getCountry())
        .build();
  }

  @Override
  public void processForgetPassword(String usernameOrEmail) {
    String email;
    Optional<User> optionalUser;

    if (isValidEmail(usernameOrEmail)) {
      optionalUser = userRepository.findByEmail(usernameOrEmail);
    } else {
      optionalUser =
          userRepository
              .findByUsername(usernameOrEmail)
              .map(User::getEmail)
              .flatMap(userRepository::findByEmail);
    }

    User user =
        optionalUser.orElseThrow(
            () -> new UserNotFoundException(PropertyConstants.ERROR_USER_NOT_FOUND));
    if (Objects.nonNull(user.getResetToken())
        && user.getResetTokenExpiry().isAfter(LocalDateTime.now())) {
      throw new CustomValidationException(PropertyConstants.RESET_PASSWORD_LINK_ALREADY_SENT);
    }
    email = user.getEmail();
    String resetToken = jwtUtil.generateResetToken(email); // Generate token
    LocalDateTime expiryDate =
        LocalDateTime.now().plusMinutes(PropertyConstants.RESET_TOKEN_EXPIRY_TIME);

    user.setResetToken(resetToken);
    user.setResetTokenExpiry(expiryDate);
    user.setUpdatedAt(LocalDateTime.now());
    userRepository.save(user);

    String resetLink = resetPasswordUrl + resetToken;
    otpService.sendResetPasswordLink(email, resetLink);
  }

  @Override
  public void resetPassword(String token, ResetPasswordDto resetPasswordDto) {
    User user =
        userRepository
            .findByResetToken(token)
            .orElseThrow(() -> new InvalidTokenException(PropertyConstants.INVALID_RESET_TOKEN));

    boolean isValidToken = jwtUtil.validateResetToken(token, user.getEmail());

    if (!isValidToken || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
      throw new InvalidTokenException(PropertyConstants.INVALID_RESET_TOKEN);
    }
    // Validate password strength
    if (!isStrongPassword(resetPasswordDto.getNewPassword())) {
      throw new CustomValidationException(PropertyConstants.ERROR_WEAK_PASSWORD);
    }
    user.setPassword(
        passwordEncoder.encode(
            resetPasswordDto.getNewPassword())); // Ensure a PasswordEncoder is autowired
    user.setResetToken(null);
    user.setResetTokenExpiry(null);
    user.setUpdatedAt(LocalDateTime.now());
    userRepository.save(user);
  }

  @Override
  public Page<UpdateUserDto> getAllUsers(
      String username, String email, Long userId, String usertype, Pageable pageable) {

    Page<User> userPage;

    // Check if all filters are null or empty
    if ((username == null || username.isEmpty())
        && (email == null || email.isEmpty())
        && (usertype == null || usertype.isEmpty())
        && userId == null) {
      // Fetch all users if no filters are provided
      userPage = userRepository.findAll(pageable);
    } else {
      // Apply filters using the repository
      userPage =
          userRepository.findByFiltersNative(
              username,
              email,
              userId, // Pass userId as a parameter
              usertype != null ? usertype.toUpperCase() : null,
              pageable);
    }

    return userPage.map(this::mapToUserDto);
  }

  @Override
  public UpdateUserDto getUserDetailsByUsername(String username) {

    Optional<User> optionalUser = userRepository.findByUsername(username);
    if (optionalUser.isEmpty()) {
      throw new UserNotFoundException(PropertyConstants.ERROR_USER_NOT_FOUND);
    }
    User user = optionalUser.get();
    return mapToUserDto(user);
  }

  @Override
  public UpdateUserDto inviteUser(InviteRequest inviteRequest) {
    // Check if the user already exists by email or username
    Optional<User> existingUser =
        userRepository.findByUsernameOrEmail(inviteRequest.getUsername(), inviteRequest.getEmail());
    if (existingUser.isPresent()) {
      throw new CustomValidationException(
          "User already exists with email or username: " + inviteRequest.getEmail());
    }
    if (userRepository.findByPhone(inviteRequest.getPhoneNo()).isPresent()) {
      throw new CustomValidationException(PropertyConstants.ERROR_PHONE_IN_USE);
    }
    // Validate email format
    if (!isValidEmail(inviteRequest.getEmail())) {
      throw new CustomValidationException(PropertyConstants.ERROR_INVALID_EMAIL_FORMAT);
    }
    // Valid user types TENANT and AGENT
    if (!inviteRequest.getUserType().equalsIgnoreCase(UserType.AGENT.name())
        && !inviteRequest.getUserType().equalsIgnoreCase(UserType.TENANT.name())) {
      throw new CustomValidationException(
          "Invalid userType: "
              + inviteRequest.getUserType()
              + ". Only AGENT or TENANT are allowed.");
    }
    User newUser =
        User.builder()
            .email(inviteRequest.getEmail())
            .email(inviteRequest.getEmail())
            .username(inviteRequest.getUsername())
            .phone(inviteRequest.getPhoneNo())
            .password(passwordEncoder.encode(PropertyConstants.DEFAULT_PASSWORD))
            .userType(inviteRequest.getUserType()) // OWNER, AGENT, or TENANT
            .createdAt(LocalDateTime.now())
            .build();
    User invitedUser = userRepository.save(newUser);

    // Return the created user's details as UserDto
    log.info("generating otp after adding data in to DB " + invitedUser.getId());
    otpService.generateAndSendEmailOtp(invitedUser.getId());
    return mapToUserDto(invitedUser);
  }
}
