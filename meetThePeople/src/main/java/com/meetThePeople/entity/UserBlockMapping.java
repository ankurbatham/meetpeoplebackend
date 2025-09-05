package com.meetThePeople.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_block_mapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBlockMapping {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_user_id", nullable = false)
    private User blockedUser;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    // Composite unique constraint to prevent duplicate block mappings
    @Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "blocked_user_id"})
    })
    public static class UserBlockMappingTable {}
} 