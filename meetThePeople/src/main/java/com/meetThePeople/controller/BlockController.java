package com.meetThePeople.controller;

import com.meetThePeople.dto.ApiResponseDto;
import com.meetThePeople.entity.User;
import com.meetThePeople.entity.UserBlockMapping;
import com.meetThePeople.service.BlockService;
import com.meetThePeople.service.UserService;
import com.meetThePeople.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/block")
@CrossOrigin(origins = "*")
public class BlockController {
    
    private final BlockService blockService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    
    public BlockController(BlockService blockService, UserService userService, JwtUtil jwtUtil) {
        this.blockService = blockService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }
    
    @PostMapping("/{userId}")
    public ResponseEntity<ApiResponseDto<String>> blockUser(@RequestHeader("Authorization") String token,
                                                          @PathVariable Long userId) {
        try {
            String mobile = jwtUtil.getMobileFromToken(token.replace("Bearer ", ""));
            User currentUser = userService.findByMobile(mobile)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            blockService.blockUser(currentUser.getId(), userId);
            return ResponseEntity.ok(ApiResponseDto.success("User blocked successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to block user: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponseDto<String>> unblockUser(@RequestHeader("Authorization") String token,
                                                            @PathVariable Long userId) {
        try {
            String mobile = jwtUtil.getMobileFromToken(token.replace("Bearer ", ""));
            User currentUser = userService.findByMobile(mobile)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            blockService.unblockUser(currentUser.getId(), userId);
            return ResponseEntity.ok(ApiResponseDto.success("User unblocked successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to unblock user: " + e.getMessage()));
        }
    }
    
    @GetMapping("/list")
    public ResponseEntity<ApiResponseDto<List<UserBlockMapping>>> getBlockedUsers(@RequestHeader("Authorization") String token) {
        try {
            String mobile = jwtUtil.getMobileFromToken(token.replace("Bearer ", ""));
            User currentUser = userService.findByMobile(mobile)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            List<UserBlockMapping> blockedUsers = blockService.getBlockedUsers(currentUser.getId());
            return ResponseEntity.ok(ApiResponseDto.success("Blocked users retrieved successfully", blockedUsers));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get blocked users: " + e.getMessage()));
        }
    }
    
    @GetMapping("/check/{userId}")
    public ResponseEntity<ApiResponseDto<Boolean>> checkIfBlocked(@RequestHeader("Authorization") String token,
                                                                @PathVariable Long userId) {
        try {
            String mobile = jwtUtil.getMobileFromToken(token.replace("Bearer ", ""));
            User currentUser = userService.findByMobile(mobile)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            boolean isBlocked = blockService.isBlocked(currentUser.getId(), userId);
            return ResponseEntity.ok(ApiResponseDto.success("Block status checked successfully", isBlocked));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to check block status: " + e.getMessage()));
        }
    }
} 