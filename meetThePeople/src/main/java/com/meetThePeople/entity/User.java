package com.meetThePeople.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be 10 digits")
    @Column(unique = true, nullable = false)
    private String mobile;
    
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;
    
    @Past(message = "Date of birth must be in the past")
    private LocalDate dob;
    
    @Size(max = 200, message = "Address must not exceed 200 characters")
    private String address;
    
    @Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be 6 digits")
    private String pincode;
    
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double latitude;
    
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double longitude;
    
    @Size(max = 500, message = "Hobbies must not exceed 500 characters")
    private String hobbies;
    
    @Size(max = 1000, message = "About you must not exceed 1000 characters")
    @Column(name = "about_you")
    private String aboutYou;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("user-profile-pics")
    private List<UserProfilePic> profilePics = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum Gender {
        MALE, FEMALE, OTHER
    }
    
    // Manual getter for id field to resolve compilation issues
    public Long getId() {
        return this.id;
    }
    
    // Manual setter for id field
    public void setId(Long id) {
        this.id = id;
    }
    
    // Convenience methods for managing profile pictures
    public void addProfilePic(UserProfilePic profilePic) {
        profilePics.add(profilePic);
        profilePic.setUser(this);
    }
    
    public void removeProfilePic(UserProfilePic profilePic) {
        profilePics.remove(profilePic);
        profilePic.setUser(null);
    }
    
    public UserProfilePic getPrimaryProfilePic() {
        return profilePics.stream()
                .filter(UserProfilePic::getIsPrimary)
                .findFirst()
                .orElse(profilePics.isEmpty() ? null : profilePics.get(0));
    }
    
    public void setPrimaryProfilePic(UserProfilePic profilePic) {
        // Remove primary flag from all other pics
        profilePics.forEach(pic -> pic.setIsPrimary(false));
        // Set the selected pic as primary
        if (profilePic != null) {
            profilePic.setIsPrimary(true);
        }
    }
} 