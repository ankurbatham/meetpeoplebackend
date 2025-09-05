package com.meetThePeople.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_communication_mapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCommunicationMapping {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id", nullable = false)
    private User user1;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id", nullable = false)
    private User user2;
    
    @Column(name = "can_communicate")
    private Boolean canCommunicate = false;
    
    @CreationTimestamp
    @Column(name = "established_at", updatable = false)
    private LocalDateTime establishedAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Composite unique constraint to prevent duplicate mappings
    @Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "connected_user_id"})
    })
    public static class UserCommunicationMappingTable {}
} 