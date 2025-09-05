package com.meetThePeople.controller;

import com.meetThePeople.dto.ApiResponseDto;
import com.meetThePeople.dto.SearchUserDto;
import com.meetThePeople.dto.UserProfileDto;
import com.meetThePeople.dto.UserSearchResultDto;
import com.meetThePeople.dto.UserProfileResponseDto;
import com.meetThePeople.dto.UserCommunicationDto;
import com.meetThePeople.entity.User;
import com.meetThePeople.entity.UserActivity;
import com.meetThePeople.service.UserService;
import com.meetThePeople.util.JwtUtil;
import com.meetThePeople.util.OnlineStatusUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
@Slf4j
public class UserController {
    
    private final UserService userService;
    private final JwtUtil jwtUtil;
    
    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }
    
    @GetMapping("/profile")
    public ResponseEntity<ApiResponseDto<UserProfileResponseDto>> getProfile(@RequestHeader("Authorization") String token) {
        try {
            String mobile = jwtUtil.getMobileFromToken(token.replace("Bearer ", ""));
            log.info("getProfile mobile: {}", mobile);
            User user = userService.findByMobile(mobile)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            log.info("getProfile user: {}", user);
            UserProfileResponseDto profileWithActivity = userService.getUserProfileWithActivity(user.getId());
            log.info("Profile retrieved successfully for user: {}", user.getId());
            return ResponseEntity.ok(ApiResponseDto.success("Profile retrieved successfully", profileWithActivity));
        } catch (Exception e) {
            log.error("Failed to get profile for user: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get profile: " + e.getMessage()));
        }
    }
    
    @PutMapping("/profile")
    public ResponseEntity<ApiResponseDto<UserProfileResponseDto>> updateProfile(@RequestHeader("Authorization") String token,
                                                                             @Valid @RequestBody UserProfileDto profileDto) {
        try {
            String mobile = jwtUtil.getMobileFromToken(token.replace("Bearer ", ""));
            User user = userService.findByMobile(mobile)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            User updatedUser = userService.updateProfile(user.getId(), profileDto);
            UserProfileResponseDto profileResponse = UserProfileResponseDto.fromUserAndActivity(updatedUser, null);
            return ResponseEntity.ok(ApiResponseDto.success("Profile updated successfully", profileResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to update profile: " + e.getMessage()));
        }
    }
    
    @PostMapping("/search")
    public ResponseEntity<ApiResponseDto<List<UserSearchResultDto>>> searchUsers(@RequestHeader("Authorization") String token,
                                                                              @RequestBody SearchUserDto searchDto) {
        try {
            String mobile = jwtUtil.getMobileFromToken(token.replace("Bearer ", ""));
            User currentUser = userService.findByMobile(mobile)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            List<UserSearchResultDto> users = userService.searchUsersWithActivity(currentUser.getId(), searchDto);
            return ResponseEntity.ok(ApiResponseDto.success("Users found successfully", users));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to search users: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{userId}/profile")
    public ResponseEntity<ApiResponseDto<UserProfileResponseDto>> getUserProfile(@RequestHeader("Authorization") String token,
                                                                              @PathVariable Long userId) {
        try {
            String mobile = jwtUtil.getMobileFromToken(token.replace("Bearer ", ""));
            userService.findByMobile(mobile)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            UserProfileResponseDto profileWithActivity = userService.getUserProfileWithActivity(userId);
            return ResponseEntity.ok(ApiResponseDto.success("User profile retrieved successfully", profileWithActivity));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get user profile: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{userId}/communication")
    public ResponseEntity<ApiResponseDto<UserCommunicationDto>> getUserCommunicationDetails(@RequestHeader("Authorization") String token,
                                                                                          @PathVariable Long userId) {
        try {
            String mobile = jwtUtil.getMobileFromToken(token.replace("Bearer ", ""));
            Long currentUserId = userService.findByMobile(mobile)
                    .orElseThrow(() -> new RuntimeException("User not found"))
                    .getId();
            
            UserCommunicationDto communicationDetails = userService.getUserCommunicationDetails(currentUserId, userId);
            return ResponseEntity.ok(ApiResponseDto.success("User communication details retrieved successfully", communicationDetails));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get user communication details: " + e.getMessage()));
        }
    }
    
} 