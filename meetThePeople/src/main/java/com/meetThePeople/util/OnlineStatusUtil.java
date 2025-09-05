package com.meetThePeople.util;

import java.time.LocalDateTime;

public class OnlineStatusUtil {
    
    private static final int ONLINE_THRESHOLD_SECONDS = 60;
    
    /**
     * Determines if a user is online based on their last activity time
     * @param lastActiveTime The last time the user was active
     * @return true if user is online (active within last 60 seconds), false otherwise
     */
    public static boolean isOnline(LocalDateTime lastActiveTime) {
        if (lastActiveTime == null) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = lastActiveTime.plusSeconds(ONLINE_THRESHOLD_SECONDS);
        
        return now.isBefore(threshold);
    }
    
    /**
     * Gets the online status as a string
     * @param lastActiveTime The last time the user was active
     * @return "online" or "offline"
     */
    public static String getOnlineStatus(LocalDateTime lastActiveTime) {
        return isOnline(lastActiveTime) ? "online" : "offline";
    }
    
    /**
     * Gets the online status as a string with custom threshold
     * @param lastActiveTime The last time the user was active
     * @param thresholdSeconds Custom threshold in seconds
     * @return "online" or "offline"
     */
    public static String getOnlineStatus(LocalDateTime lastActiveTime, int thresholdSeconds) {
        if (lastActiveTime == null) {
            return "offline";
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = lastActiveTime.plusSeconds(thresholdSeconds);
        
        return now.isBefore(threshold) ? "online" : "offline";
    }
    
    /**
     * Calculates how many seconds ago the user was last active
     * @param lastActiveTime The last time the user was active
     * @return seconds since last activity, or -1 if lastActiveTime is null
     */
    public static long getSecondsSinceLastActivity(LocalDateTime lastActiveTime) {
        if (lastActiveTime == null) {
            return -1;
        }
        
        LocalDateTime now = LocalDateTime.now();
        return java.time.Duration.between(lastActiveTime, now).getSeconds();
    }
} 