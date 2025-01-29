package com.sc.property.management.service;

import com.sc.property.management.datasource.User;
import com.sc.property.management.dto.AuthRequest;
import com.sc.property.management.dto.AuthResponse;
import com.sc.property.management.exception.AuthorizationException;
import com.sc.property.management.repository.UserRepository;
import com.sc.property.management.util.JwtUtil;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

  @Autowired private AuthenticationManager authenticationManager;

  @Autowired private UserDetailsService userDetailsService;

  @Autowired private JwtUtil jwtUtil;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private UserRepository userRepository;

  @Override
  public AuthResponse validateUser(AuthRequest authRequest) {
    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              authRequest.getUsername(), authRequest.getPassword()));
    } catch (AuthenticationException e) {
      //            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or
      // password");
      throw new AuthorizationException("Invalid username or password");
    }
    final UserDetails userDetails =
        userDetailsService.loadUserByUsername(authRequest.getUsername());
    Optional<User> user = userRepository.findByUsername(authRequest.getUsername());
    if (user.isPresent() && !user.get().isEmailVerified()) {
      throw new AuthorizationException("kindly verify your email address " + user.get().getEmail());
    }
    //    if (user.isPresent() && !user.get().isPhoneVerified()) {
    //      throw new UsernameNotFoundException("kindly verify your phone no ");
    //    }
    if (!passwordEncoder.matches(authRequest.getPassword(), userDetails.getPassword())) {
      throw new AuthorizationException("Invalid username or password");
    }
    return new AuthResponse(
        jwtUtil.generateToken(userDetails),
        String.valueOf(user.get().getId()),
        user.get().getUserType());
  }
}
