package com.meetThePeople.service;

import com.meetThePeople.dto.SearchUserDto;
import com.meetThePeople.dto.UserProfileDto;
import com.meetThePeople.dto.UserSearchResultDto;
import com.meetThePeople.dto.UserProfileResponseDto;
import com.meetThePeople.dto.UserCommunicationDto;
import com.meetThePeople.entity.User;
import com.meetThePeople.entity.UserActivity;
import com.meetThePeople.entity.Message;
import com.meetThePeople.repository.UserRepository;
import com.meetThePeople.repository.UserActivityRepository;
import com.meetThePeople.repository.MessageRepository;
import com.meetThePeople.service.CommunicationService;
import com.meetThePeople.util.DistanceUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final UserActivityRepository userActivityRepository;
    private final MessageRepository messageRepository;
    private final CommunicationService communicationService;
    private final DistanceUtil distanceUtil;
    
    public UserService(UserRepository userRepository, UserActivityRepository userActivityRepository, MessageRepository messageRepository, CommunicationService communicationService, DistanceUtil distanceUtil) {
        this.userRepository = userRepository;
        this.userActivityRepository = userActivityRepository;
        this.messageRepository = messageRepository;
        this.communicationService = communicationService;
        this.distanceUtil = distanceUtil;
    }
    
    public User createUser(String mobile) {
        User user = new User();
        user.setMobile(mobile);
        user.setName("User"); // Default name, will be updated later
        return userRepository.save(user);
    }
    
    public Optional<User> findByMobile(String mobile) {
        return userRepository.findByMobile(mobile);
    }
    
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    public User updateProfile(Long userId, UserProfileDto profileDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setName(profileDto.getName());
        user.setGender(profileDto.getGender());
        user.setDob(profileDto.getDob());
        user.setAddress(profileDto.getAddress());
        user.setPincode(profileDto.getPincode());
        user.setLatitude(profileDto.getLatitude());
        user.setLongitude(profileDto.getLongitude());
        user.setHobbies(profileDto.getHobbies());
        user.setAboutYou(profileDto.getAboutYou());
        
        return userRepository.save(user);
    }
    
    public List<User> searchUsers(Long userId, SearchUserDto searchDto) {
        List<User> users;
        
        if (searchDto.getGender() != null && searchDto.getPincode() != null) {
            users = userRepository.findUsersByGenderAndPincode(searchDto.getGender(), 
                                                             searchDto.getPincode(), 
                                                             userId);
        } else if (searchDto.getGender() != null) {
            users = userRepository.findUsersByGender(searchDto.getGender(), userId);
        } else {
            users = userRepository.findAllUsersExceptBlocked(userId);
        }
        
        // Apply age group filter if specified
        if (searchDto.getAgeGroup() != null) {
            users = users.stream()
                    .filter(user -> isInAgeGroup(user.getDob(), searchDto.getAgeGroup()))
                    .collect(Collectors.toList());
        }
        
        // Apply distance filter if specified
        if (searchDto.getMaxDistanceKm() != null && searchDto.getUserLatitude() != null 
            && searchDto.getUserLongitude() != null) {
            users = users.stream()
                    .filter(user -> user.getLatitude() != null && user.getLongitude() != null)
                    .filter(user -> distanceUtil.isWithinDistance(
                            searchDto.getUserLatitude(), searchDto.getUserLongitude(),
                            user.getLatitude(), user.getLongitude(),
                            searchDto.getMaxDistanceKm()))
                    .sorted((u1, u2) -> {
                        double dist1 = distanceUtil.calculateDistance(
                                searchDto.getUserLatitude(), searchDto.getUserLongitude(),
                                u1.getLatitude(), u1.getLongitude());
                        double dist2 = distanceUtil.calculateDistance(
                                searchDto.getUserLatitude(), searchDto.getUserLongitude(),
                                u2.getLatitude(), u2.getLongitude());
                        return Double.compare(dist1, dist2);
                    })
                    .collect(Collectors.toList());
        }
        
        return users;
    }
    
    public List<UserSearchResultDto> searchUsersWithActivity(Long userId, SearchUserDto searchDto) {
        List<User> users = searchUsers(userId, searchDto);
        
        return users.stream()
                .map(user -> {
                    UserActivity activity = userActivityRepository.findByUserId(user.getId()).orElse(null);
                    return UserSearchResultDto.fromUserAndActivity(user, activity);
                })
                .collect(Collectors.toList());
    }
    
    private boolean isInAgeGroup(LocalDate dob, String ageGroup) {
        int age = Period.between(dob, LocalDate.now()).getYears();
        
        return switch (ageGroup) {
            case "18-25" -> age >= 18 && age <= 25;
            case "26-35" -> age >= 26 && age <= 35;
            case "36-45" -> age >= 36 && age <= 45;
            case "46+" -> age >= 46;
            default -> true;
        };
    }
    
    public UserActivity getUserActivity(Long userId) {
        return userActivityRepository.findByUserId(userId).orElse(null);
    }
    
    public UserProfileResponseDto getUserProfileWithActivity(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserActivity activity = userActivityRepository.findByUserId(userId).orElse(null);
        
        return UserProfileResponseDto.fromUserAndActivity(user, activity);
    }
    
    public UserCommunicationDto getUserCommunicationDetails(Long currentUserId, Long otherUserId) {
        // Get the other user
        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get user activity for online status
        UserActivity activity = userActivityRepository.findByUserId(otherUserId).orElse(null);
        
        // Get communication mapping details
        var communicationMapping = communicationService.getCommunicationMapping(currentUserId, otherUserId);
        
        Long communicationId = null;
        Boolean canCommunicate = false;
        LocalDateTime communicationEstablishedAt = null;
        LocalDateTime communicationUpdatedAt = null;
        
        if (communicationMapping.isPresent()) {
            var mapping = communicationMapping.get();
            communicationId = mapping.getId();
            canCommunicate = mapping.getCanCommunicate();
            communicationEstablishedAt = mapping.getEstablishedAt();
            communicationUpdatedAt = mapping.getUpdatedAt();
        }
        
        // Get last message between users
        Message lastMessage = messageRepository.findLastMessageBetweenUsers(currentUserId, otherUserId);
        
        return UserCommunicationDto.fromUserActivityAndMessage(
                otherUser, 
                activity, 
                communicationId,
                canCommunicate,
                communicationEstablishedAt,
                communicationUpdatedAt,
                lastMessage,
                currentUserId
        );
    }
} 