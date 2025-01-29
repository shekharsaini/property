package com.sc.property.management.repository;

import com.sc.property.management.datasource.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  Optional<User> findByPhone(String phone);

  Optional<User> findByResetToken(String token);

  Optional<User> findByUsernameOrEmail(String username, String email);

  @Query(
      value =
          "SELECT * FROM users u WHERE "
              + "(:username IS NULL OR CAST(u.username AS text) ILIKE CONCAT(:username, '%')) AND "
              + "(:email IS NULL OR CAST(u.email AS text) ILIKE CONCAT(:email, '%')) AND "
              + "(:userId IS NULL OR u.id = :userId) AND " // Add filter for userId
              + "(:usertype IS NULL OR u.user_type = :usertype)",
      nativeQuery = true)
  Page<User> findByFiltersNative(
      @Param("username") String username,
      @Param("email") String email,
      @Param("userId") Long userId, // Add userId parameter
      @Param("usertype") String userType,
      Pageable pageable);
}
