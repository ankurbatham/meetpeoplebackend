package com.meetThePeople.service;

import com.meetThePeople.entity.User;
import com.meetThePeople.entity.UserActivity;
import com.meetThePeople.repository.UserActivityRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserActivityService {
    
    private final UserActivityRepository userActivityRepository;
    
    public UserActivityService(UserActivityRepository userActivityRepository) {
        this.userActivityRepository = userActivityRepository;
    }
    
    public void captureActivity(Long userId, UserActivity.ActiveSource activeSource) {
        Optional<UserActivity> existingActivity = userActivityRepository.findByUserId(userId);
        
        UserActivity userActivity;
        if (existingActivity.isPresent()) {
            userActivity = existingActivity.get();
            userActivity.setActiveSource(activeSource);
            userActivity.setLastActiveTime(LocalDateTime.now());
        } else {
            userActivity = new UserActivity();
            userActivity.setUser(new User());
            userActivity.getUser().setId(userId);
            userActivity.setActiveSource(activeSource);
            userActivity.setLastActiveTime(LocalDateTime.now());
        }
        
        userActivityRepository.save(userActivity);
    }
    
    public Optional<UserActivity> getUserActivity(Long userId) {
        return userActivityRepository.findByUserId(userId);
    }
    
    public void deleteUserActivity(Long userId) {
        userActivityRepository.deleteByUserId(userId);
    }
    
    public boolean isUserOnline(Long userId) {
        Optional<UserActivity> activity = userActivityRepository.findByUserId(userId);
        if (activity.isPresent()) {
            LocalDateTime lastActive = activity.get().getLastActiveTime();
            LocalDateTime now = LocalDateTime.now();
            return lastActive != null && lastActive.plusSeconds(60).isAfter(now);
        }
        return false;
    }
    
    public String getUserOnlineStatus(Long userId) {
        return isUserOnline(userId) ? "online" : "offline";
    }
    
    public UserActivity.ActiveSource getUserLastActiveSource(Long userId) {
        Optional<UserActivity> activity = userActivityRepository.findByUserId(userId);
        return activity.map(UserActivity::getActiveSource).orElse(null);
    }
    
    public LocalDateTime getUserLastActiveTime(Long userId) {
        Optional<UserActivity> activity = userActivityRepository.findByUserId(userId);
        return activity.map(UserActivity::getLastActiveTime).orElse(null);
    }
    
    /**
     * Capture activity for anonymous users (without user ID)
     * This is useful for tracking app usage before login
     */
    public void captureAnonymousActivity(UserActivity.ActiveSource activeSource) {
        // For anonymous users, we can either:
        // 1. Store in a separate anonymous activity table
        // 2. Store with a special user ID (e.g., 0 or -1)
        // 3. Log the activity for analytics
        
        // For now, we'll log the anonymous activity
        // In a production system, you might want to store this in a separate table
        System.out.println("Anonymous activity captured - Source: " + activeSource + ", Time: " + LocalDateTime.now());
        
        // TODO: Implement proper anonymous activity storage if needed
        // This could be useful for analytics, user behavior tracking, etc.
    }
} 