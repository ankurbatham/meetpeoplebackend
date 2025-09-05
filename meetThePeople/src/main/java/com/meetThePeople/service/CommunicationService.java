package com.meetThePeople.service;

import com.meetThePeople.entity.User;
import com.meetThePeople.entity.UserCommunicationMapping;
import com.meetThePeople.repository.UserCommunicationMappingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommunicationService {
    
    private final UserCommunicationMappingRepository communicationMappingRepository;
    
    public CommunicationService(UserCommunicationMappingRepository communicationMappingRepository) {
        this.communicationMappingRepository = communicationMappingRepository;
    }
    
    public boolean canCommunicate(Long userId, Long otherUserId) {
        return communicationMappingRepository.existsByUser1IdAndUser2Id(userId, otherUserId) ||
               communicationMappingRepository.existsByUser1IdAndUser2Id(otherUserId, userId);
    }
    
    public void establishCommunication(Long userId, Long otherUserId) {
        // Check if communication already exists
        if (!canCommunicate(userId, otherUserId)) {
            UserCommunicationMapping mapping = new UserCommunicationMapping();
            mapping.setUser1(new User());
            mapping.getUser1().setId(userId);
            mapping.setUser2(new User());
            mapping.getUser2().setId(otherUserId);
            mapping.setCanCommunicate(true);
            
            communicationMappingRepository.save(mapping);
        }
    }
    
    public Optional<UserCommunicationMapping> getCommunicationMapping(Long userId, Long otherUserId) {
        return communicationMappingRepository.findByUser1IdAndUser2Id(userId, otherUserId)
                .or(() -> communicationMappingRepository.findByUser1IdAndUser2Id(otherUserId, userId));
    }
} 