package com.meetThePeople.repository;

import com.meetThePeople.entity.UserActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {
    
    Optional<UserActivity> findByUserId(Long userId);
    
    void deleteByUserId(Long userId);
} 