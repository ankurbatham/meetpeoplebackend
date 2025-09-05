package com.meetThePeople.repository;

import com.meetThePeople.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByMobile(String mobile);
    
    boolean existsByMobile(String mobile);
    
    @Query("SELECT u FROM User u WHERE u.gender = :gender " +
           "AND u.pincode = :pincode " +
           "AND u.id NOT IN (SELECT ubm.blockedUser.id FROM UserBlockMapping ubm WHERE ubm.user.id = :userId) " +
           "AND u.id NOT IN (SELECT ubm.user.id FROM UserBlockMapping ubm WHERE ubm.blockedUser.id = :userId) " +
           "AND u.id != :userId")
    List<User> findUsersByGenderAndPincode(@Param("gender") User.Gender gender, 
                                          @Param("pincode") String pincode, 
                                          @Param("userId") Long userId);
    
    @Query("SELECT u FROM User u WHERE u.gender = :gender " +
           "AND u.id NOT IN (SELECT ubm.blockedUser.id FROM UserBlockMapping ubm WHERE ubm.user.id = :userId) " +
           "AND u.id NOT IN (SELECT ubm.user.id FROM UserBlockMapping ubm WHERE ubm.blockedUser.id = :userId) " +
           "AND u.id != :userId")
    List<User> findUsersByGender(@Param("gender") User.Gender gender, @Param("userId") Long userId);
    
    @Query("SELECT u FROM User u WHERE u.id NOT IN (SELECT ubm.blockedUser.id FROM UserBlockMapping ubm WHERE ubm.user.id = :userId) " +
           "AND u.id NOT IN (SELECT ubm.user.id FROM UserBlockMapping ubm WHERE ubm.blockedUser.id = :userId) " +
           "AND u.id != :userId")
    List<User> findAllUsersExceptBlocked(@Param("userId") Long userId);
} 