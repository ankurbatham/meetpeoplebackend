package com.meetThePeople.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.message.retention")
public class MessageRetentionConfig {
    
    private int count = 3; // Default to 3 messages
    private boolean enabled = true; // Default to enabled
    
    public int getCount() {
        return count;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    // Manual getter for count field to resolve compilation issues
    public int getRetentionCount() {
        return this.count;
    }
    
    // Manual setter for count field
    public void setRetentionCount(int count) {
        this.count = count;
    }
} 