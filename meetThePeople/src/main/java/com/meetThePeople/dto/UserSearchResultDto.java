package com.meetThePeople.dto;

import com.meetThePeople.entity.User;
import com.meetThePeople.entity.UserActivity;
import com.meetThePeople.util.OnlineStatusUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchResultDto {
    
    private Long id;
    private String name;
    private User.Gender gender;
    private LocalDate dob;
    private String address;
    private String pincode;
    private Double latitude;
    private Double longitude;
    private String hobbies;
    private String aboutYou;
    private List<ProfilePicDto> profilePics;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Online status information
    private String onlineStatus; // "online" or "offline"
    private LocalDateTime lastActiveTime;
    private UserActivity.ActiveSource lastActiveSource;
    
    // Manual getter for id field to resolve compilation issues
    public Long getId() {
        return this.id;
    }
    
    // Manual setter for id field
    public void setId(Long id) {
        this.id = id;
    }
    
    // Static factory method to create from User and UserActivity
    public static UserSearchResultDto fromUserAndActivity(User user, UserActivity activity) {
        UserSearchResultDto dto = new UserSearchResultDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setGender(user.getGender());
        dto.setDob(user.getDob());
        dto.setAddress(user.getAddress());
        dto.setPincode(user.getPincode());
        dto.setLatitude(user.getLatitude());
        dto.setLongitude(user.getLongitude());
        dto.setHobbies(user.getHobbies());
        dto.setAboutYou(user.getAboutYou());
        dto.setProfilePics(user.getProfilePics().stream()
                .map(pic -> {
                    ProfilePicDto picDto = new ProfilePicDto();
                    picDto.setId(pic.getId());
                    picDto.setImagePath(pic.getImagePath());
                    picDto.setImageName(pic.getImageName());
                    picDto.setImageType(pic.getImageType());
                    picDto.setFileSize(pic.getFileSize());
                    picDto.setIsPrimary(pic.getIsPrimary());
                    picDto.setDisplayOrder(pic.getDisplayOrder());
                    picDto.setCreatedAt(pic.getCreatedAt());
                    picDto.setUpdatedAt(pic.getUpdatedAt());
                    return picDto;
                })
                .toList());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        
        // Set online status
        if (activity != null) {
            dto.setLastActiveTime(activity.getLastActiveTime());
            dto.setLastActiveSource(activity.getActiveSource());
            
            // Use utility class to determine online/offline status based on 60-second threshold
            dto.setOnlineStatus(OnlineStatusUtil.getOnlineStatus(activity.getLastActiveTime()));
        } else {
            dto.setOnlineStatus("offline");
            dto.setLastActiveTime(null);
            dto.setLastActiveSource(null);
        }
        
        return dto;
    }
} 