package com.meetThePeople.repository;

import com.meetThePeople.entity.UserCommunicationMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCommunicationMappingRepository extends JpaRepository<UserCommunicationMapping, Long> {
    
    @Query("SELECT ucm FROM UserCommunicationMapping ucm WHERE " +
           "(ucm.user1.id = :userId1 AND ucm.user2.id = :userId2) OR " +
           "(ucm.user1.id = :userId2 AND ucm.user2.id = :userId1)")
    Optional<UserCommunicationMapping> findByUser1IdAndUser2Id(@Param("userId1") Long userId1, 
                                                               @Param("userId2") Long userId2);
    
    @Query("SELECT ucm FROM UserCommunicationMapping ucm WHERE ucm.user1.id = :userId OR ucm.user2.id = :userId")
    List<UserCommunicationMapping> findByUser1IdOrUser2Id(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(ucm) > 0 FROM UserCommunicationMapping ucm WHERE " +
           "(ucm.user1.id = :userId1 AND ucm.user2.id = :userId2) OR " +
           "(ucm.user1.id = :userId2 AND ucm.user2.id = :userId1)")
    boolean existsByUser1IdAndUser2Id(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
} 