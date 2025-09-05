package com.meetThePeople.dto;

import com.meetThePeople.entity.Message;
import com.meetThePeople.entity.User;
import com.meetThePeople.entity.UserActivity;
import com.meetThePeople.util.OnlineStatusUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCommunicationDto {
    
    private Long userId;
    private String userName;
    private String userMobile;
    private User.Gender userGender;
    private String userAddress;
    private String userPincode;
    private Double userLatitude;
    private Double userLongitude;
    private String userHobbies;
    private String userAboutYou;
    private List<ProfilePicDto> userProfilePics;
    private LocalDateTime userCreatedAt;
    private LocalDateTime userUpdatedAt;
    
    // Online status information
    private String onlineStatus; // "online" or "offline"
    private LocalDateTime lastActiveTime;
    private UserActivity.ActiveSource lastActiveSource;
    
    // Communication details
    private Long communicationId;
    private Boolean canCommunicate;
    private LocalDateTime communicationEstablishedAt;
    private LocalDateTime communicationUpdatedAt;
    
    // Last message information
    private Long lastMessageId;
    private String lastMessageType;
    private String lastMessageContent;
    private String lastMessageMediaPath;
    private LocalDateTime lastMessageTime;
    private Boolean isLastMessageFromMe;
    
    // Manual getter for userId field to resolve compilation issues
    public Long getUserId() {
        return this.userId;
    }
    
    // Manual setter for userId field
    public void setId(Long userId) {
        this.userId = userId;
    }
    
    // Static factory method to create from User, UserActivity, and Message
    public static UserCommunicationDto fromUserActivityAndMessage(
            User user, 
            UserActivity activity, 
            Long communicationId,
            Boolean canCommunicate,
            LocalDateTime communicationEstablishedAt,
            LocalDateTime communicationUpdatedAt,
            Message lastMessage,
            Long currentUserId) {
        
        UserCommunicationDto dto = new UserCommunicationDto();
        
        // Set user information
        dto.setUserId(user.getId());
        dto.setUserName(user.getName());
        dto.setUserMobile(user.getMobile());
        dto.setUserGender(user.getGender());
        dto.setUserAddress(user.getAddress());
        dto.setUserPincode(user.getPincode());
        dto.setUserLatitude(user.getLatitude());
        dto.setUserLongitude(user.getLongitude());
        dto.setUserHobbies(user.getHobbies());
        dto.setUserAboutYou(user.getAboutYou());
        dto.setUserProfilePics(user.getProfilePics().stream()
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
        dto.setUserCreatedAt(user.getCreatedAt());
        dto.setUserUpdatedAt(user.getUpdatedAt());
        
        // Set online status
        if (activity != null) {
            dto.setLastActiveTime(activity.getLastActiveTime());
            dto.setLastActiveSource(activity.getActiveSource());
            dto.setOnlineStatus(OnlineStatusUtil.getOnlineStatus(activity.getLastActiveTime()));
        } else {
            dto.setOnlineStatus("offline");
            dto.setLastActiveTime(null);
            dto.setLastActiveSource(null);
        }
        
        // Set communication details
        dto.setCommunicationId(communicationId);
        dto.setCanCommunicate(canCommunicate);
        dto.setCommunicationEstablishedAt(communicationEstablishedAt);
        dto.setCommunicationUpdatedAt(communicationUpdatedAt);
        
        // Set last message information
        if (lastMessage != null) {
            dto.setLastMessageId(lastMessage.getId());
            dto.setLastMessageType(lastMessage.getMessageType().toString());
            dto.setLastMessageContent(lastMessage.getTextContent());
            dto.setLastMessageMediaPath(lastMessage.getMediaPath());
            dto.setLastMessageTime(lastMessage.getCreatedAt());
            dto.setIsLastMessageFromMe(lastMessage.getSender().getId().equals(currentUserId));
        } else {
            dto.setLastMessageId(null);
            dto.setLastMessageType(null);
            dto.setLastMessageContent(null);
            dto.setLastMessageMediaPath(null);
            dto.setLastMessageTime(null);
            dto.setIsLastMessageFromMe(null);
        }
        
        return dto;
    }
} 