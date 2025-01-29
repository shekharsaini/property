package com.sc.property.management.datasource;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@Entity
@Table(name = "users")
@Data
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Email
  @NotBlank
  @Column(unique = true)
  private String email;

  @NotBlank
  @Column(unique = true)
  private String username;

  @NotBlank
  @Size(min = 8)
  private String password;

  @NotBlank
  @Column(unique = true)
  private String phone;

  @Setter private boolean isEmailVerified;
  @Setter private boolean isPhoneVerified;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "emergency_contact_address", referencedColumnName = "id")
  private Address emergencyContactAddress;

  @Column(name = "emergency_contact_name")
  private String emergencyContactName;

  @Column(name = "emergency_contact_email")
  private String emergencyContactEmail;

  @Column(name = "emergency_contact_number")
  private Long emergencyContactNumber;

  @Column(name = "user_type")
  private String userType;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "vehicle_id", referencedColumnName = "id")
  private Vehicle vehicle;

  @Column(name = "reset_token")
  private String resetToken;

  @Column(name = "reset_token_expiry")
  private LocalDateTime resetTokenExpiry;
}
