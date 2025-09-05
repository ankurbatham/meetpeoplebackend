package com.meetThePeople.repository;

import com.meetThePeople.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    
    Optional<OtpVerification> findByMobileAndOtpAndIsVerifiedFalse(String mobile, String otp);
    
    List<OtpVerification> findByMobileAndExpiryTimeAfter(String mobile, LocalDateTime currentTime);
    
    void deleteByMobile(String mobile);
} 