package com.sc.property.management.config;

import com.sc.property.management.repository.UserRepository;
import com.sc.property.management.service.CustomUserDetailsService;
import com.sc.property.management.util.JwtUtil;
import com.sc.property.management.util.PropertyConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

  @Autowired private CustomUserDetailsService userDetailsService;

  @Autowired private JwtUtil jwtUtil;

  @Autowired UserRepository userRepository;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    final String authorizationHeader = request.getHeader("Authorization");

    String username = null;
    String jwt = null;
    try {
      if (authorizationHeader != null
          && authorizationHeader.startsWith(PropertyConstants.JWT_PREFIX)) {
        jwt = authorizationHeader.substring(7);
        System.out.println("Extracted JWT: " + jwt);
        String sub = jwtUtil.extractSub(jwt);
        username = userRepository.findByEmail(sub).get().getUsername();
        System.out.println("Username after JWT extraction is: " + username);
      }

      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

        UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

        if (jwtUtil.validateToken(jwt, userDetails)) {

          UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
              new UsernamePasswordAuthenticationToken(
                  userDetails, null, userDetails.getAuthorities());
          usernamePasswordAuthenticationToken.setDetails(
              new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        }
      }
      chain.doFilter(request, response);
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }
}
