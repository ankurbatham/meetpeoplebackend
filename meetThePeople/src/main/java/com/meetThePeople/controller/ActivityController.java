package com.meetThePeople.controller;

import com.meetThePeople.dto.ApiResponseDto;
import com.meetThePeople.entity.User;
import com.meetThePeople.entity.UserActivity;
import com.meetThePeople.service.UserActivityService;
import com.meetThePeople.service.UserService;
import com.meetThePeople.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/activity")
@CrossOrigin(origins = "*")
@Slf4j
public class ActivityController {
    
    private final UserActivityService activityService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    
    public ActivityController(UserActivityService activityService, UserService userService, JwtUtil jwtUtil) {
        this.activityService = activityService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }
    
    @PostMapping("/capture")
    public ResponseEntity<ApiResponseDto<String>> captureActivity(@RequestParam("source") UserActivity.ActiveSource source,
                                                                @RequestHeader(value = "Authorization", required = false) String token,
                                                                @RequestParam(value = "mobile", required = false) String mobile) {
        try {
            log.info("captureActivity source: {} token: {} mobile: {}", source, token != null ? "present" : "absent", mobile);
            
            if (token != null && token.startsWith("Bearer ")) {
                try {
                    // Authenticated user - capture activity with user ID
                    String userMobile = jwtUtil.getMobileFromToken(token.replace("Bearer ", ""));
                    User user = userService.findByMobile(userMobile)
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    
                    activityService.captureActivity(user.getId(), source);
                    log.info("Activity captured for authenticated user: {}", user.getId());
                    return ResponseEntity.ok(ApiResponseDto.success("Activity captured successfully for user", null));
                } catch (Exception e) {
                    log.warn("Failed to capture activity for authenticated user: {}", e.getMessage());
                    // Fall back to anonymous activity capture
                }
            }
            
            // Try to capture activity with mobile if provided (for pre-login tracking)
            if (mobile != null && !mobile.trim().isEmpty()) {
                try {
                    Optional<User> user = userService.findByMobile(mobile);
                    if (user.isPresent()) {
                        // User exists but no valid token - capture activity with user ID
                        activityService.captureActivity(user.get().getId(), source);
                        log.info("Activity captured for user by mobile (no token): {}", user.get().getId());
                        return ResponseEntity.ok(ApiResponseDto.success("Activity captured successfully for user", null));
                    }
                } catch (Exception e) {
                    log.warn("Failed to capture activity by mobile: {}", e.getMessage());
                }
            }
            
            // Anonymous user or fallback - capture activity without user ID
            activityService.captureAnonymousActivity(source);
            log.info("Activity captured for anonymous user from source: {}", source);
            return ResponseEntity.ok(ApiResponseDto.success("Activity captured successfully", null));
            
        } catch (Exception e) {
            log.error("Failed to capture activity: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to capture activity: " + e.getMessage()));
        }
    }
    
    @GetMapping("/status")
    public ResponseEntity<ApiResponseDto<UserActivity>> getActivityStatus(@RequestHeader("Authorization") String token) {
        try {
            String mobile = jwtUtil.getMobileFromToken(token.replace("Bearer ", ""));
            User user = userService.findByMobile(mobile)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            UserActivity activity = activityService.getUserActivity(user.getId())
                    .orElse(null);
            
            return ResponseEntity.ok(ApiResponseDto.success("Activity status retrieved successfully", activity));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get activity status: " + e.getMessage()));
        }
    }
    
    /**
     * Health check endpoint for activity service
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponseDto<String>> checkActivityServiceHealth() {
        try {
            log.info("Activity service health check");
            return ResponseEntity.ok(ApiResponseDto.success("Activity service is healthy", "UP"));
        } catch (Exception e) {
            log.error("Activity service health check failed", e);
            return ResponseEntity.status(503).body(ApiResponseDto.error("Activity service is down"));
        }
    }
    
    /**
     * Capture activity without authentication (for app initialization)
     */
    @PostMapping("/ping")
    public ResponseEntity<ApiResponseDto<String>> pingActivity(@RequestParam("source") UserActivity.ActiveSource source) {
        try {
            log.info("Ping activity from source: {}", source);
            activityService.captureAnonymousActivity(source);
            return ResponseEntity.ok(ApiResponseDto.success("Ping activity captured", null));
        } catch (Exception e) {
            log.error("Failed to capture ping activity: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to capture ping activity: " + e.getMessage()));
        }
    }
} 