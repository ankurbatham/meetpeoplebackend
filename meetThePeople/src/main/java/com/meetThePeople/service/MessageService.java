package com.meetThePeople.service;

import com.meetThePeople.dto.MessageDto;
import com.meetThePeople.entity.Message;
import com.meetThePeople.entity.User;
import com.meetThePeople.repository.MessageRepository;
import com.meetThePeople.service.MessageRetentionService;
import com.meetThePeople.util.FileUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class MessageService {
    
    private final MessageRepository messageRepository;
    private final CommunicationService communicationService;
    private final FileUtil fileUtil;
    private final MessageRetentionService retentionService;
    
    public MessageService(MessageRepository messageRepository, 
                         CommunicationService communicationService,
                         FileUtil fileUtil,
                         MessageRetentionService retentionService) {
        this.messageRepository = messageRepository;
        this.communicationService = communicationService;
        this.fileUtil = fileUtil;
        this.retentionService = retentionService;
    }
    
    public Message sendMessage(Long senderId, MessageDto messageDto) {
        // Check if users can communicate
        if (!communicationService.canCommunicate(senderId, messageDto.getReceiverId())) {
            // Check if this is the first message
            long messageCount = messageRepository.countMessagesFromSenderToReceiver(senderId, messageDto.getReceiverId());
            if (messageCount > 0) {
                throw new RuntimeException("Cannot send message. Communication not established.");
            }
        }
        
        Message message = new Message();
        message.setSender(new User());
        message.getSender().setId(senderId);
        message.setReceiver(new User());
        message.getReceiver().setId(messageDto.getReceiverId());
        message.setMessageType(messageDto.getMessageType());
        message.setTextContent(messageDto.getTextContent());
        message.setMediaPath(messageDto.getMediaPath());
        
        Message savedMessage = messageRepository.save(message);
        
        // Establish communication if this is the first message
        if (!communicationService.canCommunicate(senderId, messageDto.getReceiverId())) {
            communicationService.establishCommunication(senderId, messageDto.getReceiverId());
        }
        
        // Enforce message retention policy after sending
        retentionService.enforceRetentionPolicy(senderId, messageDto.getReceiverId());
        
        return savedMessage;
    }
    
    public Message sendMessageWithMedia(Long senderId, Long receiverId, Message.MessageType messageType, 
                                      String textContent, MultipartFile mediaFile) throws IOException {
        String mediaPath = null;
        
        if (mediaFile != null && !mediaFile.isEmpty()) {
            switch (messageType) {
                case IMAGE -> mediaPath = fileUtil.saveImage(mediaFile);
                case VOICE -> mediaPath = fileUtil.saveVoiceMessage(mediaFile);
                default -> throw new RuntimeException("Invalid message type for media upload");
            }
        }
        
        MessageDto messageDto = new MessageDto();
        messageDto.setReceiverId(receiverId);
        messageDto.setMessageType(messageType);
        messageDto.setTextContent(textContent);
        messageDto.setMediaPath(mediaPath);
        
        return sendMessage(senderId, messageDto);
    }
    
    public List<Message> getMessagesBetweenUsers(Long userId, Long otherUserId) {
        // Use retention service to get messages with retention policy applied
        return retentionService.getMessagesWithRetention(userId, otherUserId);
    }
    
    public void deleteMessage(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        
        // Only sender can delete the message
        if (!message.getSender().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this message");
        }
        
        // Delete media file if exists
        if (message.getMediaPath() != null) {
            fileUtil.deleteFile(message.getMediaPath());
        }
        
        messageRepository.delete(message);
    }
} 