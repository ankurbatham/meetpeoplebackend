package com.meetThePeople.dto;

import com.meetThePeople.entity.Message;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessageDto {
    
    @NotNull(message = "Receiver ID is required")
    private Long receiverId;
    
    @NotNull(message = "Message type is required")
    private Message.MessageType messageType;
    
    private String textContent;
    private String mediaPath;
} 