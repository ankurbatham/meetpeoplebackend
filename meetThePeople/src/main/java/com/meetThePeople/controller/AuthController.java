package com.meetThePeople.controller;

import com.meetThePeople.dto.ApiResponseDto;
import com.meetThePeople.dto.LoginRequestDto;
import com.meetThePeople.dto.OtpRequestDto;
import com.meetThePeople.entity.User;
import com.meetThePeople.service.OtpService;
import com.meetThePeople.service.UserService;
import com.meetThePeople.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final OtpService otpService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    
    public AuthController(OtpService otpService, UserService userService, JwtUtil jwtUtil) {
        this.otpService = otpService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }
    
    @PostMapping("/generate-otp")
    public ResponseEntity<ApiResponseDto<String>> generateOtp(@Valid @RequestBody OtpRequestDto request) {
        try {
            String otp = otpService.generateOtp(request.getMobile());
            return ResponseEntity.ok(ApiResponseDto.success("OTP generated successfully", otp));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to generate OTP: " + e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> login(@Valid @RequestBody LoginRequestDto request) {
        try {
            // Verify OTP
            boolean isValidOtp = otpService.verifyOtp(request.getMobile(), request.getOtp());
            
            if (!isValidOtp) {
                return ResponseEntity.badRequest().body(ApiResponseDto.error("Invalid or expired OTP"));
            }
            
            // Find or create user
            User user = userService.findByMobile(request.getMobile())
                    .orElseGet(() -> userService.createUser(request.getMobile()));
            
            // Generate JWT token
            String token = jwtUtil.generateToken(user.getMobile());
            
            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", user);
            
            return ResponseEntity.ok(ApiResponseDto.success("Login successful", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Login failed: " + e.getMessage()));
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<ApiResponseDto<Object>> checkOtpServiceHealth() {
        try {
            boolean isHealthy = otpService.isServiceHealthy();
            Map<String, Object> healthInfo = new HashMap<>();
            healthInfo.put("otpService", isHealthy ? "UP" : "DOWN");
            healthInfo.put("timestamp", java.time.LocalDateTime.now());
            
            if (isHealthy) {
                return ResponseEntity.ok(ApiResponseDto.success("OTP service is healthy", healthInfo));
            } else {
                return ResponseEntity.status(503).body(ApiResponseDto.error("OTP service is down"));
            }
        } catch (Exception e) {
            Map<String, Object> healthInfo = new HashMap<>();
            healthInfo.put("otpService", "ERROR");
            healthInfo.put("error", e.getMessage());
            healthInfo.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.status(503).body(ApiResponseDto.error("OTP service health check failed"));
        }
    }
    
    @GetMapping("/test-db")
    public ResponseEntity<ApiResponseDto<Object>> testDatabaseConnection() {
        try {
            Map<String, Object> dbTestResult = otpService.testDatabaseConnection();
            return ResponseEntity.ok(ApiResponseDto.success("Database connection test completed", dbTestResult));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponseDto.error("Database connection test failed: " + e.getMessage()));
        }
    }
    
    @GetMapping("/jwt-status")
    public ResponseEntity<ApiResponseDto<Object>> checkJwtStatus() {
        try {
            Map<String, Object> jwtInfo = new HashMap<>();
            jwtInfo.put("keyInfo", jwtUtil.getKeyInfo());
            jwtInfo.put("expiration", jwtUtil.getExpiration());
            jwtInfo.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(ApiResponseDto.success("JWT status check completed", jwtInfo));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponseDto.error("JWT status check failed: " + e.getMessage()));
        }
    }
    
    /**
     * Test endpoint to verify JWT authentication is working
     * This endpoint requires authentication
     */
    @GetMapping("/test-auth")
    public ResponseEntity<ApiResponseDto<Object>> testAuthentication() {
        try {
            Map<String, Object> authInfo = new HashMap<>();
            authInfo.put("authenticated", true);
            authInfo.put("message", "JWT authentication is working correctly");
            authInfo.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(ApiResponseDto.success("Authentication test successful", authInfo));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponseDto.error("Authentication test failed: " + e.getMessage()));
        }
    }
} 