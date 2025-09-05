package com.meetThePeople.repository;

import com.meetThePeople.entity.UserBlockMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBlockMappingRepository extends JpaRepository<UserBlockMapping, Long> {
    
    @Query("SELECT ubm FROM UserBlockMapping ubm WHERE " +
           "(ubm.user.id = :userId AND ubm.blockedUser.id = :blockedUserId) OR " +
           "(ubm.user.id = :blockedUserId AND ubm.blockedUser.id = :userId)")
    Optional<UserBlockMapping> findByUserAndBlockedUser(@Param("userId") Long userId, 
                                                       @Param("blockedUserId") Long blockedUserId);
    
    @Query("SELECT ubm FROM UserBlockMapping ubm WHERE ubm.user.id = :userId")
    List<UserBlockMapping> findBlockedUsersByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(ubm) > 0 FROM UserBlockMapping ubm WHERE " +
           "(ubm.user.id = :userId AND ubm.blockedUser.id = :blockedUserId) OR " +
           "(ubm.user.id = :blockedUserId AND ubm.blockedUser.id = :userId)")
    boolean existsByUserIdAndBlockedUserId(@Param("userId") Long userId, @Param("blockedUserId") Long blockedUserId);
} 