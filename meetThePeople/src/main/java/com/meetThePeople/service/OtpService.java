package com.meetThePeople.service;

import com.meetThePeople.entity.OtpVerification;
import com.meetThePeople.repository.OtpVerificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@Transactional
@Slf4j
public class OtpService {
    
    private final OtpVerificationRepository otpVerificationRepository;
    
    @Value("${app.otp.expiry-minutes}")
    private int otpExpiryMinutes;
    
    @Value("${app.otp.length}")
    private int otpLength;
    
    public OtpService(OtpVerificationRepository otpVerificationRepository) {
        this.otpVerificationRepository = otpVerificationRepository;
    }
    
    @Transactional
    public String generateOtp(String mobile) {
        log.info("generateOtp: {}", mobile);
        
        try {
            // Generate random OTP
            String otp = generateRandomOtp();
            
            // Set expiry time
            LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(otpExpiryMinutes);
            
            // Delete any existing OTP for this mobile
            log.info("Deleting existing OTP for mobile: {}", mobile);
            otpVerificationRepository.deleteByMobile(mobile);
            
            // Save new OTP
            OtpVerification otpVerification = new OtpVerification();
            otpVerification.setMobile(mobile);
            otpVerification.setOtp(otp);
            otpVerification.setExpiryTime(expiryTime);
            otpVerification.setIsVerified(false);
            
            log.info("Saving new OTP for mobile: {}", mobile);
            OtpVerification savedOtp = otpVerificationRepository.save(otpVerification);
            log.info("OTP saved successfully with ID: {}", savedOtp.getId());
            
            // Mock SMS service - in real implementation, integrate with SMS gateway
            sendMockSms(mobile, otp);
            
            return otp;
        } catch (Exception e) {
            log.error("Error generating OTP for mobile: {}", mobile, e);
            throw new RuntimeException("Failed to generate OTP: " + e.getMessage(), e);
        }
    }
    
    @Transactional
    public boolean verifyOtp(String mobile, String otp) {
        log.info("verifyOtp: mobile={}, otp={}", mobile, otp);
        
        try {
            OtpVerification otpVerification = otpVerificationRepository
                    .findByMobileAndOtpAndIsVerifiedFalse(mobile, otp)
                    .orElse(null);
            
            if (otpVerification == null) {
                log.info("OTP verification failed: No valid OTP found for mobile: {}", mobile);
                return false;
            }
            
            // Check if OTP is expired
            if (LocalDateTime.now().isAfter(otpVerification.getExpiryTime())) {
                log.info("OTP verification failed: OTP expired for mobile: {}", mobile);
                return false;
            }
            
            // Mark OTP as verified
            otpVerification.setIsVerified(true);
            OtpVerification savedOtp = otpVerificationRepository.save(otpVerification);
            log.info("OTP verified successfully for mobile: {}, OTP ID: {}", mobile, savedOtp.getId());
            
            return true;
        } catch (Exception e) {
            log.error("Error verifying OTP for mobile: {}", mobile, e);
            throw new RuntimeException("Failed to verify OTP: " + e.getMessage(), e);
        }
    }
    
    private String generateRandomOtp() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
    
    private void sendMockSms(String mobile, String otp) {
        // Mock SMS service - in real implementation, integrate with SMS gateway
        log.info("Mock SMS sent to {}: Your OTP is {}", mobile, otp);
        System.out.println("Mock SMS sent to " + mobile + ": Your OTP is " + otp);
    }
    
    /**
     * Health check method to verify OTP service is working
     */
    public boolean isServiceHealthy() {
        try {
            // Try to perform a simple database operation
            long count = otpVerificationRepository.count();
            log.info("OTP service health check: Found {} existing OTP records", count);
            return true;
        } catch (Exception e) {
            log.error("OTP service health check failed", e);
            return false;
        }
    }
    
    /**
     * Test database connection and table existence
     */
    public Map<String, Object> testDatabaseConnection() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Test basic repository operations
            long count = otpVerificationRepository.count();
            result.put("tableExists", true);
            result.put("recordCount", count);
            result.put("status", "SUCCESS");
            
            // Test if we can save a test record
            OtpVerification testOtp = new OtpVerification();
            testOtp.setMobile("9999999999");
            testOtp.setOtp("000000");
            testOtp.setExpiryTime(LocalDateTime.now().plusMinutes(5));
            testOtp.setIsVerified(false);
            
            OtpVerification saved = otpVerificationRepository.save(testOtp);
            result.put("canSave", true);
            result.put("savedId", saved.getId());
            
            // Clean up test record
            otpVerificationRepository.deleteById(saved.getId());
            result.put("canDelete", true);
            
        } catch (Exception e) {
            result.put("tableExists", false);
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
            log.error("Database connection test failed", e);
        }
        
        return result;
    }
} 