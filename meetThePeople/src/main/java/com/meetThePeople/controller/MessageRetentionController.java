package com.meetThePeople.controller;

import com.meetThePeople.dto.ApiResponseDto;
import com.meetThePeople.service.MessageRetentionService;
import com.meetThePeople.service.UserService;
import com.meetThePeople.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/message-retention")
@CrossOrigin(origins = "*")
public class MessageRetentionController {
    
    private final MessageRetentionService retentionService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    
    public MessageRetentionController(MessageRetentionService retentionService, 
                                   UserService userService,
                                   JwtUtil jwtUtil) {
        this.retentionService = retentionService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }
    
    @GetMapping("/config")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> getRetentionConfig(@RequestHeader("Authorization") String token) {
        try {
            String mobile = jwtUtil.getMobileFromToken(token.replace("Bearer ", ""));
            userService.findByMobile(mobile)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Map<String, Object> config = new HashMap<>();
            config.put("enabled", retentionService.isRetentionEnabled());
            config.put("retentionCount", retentionService.getRetentionCount());
            
            return ResponseEntity.ok(ApiResponseDto.success("Retention configuration retrieved successfully", config));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get retention config: " + e.getMessage()));
        }
    }
    
    @PutMapping("/config")
    public ResponseEntity<ApiResponseDto<String>> updateRetentionConfig(@RequestHeader("Authorization") String token,
                                                                      @RequestParam int retentionCount,
                                                                      @RequestParam boolean enabled) {
        try {
            String mobile = jwtUtil.getMobileFromToken(token.replace("Bearer ", ""));
            userService.findByMobile(mobile)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Validate retention count
            if (retentionCount < 1 || retentionCount > 100) {
                return ResponseEntity.badRequest().body(ApiResponseDto.error("Retention count must be between 1 and 100"));
            }
            
            retentionService.updateRetentionConfig(retentionCount, enabled);
            
            String message = String.format("Retention configuration updated: enabled=%s, count=%d", enabled, retentionCount);
            return ResponseEntity.ok(ApiResponseDto.success(message, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to update retention config: " + e.getMessage()));
        }
    }
    
    @GetMapping("/stats/{otherUserId}")
    public ResponseEntity<ApiResponseDto<MessageRetentionService.MessageRetentionStats>> getRetentionStats(
            @RequestHeader("Authorization") String token,
            @PathVariable Long otherUserId) {
        try {
            String mobile = jwtUtil.getMobileFromToken(token.replace("Bearer ", ""));
            Long currentUserId = userService.findByMobile(mobile)
                    .orElseThrow(() -> new RuntimeException("User not found"))
                    .getId();
            
            MessageRetentionService.MessageRetentionStats stats = 
                retentionService.getRetentionStats(currentUserId, otherUserId);
            
            return ResponseEntity.ok(ApiResponseDto.success("Retention statistics retrieved successfully", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get retention stats: " + e.getMessage()));
        }
    }
    
    @PostMapping("/cleanup/{otherUserId}")
    public ResponseEntity<ApiResponseDto<String>> cleanupConversation(@RequestHeader("Authorization") String token,
                                                                    @PathVariable Long otherUserId) {
        try {
            String mobile = jwtUtil.getMobileFromToken(token.replace("Bearer ", ""));
            Long currentUserId = userService.findByMobile(mobile)
                    .orElseThrow(() -> new RuntimeException("User not found"))
                    .getId();
            
            // Enforce retention policy for this conversation
            retentionService.enforceRetentionPolicy(currentUserId, otherUserId);
            
            return ResponseEntity.ok(ApiResponseDto.success("Conversation cleanup completed successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to cleanup conversation: " + e.getMessage()));
        }
    }
    
    @PostMapping("/cleanup-all")
    public ResponseEntity<ApiResponseDto<String>> cleanupAllConversations(@RequestHeader("Authorization") String token) {
        try {
            String mobile = jwtUtil.getMobileFromToken(token.replace("Bearer ", ""));
            userService.findByMobile(mobile)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Trigger cleanup for all conversations
            retentionService.cleanupAllConversations();
            
            return ResponseEntity.ok(ApiResponseDto.success("All conversations cleanup initiated successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to cleanup all conversations: " + e.getMessage()));
        }
    }
} 