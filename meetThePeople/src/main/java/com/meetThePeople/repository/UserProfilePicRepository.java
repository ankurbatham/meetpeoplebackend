package com.meetThePeople.repository;

import com.meetThePeople.entity.UserProfilePic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfilePicRepository extends JpaRepository<UserProfilePic, Long> {
    
    List<UserProfilePic> findByUserIdOrderByDisplayOrderAsc(Long userId);
    
    Optional<UserProfilePic> findByUserIdAndIsPrimaryTrue(Long userId);
    
    @Query("SELECT p FROM UserProfilePic p WHERE p.user.id = :userId ORDER BY p.displayOrder ASC")
    List<UserProfilePic> findProfilePicsByUserIdOrdered(@Param("userId") Long userId);
    
    @Query("SELECT p FROM UserProfilePic p WHERE p.user.id = :userId AND p.isPrimary = true")
    Optional<UserProfilePic> findPrimaryProfilePicByUserId(@Param("userId") Long userId);
    
    void deleteByUserIdAndIdNotIn(Long userId, List<Long> picIds);
} 