package com.meetThePeople.service;

import com.meetThePeople.entity.User;
import com.meetThePeople.entity.UserProfilePic;
import com.meetThePeople.repository.UserProfilePicRepository;
import com.meetThePeople.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserProfilePicService {
    
    private final UserProfilePicRepository profilePicRepository;
    private final UserRepository userRepository;
    private final String uploadDir = "uploads/profile-pics/";
    
    public UserProfilePicService(UserProfilePicRepository profilePicRepository, UserRepository userRepository) {
        this.profilePicRepository = profilePicRepository;
        this.userRepository = userRepository;
        // Create upload directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            // Log error but don't fail startup
        }
    }
    
    public List<UserProfilePic> getUserProfilePics(Long userId) {
        return profilePicRepository.findByUserIdOrderByDisplayOrderAsc(userId);
    }
    
    public Optional<UserProfilePic> getPrimaryProfilePic(Long userId) {
        return profilePicRepository.findByUserIdAndIsPrimaryTrue(userId);
    }
    
    public UserProfilePic uploadProfilePic(Long userId, MultipartFile file, boolean isPrimary) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID().toString() + fileExtension;
        
        // Save file to disk
        Path filePath = Paths.get(uploadDir + filename);
        Files.copy(file.getInputStream(), filePath);
        
        // Create profile pic entity
        UserProfilePic profilePic = new UserProfilePic();
        profilePic.setUser(user);
        profilePic.setImagePath(uploadDir + filename);
        profilePic.setImageName(originalFilename);
        profilePic.setImageType(file.getContentType());
        profilePic.setFileSize(file.getSize());
        profilePic.setIsPrimary(isPrimary);
        
        // Set display order
        List<UserProfilePic> existingPics = profilePicRepository.findByUserIdOrderByDisplayOrderAsc(userId);
        profilePic.setDisplayOrder(existingPics.size());
        
        // If this is primary, remove primary flag from others
        if (isPrimary) {
            profilePicRepository.findByUserIdAndIsPrimaryTrue(userId)
                    .ifPresent(existingPrimary -> {
                        existingPrimary.setIsPrimary(false);
                        profilePicRepository.save(existingPrimary);
                    });
        }
        
        return profilePicRepository.save(profilePic);
    }
    
    public void deleteProfilePic(Long userId, Long picId) {
        UserProfilePic profilePic = profilePicRepository.findById(picId)
                .orElseThrow(() -> new RuntimeException("Profile picture not found"));
        
        if (!profilePic.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this profile picture");
        }
        
        // Delete file from disk
        try {
            Files.deleteIfExists(Paths.get(profilePic.getImagePath()));
        } catch (IOException e) {
            // Log error but continue with database deletion
        }
        
        // Delete from database
        profilePicRepository.delete(profilePic);
        
        // Reorder remaining pictures
        reorderProfilePics(userId);
    }
    
    public UserProfilePic setPrimaryProfilePic(Long userId, Long picId) {
        UserProfilePic profilePic = profilePicRepository.findById(picId)
                .orElseThrow(() -> new RuntimeException("Profile picture not found"));
        
        if (!profilePic.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized to modify this profile picture");
        }
        
        // Remove primary flag from all other pictures
        List<UserProfilePic> userPics = profilePicRepository.findByUserIdOrderByDisplayOrderAsc(userId);
        userPics.forEach(pic -> {
            if (!pic.getId().equals(picId)) {
                pic.setIsPrimary(false);
                profilePicRepository.save(pic);
            }
        });
        
        // Set this picture as primary
        profilePic.setIsPrimary(true);
        return profilePicRepository.save(profilePic);
    }
    
    public void reorderProfilePics(Long userId) {
        List<UserProfilePic> userPics = profilePicRepository.findByUserIdOrderByDisplayOrderAsc(userId);
        for (int i = 0; i < userPics.size(); i++) {
            UserProfilePic pic = userPics.get(i);
            pic.setDisplayOrder(i);
            profilePicRepository.save(pic);
        }
    }
    
    public void updateDisplayOrder(Long userId, List<Long> picIdsInOrder) {
        for (int i = 0; i < picIdsInOrder.size(); i++) {
            final int displayOrder = i;
            Long picId = picIdsInOrder.get(i);
            profilePicRepository.findById(picId).ifPresent(pic -> {
                if (pic.getUser().getId().equals(userId)) {
                    pic.setDisplayOrder(displayOrder);
                    profilePicRepository.save(pic);
                }
            });
        }
    }
} 