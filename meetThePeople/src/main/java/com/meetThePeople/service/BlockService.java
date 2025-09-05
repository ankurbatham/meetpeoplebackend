package com.meetThePeople.service;

import com.meetThePeople.entity.User;
import com.meetThePeople.entity.UserBlockMapping;
import com.meetThePeople.repository.UserBlockMappingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BlockService {
    
    private final UserBlockMappingRepository blockMappingRepository;
    
    public BlockService(UserBlockMappingRepository blockMappingRepository) {
        this.blockMappingRepository = blockMappingRepository;
    }
    
    public void blockUser(Long userId, Long blockedUserId) {
        // Check if already blocked
        if (!isBlocked(userId, blockedUserId)) {
            UserBlockMapping blockMapping = new UserBlockMapping();
            blockMapping.setUser(new User());
            blockMapping.getUser().setId(userId);
            blockMapping.setBlockedUser(new User());
            blockMapping.getBlockedUser().setId(blockedUserId);
            
            blockMappingRepository.save(blockMapping);
        }
    }
    
    public void unblockUser(Long userId, Long blockedUserId) {
        Optional<UserBlockMapping> blockMapping = blockMappingRepository
                .findByUserAndBlockedUser(userId, blockedUserId);
        
        blockMapping.ifPresent(blockMappingRepository::delete);
    }
    
    public boolean isBlocked(Long userId, Long otherUserId) {
        return blockMappingRepository.existsByUserIdAndBlockedUserId(userId, otherUserId) ||
               blockMappingRepository.existsByUserIdAndBlockedUserId(otherUserId, userId);
    }
    
    public List<UserBlockMapping> getBlockedUsers(Long userId) {
        return blockMappingRepository.findBlockedUsersByUserId(userId);
    }
    
    public Optional<UserBlockMapping> getBlockMapping(Long userId, Long otherUserId) {
        return blockMappingRepository.findByUserAndBlockedUser(userId, otherUserId);
    }
} 