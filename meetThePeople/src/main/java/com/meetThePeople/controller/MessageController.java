package com.meetThePeople.controller;

import com.meetThePeople.dto.ApiResponseDto;
import com.meetThePeople.dto.MessageDto;
import com.meetThePeople.entity.Message;
import com.meetThePeople.entity.User;
import com.meetThePeople.service.MessageService;
import com.meetThePeople.service.UserService;
import com.meetThePeople.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/messages")
@CrossOrigin(origins = "*")
public class MessageController {
    
    private final MessageService messageService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    
    public MessageController(MessageService messageService, UserService userService, JwtUtil jwtUtil) {
        this.messageService = messageService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }
    
    @PostMapping("/send")
    public ResponseEntity<ApiResponseDto<Message>> sendMessage(@RequestHeader("Authorization") String token,
                                                             @Valid @RequestBody MessageDto messageDto) {
        try {
            String mobile = jwtUtil.getMobileFromToken(token.replace("Bearer ", ""));
            User user = userService.findByMobile(mobile)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Message message = messageService.sendMessage(user.getId(), messageDto);
            return ResponseEntity.ok(ApiResponseDto.success("Message sent successfully", message));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to send message: " + e.getMessage()));
        }
    }
    
    @PostMapping("/send-media")
    public ResponseEntity<ApiResponseDto<Message>> sendMediaMessage(@RequestHeader("Authorization") String token,
                                                                  @RequestParam("receiverId") Long receiverId,
                                                                  @RequestParam("messageType") Message.MessageType messageType,
                                                                  @RequestParam(value = "textContent", required = false) String textContent,
                                                                  @RequestParam("mediaFile") MultipartFile mediaFile) {
        try {
            String mobile = jwtUtil.getMobileFromToken(token.replace("Bearer ", ""));
            User user = userService.findByMobile(mobile)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Message message = messageService.sendMessageWithMedia(user.getId(), receiverId, messageType, textContent, mediaFile);
            return ResponseEntity.ok(ApiResponseDto.success("Media message sent successfully", message));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to process media file: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to send media message: " + e.getMessage()));
        }
    }
    
    @GetMapping("/conversation/{otherUserId}")
    public ResponseEntity<ApiResponseDto<List<Message>>> getConversation(@RequestHeader("Authorization") String token,
                                                                       @PathVariable Long otherUserId) {
        try {
            String mobile = jwtUtil.getMobileFromToken(token.replace("Bearer ", ""));
            User user = userService.findByMobile(mobile)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            List<Message> messages = messageService.getMessagesBetweenUsers(user.getId(), otherUserId);
            return ResponseEntity.ok(ApiResponseDto.success("Conversation retrieved successfully", messages));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get conversation: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{messageId}")
    public ResponseEntity<ApiResponseDto<String>> deleteMessage(@RequestHeader("Authorization") String token,
                                                              @PathVariable Long messageId) {
        try {
            String mobile = jwtUtil.getMobileFromToken(token.replace("Bearer ", ""));
            User user = userService.findByMobile(mobile)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            messageService.deleteMessage(messageId, user.getId());
            return ResponseEntity.ok(ApiResponseDto.success("Message deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to delete message: " + e.getMessage()));
        }
    }
} 