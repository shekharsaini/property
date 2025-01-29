package com.sc.property.management.service;

import com.sc.property.management.datasource.User;
import com.sc.property.management.exception.UserNotFoundException;
import com.sc.property.management.repository.UserRepository;
import com.sc.property.management.util.UserType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  @Autowired private UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Optional<User> user = userRepository.findByUsername(username); // Use email instead of username
    if (user.isEmpty()) {
      throw new UserNotFoundException("User not found : " + username);
    }
    User userDetails = user.get();
    // Add authorities based on usertype
    List<GrantedAuthority> authorities = new ArrayList<>();
    if (UserType.ADMIN.name().equals(userDetails.getUserType())) {
      authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
    if (UserType.LANDLORD.name().equals(userDetails.getUserType())) {
      authorities.add(new SimpleGrantedAuthority("ROLE_LANDLORD"));
    }
    if (UserType.AGENT.name().equals(userDetails.getUserType())) {
      authorities.add(new SimpleGrantedAuthority("ROLE_AGENT"));
    }
    return new org.springframework.security.core.userdetails.User(
        userDetails.getEmail(),
        userDetails.getPassword(),
        authorities); // Convert your user to a Spring Security user
  }
}
