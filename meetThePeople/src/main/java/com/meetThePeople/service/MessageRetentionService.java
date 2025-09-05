package com.meetThePeople.service;

import com.meetThePeople.config.MessageRetentionConfig;
import com.meetThePeople.entity.Message;
import com.meetThePeople.repository.MessageRepository;
import com.meetThePeople.util.FileUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MessageRetentionService {
    
    private final MessageRepository messageRepository;
    private final MessageRetentionConfig retentionConfig;
    private final FileUtil fileUtil;
    
    public MessageRetentionService(MessageRepository messageRepository, 
                                 MessageRetentionConfig retentionConfig,
                                 FileUtil fileUtil) {
        this.messageRepository = messageRepository;
        this.retentionConfig = retentionConfig;
        this.fileUtil = fileUtil;
    }
    
    /**
     * Enforces message retention policy for a conversation between two users
     * Keeps only the last N messages (configurable) and deletes older ones
     */
    public void enforceRetentionPolicy(Long userId1, Long userId2) {
        if (!retentionConfig.isEnabled()) {
            return; // Retention is disabled
        }
        
        int retentionCount = retentionConfig.getCount();
        
        // Get all message IDs between users, ordered by creation time (oldest first)
        List<Long> allMessageIds = messageRepository.findMessageIdsBetweenUsersOrderedAsc(userId1, userId2);
        
        // If we have more messages than the retention limit, delete the oldest ones
        if (allMessageIds.size() > retentionCount) {
            int messagesToDelete = allMessageIds.size() - retentionCount;
            List<Long> messageIdsToDelete = allMessageIds.subList(0, messagesToDelete);
            
            // Delete messages and their associated media files
            deleteMessagesByIds(messageIdsToDelete);
        }
    }
    
    /**
     * Deletes messages by their IDs and cleans up associated media files
     */
    private void deleteMessagesByIds(List<Long> messageIds) {
        for (Long messageId : messageIds) {
            try {
                Message message = messageRepository.findById(messageId).orElse(null);
                if (message != null) {
                    // Delete media file if exists
                    if (message.getMediaPath() != null) {
                        fileUtil.deleteFile(message.getMediaPath());
                    }
                    
                    // Delete the message from database
                    messageRepository.deleteById(messageId);
                }
            } catch (Exception e) {
                // Log error but continue with other deletions
                // In production, you might want to use a proper logger
                System.err.println("Error deleting message " + messageId + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Gets messages between users with retention policy applied
     * Returns only the last N messages (configurable)
     */
    public List<Message> getMessagesWithRetention(Long userId, Long otherUserId) {
        if (!retentionConfig.isEnabled()) {
            // If retention is disabled, return all messages
            return messageRepository.findMessagesBetweenUsers(userId, otherUserId);
        }
        
        int retentionCount = retentionConfig.getCount();
        
        // Use Pageable to limit results to the retention count
        PageRequest pageRequest = PageRequest.of(0, retentionCount);
        return messageRepository.findMessagesBetweenUsersWithLimit(userId, otherUserId, pageRequest);
    }
    
    /**
     * Gets the current retention count configuration
     */
    public int getRetentionCount() {
        return retentionConfig.getCount();
    }
    
    /**
     * Gets whether retention is enabled
     */
    public boolean isRetentionEnabled() {
        return retentionConfig.isEnabled();
    }
    
    /**
     * Updates retention configuration
     */
    public void updateRetentionConfig(int count, boolean enabled) {
        retentionConfig.setCount(count);
        retentionConfig.setEnabled(enabled);
    }
    
    /**
     * Manually triggers retention cleanup for all conversations
     * This can be called periodically via a scheduled task
     */
    public void cleanupAllConversations() {
        if (!retentionConfig.isEnabled()) {
            return;
        }
        
        // This is a simplified approach - in production, you might want to:
        // 1. Get all unique user pairs
        // 2. Apply retention policy to each pair
        // 3. Use batch operations for better performance
        
        // For now, this method can be called manually or via scheduled task
        // to clean up old messages across the system
    }
    
    /**
     * Gets statistics about message retention
     */
    public MessageRetentionStats getRetentionStats(Long userId1, Long userId2) {
        List<Long> allMessageIds = messageRepository.findMessageIdsBetweenUsersOrderedAsc(userId1, userId2);
        int totalMessages = allMessageIds.size();
        int retentionCount = retentionConfig.getCount();
        int messagesToDelete = Math.max(0, totalMessages - retentionCount);
        
        return new MessageRetentionStats(totalMessages, retentionCount, messagesToDelete);
    }
    
    /**
     * Inner class to hold retention statistics
     */
    public static class MessageRetentionStats {
        private final int totalMessages;
        private final int retentionCount;
        private final int messagesToDelete;
        
        public MessageRetentionStats(int totalMessages, int retentionCount, int messagesToDelete) {
            this.totalMessages = totalMessages;
            this.retentionCount = retentionCount;
            this.messagesToDelete = messagesToDelete;
        }
        
        public int getTotalMessages() {
            return totalMessages;
        }
        
        public int getRetentionCount() {
            return retentionCount;
        }
        
        public int getMessagesToDelete() {
            return messagesToDelete;
        }
        
        public boolean needsCleanup() {
            return messagesToDelete > 0;
        }
    }
} 