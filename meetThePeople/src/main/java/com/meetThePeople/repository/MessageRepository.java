package com.meetThePeople.repository;

import com.meetThePeople.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender.id = :userId AND m.receiver.id = :otherUserId) OR " +
           "(m.sender.id = :otherUserId AND m.receiver.id = :userId) " +
           "ORDER BY m.createdAt DESC")
    List<Message> findMessagesBetweenUsers(@Param("userId") Long userId, 
                                          @Param("otherUserId") Long otherUserId);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE " +
           "m.sender.id = :senderId AND m.receiver.id = :receiverId")
    long countMessagesFromSenderToReceiver(@Param("senderId") Long senderId, 
                                          @Param("receiverId") Long receiverId);
    
    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender.id = :userId AND m.receiver.id = :otherUserId) OR " +
           "(m.sender.id = :otherUserId AND m.receiver.id = :userId) " +
           "ORDER BY m.createdAt DESC")
    List<Message> findMessagesBetweenUsersOrdered(@Param("userId") Long userId, 
                                                 @Param("otherUserId") Long otherUserId);
    
    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender.id = :userId AND m.receiver.id = :otherUserId) OR " +
           "(m.sender.id = :otherUserId AND m.receiver.id = :userId) " +
           "ORDER BY m.createdAt ASC")
    List<Message> findMessagesBetweenUsersOrderedAsc(@Param("userId") Long userId, 
                                                    @Param("otherUserId") Long otherUserId);
    
    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender.id = :userId AND m.receiver.id = :otherUserId) OR " +
           "(m.sender.id = :otherUserId AND m.receiver.id = :userId) " +
           "ORDER BY m.createdAt DESC")
    List<Message> findMessagesBetweenUsersWithLimit(@Param("userId") Long userId, 
                                                   @Param("otherUserId") Long otherUserId, 
                                                   org.springframework.data.domain.Pageable pageable);
    
    @Query("SELECT m.id FROM Message m WHERE " +
           "(m.sender.id = :userId AND m.receiver.id = :otherUserId) OR " +
           "(m.sender.id = :otherUserId AND m.receiver.id = :userId) " +
           "ORDER BY m.createdAt ASC")
    List<Long> findMessageIdsBetweenUsersOrderedAsc(@Param("userId") Long userId, 
                                                   @Param("otherUserId") Long otherUserId);
    
    @Query("SELECT DISTINCT " +
           "CASE WHEN m.sender.id < m.receiver.id THEN m.sender.id ELSE m.receiver.id END as user1, " +
           "CASE WHEN m.sender.id < m.receiver.id THEN m.receiver.id ELSE m.sender.id END as user2 " +
           "FROM Message m")
    List<Object[]> findDistinctUserPairs();
    
    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender.id = :userId1 AND m.receiver.id = :userId2) OR " +
           "(m.sender.id = :userId2 AND m.receiver.id = :userId1) " +
           "ORDER BY m.createdAt DESC LIMIT 1")
    Message findLastMessageBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
} 