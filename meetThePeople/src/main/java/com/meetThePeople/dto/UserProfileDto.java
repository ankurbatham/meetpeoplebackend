package com.meetThePeople.dto;

import com.meetThePeople.entity.User;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UserProfileDto {
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;
    
    @NotNull(message = "Gender is required")
    private User.Gender gender;
    
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dob;
    
    @Size(max = 200, message = "Address must not exceed 200 characters")
    private String address;
    
    @Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be 6 digits")
    private String pincode;
    
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double latitude;
    
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double longitude;
    
    @Size(max = 500, message = "Hobbies must not exceed 500 characters")
    private String hobbies;
    
    @Size(max = 1000, message = "About you must not exceed 1000 characters")
    private String aboutYou;
    
    private List<ProfilePicDto> profilePics;
} 