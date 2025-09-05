package com.meetThePeople.service;

import com.meetThePeople.repository.MessageRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MessageRetentionScheduler {
    
    private final MessageRetentionService retentionService;
    private final MessageRepository messageRepository;
    
    public MessageRetentionScheduler(MessageRetentionService retentionService, 
                                   MessageRepository messageRepository) {
        this.retentionService = retentionService;
        this.messageRepository = messageRepository;
    }
    
    /**
     * Scheduled task to clean up old messages every hour
     * This ensures that retention policies are enforced even for inactive conversations
     */
    @Scheduled(fixedRate = 3600000) // 1 hour = 3,600,000 milliseconds
    public void scheduledMessageCleanup() {
        if (!retentionService.isRetentionEnabled()) {
            return; // Retention is disabled
        }
        
        try {
            // Get all unique user pairs that have messages
            List<Object[]> userPairs = messageRepository.findDistinctUserPairs();
            
            for (Object[] pair : userPairs) {
                Long userId1 = (Long) pair[0];
                Long userId2 = (Long) pair[1];
                
                try {
                    // Enforce retention policy for this user pair
                    retentionService.enforceRetentionPolicy(userId1, userId2);
                } catch (Exception e) {
                    // Log error but continue with other pairs
                    // In production, use proper logging
                    System.err.println("Error cleaning up messages for users " + userId1 + " and " + userId2 + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            // Log error but don't fail the scheduled task
            System.err.println("Error in scheduled message cleanup: " + e.getMessage());
        }
    }
    
    /**
     * Scheduled task to clean up old messages daily at 2 AM
     * This is a more thorough cleanup that can handle larger datasets
     */
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2:00 AM
    public void dailyMessageCleanup() {
        if (!retentionService.isRetentionEnabled()) {
            return; // Retention is disabled
        }
        
        try {
            // Trigger comprehensive cleanup
            retentionService.cleanupAllConversations();
        } catch (Exception e) {
            // Log error but don't fail the scheduled task
            System.err.println("Error in daily message cleanup: " + e.getMessage());
        }
    }
    
    /**
     * Scheduled task to clean up old messages weekly on Sunday at 3 AM
     * This can handle very large datasets and perform deep cleanup
     */
    @Scheduled(cron = "0 0 3 ? * SUN") // Weekly on Sunday at 3:00 AM
    public void weeklyMessageCleanup() {
        if (!retentionService.isRetentionEnabled()) {
            return; // Retention is disabled
        }
        
        try {
            // Perform deep cleanup and optimization
            performDeepCleanup();
        } catch (Exception e) {
            // Log error but don't fail the scheduled task
            System.err.println("Error in weekly message cleanup: " + e.getMessage());
        }
    }
    
    /**
     * Performs a deep cleanup of the message system
     * This can include database optimization and cleanup
     */
    private void performDeepCleanup() {
        try {
            // Get retention statistics for monitoring
            // In production, you might want to log these statistics
            
            // Trigger the main cleanup
            retentionService.cleanupAllConversations();
            
            // Additional cleanup tasks can be added here:
            // 1. Clean up orphaned media files
            // 2. Optimize database tables
            // 3. Archive old messages (if needed)
            // 4. Update statistics and metrics
            
        } catch (Exception e) {
            System.err.println("Error in deep cleanup: " + e.getMessage());
        }
    }
} 