package com.meetThePeople.dto;

import com.meetThePeople.entity.User;
import lombok.Data;

@Data
public class SearchUserDto {
    
    private User.Gender gender;
    private String pincode;
    private String ageGroup; // e.g., "18-25", "26-35", "36-45", "46+"
    private Double maxDistanceKm;
    private Double userLatitude;
    private Double userLongitude;
} 