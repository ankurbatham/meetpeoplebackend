package com.meetThePeople.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfilePicDto {
    
    private Long id;
    private String imagePath;
    private String imageName;
    private String imageType;
    private Long fileSize;
    private Boolean isPrimary;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Manual getter for id field to resolve compilation issues
    public Long getId() {
        return this.id;
    }
    
    // Manual setter for id field
    public void setId(Long id) {
        this.id = id;
    }
} 