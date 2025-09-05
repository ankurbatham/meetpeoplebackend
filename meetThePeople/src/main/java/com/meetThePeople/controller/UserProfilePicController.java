package com.meetThePeople.controller;

import com.meetThePeople.dto.ApiResponseDto;
import com.meetThePeople.entity.UserProfilePic;
import com.meetThePeople.service.UserProfilePicService;
import com.meetThePeople.service.UserService;
import com.meetThePeople.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/profile-pics")
@CrossOrigin(origins = "*")
public class UserProfilePicController {
    
    private final UserProfilePicService profilePicService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    
    public UserProfilePicController(UserProfilePicService profilePicService, UserService userService, JwtUtil jwtUtil) {
        this.profilePicService = profilePicService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponseDto<List<UserProfilePic>>> getUserProfilePics(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String token) {
        try {
            String mobile = jwtUtil.getMobileFromToken(token.replace("Bearer ", ""));
            userService.findByMobile(mobile)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            List<UserProfilePic> profilePics = profilePicService.getUserProfilePics(userId);
            return ResponseEntity.ok(ApiResponseDto.success("Profile pictures retrieved successfully", profilePics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get profile pictures: " + e.getMessage()));
        }
    }
    
    @PostMapping("/upload")
    public ResponseEntity<ApiResponseDto<UserProfilePic>> uploadProfilePic(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "isPrimary", defaultValue = "false") boolean isPrimary,
            @RequestHeader("Authorization") String token) {
        try {
            String mobile = jwtUtil.getMobileFromToken(token.replace("Bearer ", ""));
            Long userId = userService.findByMobile(mobile)
                    .orElseThrow(() -> new RuntimeException("User not found"))
                    .getId();
            
            UserProfilePic profilePic = profilePicService.uploadProfilePic(userId, file, isPrimary);
            return ResponseEntity.ok(ApiResponseDto.success("Profile picture uploaded successfully", profilePic));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to upload profile picture: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{picId}")
    public ResponseEntity<ApiResponseDto<String>> deleteProfilePic(
            @PathVariable Long picId,
            @RequestHeader("Authorization") String token) {
        try {
            String mobile = jwtUtil.getMobileFromToken(token.replace("Bearer ", ""));
            Long userId = userService.findByMobile(mobile)
                    .orElseThrow(() -> new RuntimeException("User not found"))
                    .getId();
            
            profilePicService.deleteProfilePic(userId, picId);
            return ResponseEntity.ok(ApiResponseDto.success("Profile picture deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to delete profile picture: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{picId}/primary")
    public ResponseEntity<ApiResponseDto<UserProfilePic>> setPrimaryProfilePic(
            @PathVariable Long picId,
            @RequestHeader("Authorization") String token) {
        try {
            String mobile = jwtUtil.getMobileFromToken(token.replace("Bearer ", ""));
            Long userId = userService.findByMobile(mobile)
                    .orElseThrow(() -> new RuntimeException("User not found"))
                    .getId();
            
            UserProfilePic profilePic = profilePicService.setPrimaryProfilePic(userId, picId);
            return ResponseEntity.ok(ApiResponseDto.success("Primary profile picture set successfully", profilePic));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to set primary profile picture: " + e.getMessage()));
        }
    }
    
    @PutMapping("/reorder")
    public ResponseEntity<ApiResponseDto<String>> updateDisplayOrder(
            @RequestBody List<Long> picIdsInOrder,
            @RequestHeader("Authorization") String token) {
        try {
            String mobile = jwtUtil.getMobileFromToken(token.replace("Bearer ", ""));
            Long userId = userService.findByMobile(mobile)
                    .orElseThrow(() -> new RuntimeException("User not found"))
                    .getId();
            
            profilePicService.updateDisplayOrder(userId, picIdsInOrder);
            return ResponseEntity.ok(ApiResponseDto.success("Display order updated successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to update display order: " + e.getMessage()));
        }
    }
} 