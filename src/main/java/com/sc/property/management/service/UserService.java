package com.sc.property.management.service;

import com.sc.property.management.datasource.User;
import com.sc.property.management.dto.InviteRequest;
import com.sc.property.management.dto.ResetPasswordDto;
import com.sc.property.management.dto.UpdateUserDto;
import com.sc.property.management.dto.UserDto;
import com.sc.property.management.util.UserType;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
  User registerUser(UserDto userDto);

  void verifyEmailOtp(String email, String otp, String otpType);

  void verifyPhoneOtp(String phone, String otp);

  UpdateUserDto updateUser(UpdateUserDto userDto);

  void processForgetPassword(@NotBlank String usernameOrEmail);

  void resetPassword(String token, ResetPasswordDto resetPasswordDto);

  Page<UpdateUserDto> getAllUsers(
      String userName, String email, Long userId, String userType, Pageable pageable);

  UpdateUserDto getUserDetailsByUsername(String username);

  UpdateUserDto inviteUser(InviteRequest inviteRequest);
}
