package com.sc.property.management.repository;

import com.sc.property.management.datasource.OtpRequest;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpRequestRepository extends JpaRepository<OtpRequest, Long> {

  @Query(
      value =
          "select o from OtpRequest o where o.user.id=:userId and otpCode=:otp and otpType=:otpType")
  Optional<OtpRequest> findByUserIdAndOtpCodeAndOtpType(
      @Param("userId") Long userId, @Param("otp") String otp, @Param("otpType") String otpType);

  @Query("SELECT o FROM OtpRequest o WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
  Optional<OtpRequest> findLatestOtpByUser(@Param("userId") Long userId);
}
