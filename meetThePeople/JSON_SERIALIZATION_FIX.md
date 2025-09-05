# 🔄 JSON Serialization Fix - Circular Reference Resolution

## 🚨 **Problem Identified**

The application was failing with the following JSON serialization error:

```json
{
  "message": "Runtime error: Could not write JSON: Document nesting depth (1001) exceeds the maximum allowed (1000, from `StreamWriteConstraints.getMaxNestingDepth()`)"
}
```

**Root Cause**: Infinite recursion during JSON serialization due to circular references between `User` and `UserProfilePic` entities.

## 🔍 **Root Cause Analysis**

### **1. Circular Reference Issue**
- **User Entity**: Has `@OneToMany` relationship with `UserProfilePic`
- **UserProfilePic Entity**: Has `@ManyToOne` relationship with `User`
- **Problem**: When Jackson tries to serialize, it goes into infinite recursion

### **2. Entity Structure**
```java
// User.java
@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
private List<UserProfilePic> profilePics = new ArrayList<>();

// UserProfilePic.java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id", nullable = false)
private User user;
```

### **3. Serialization Flow**
1. **Start**: Serialize User entity
2. **Step 1**: Serialize profilePics list
3. **Step 2**: Serialize each UserProfilePic
4. **Step 3**: Serialize User reference in UserProfilePic
5. **Step 4**: Back to Step 1 (infinite loop)

## ✅ **Solution Implemented**

### **1. Jackson Annotations for Circular Reference**

#### **User Entity (`@JsonManagedReference`)**
```java
@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
@JsonManagedReference("user-profile-pics")
private List<UserProfilePic> profilePics = new ArrayList<>();
```

#### **UserProfilePic Entity (`@JsonBackReference`)**
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id", nullable = false)
@JsonBackReference("user-profile-pics")
private User user;
```

#### **How It Works**
- **`@JsonManagedReference`**: Marks the "owning" side of the relationship
- **`@JsonBackReference`**: Marks the "inverse" side that should not be serialized
- **Result**: Prevents infinite recursion during serialization

### **2. Additional Jackson Safety Measures**

#### **@JsonIgnoreProperties**
```java
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {
    // ... entity fields
}
```

#### **Purpose**
- **`hibernateLazyInitializer`**: Prevents Hibernate proxy serialization issues
- **`handler`**: Prevents JPA handler serialization issues
- **Result**: Cleaner JSON output without Hibernate-specific fields

### **3. Jackson Configuration (`JacksonConfig.java`)**

#### **Custom ObjectMapper Configuration**
```java
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Register JavaTimeModule for proper LocalDateTime handling
        mapper.registerModule(new JavaTimeModule());
        
        // Disable features that can cause issues
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Enable features for better serialization
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        return mapper;
    }
}
```

#### **Key Features**
- **JavaTimeModule**: Proper LocalDateTime serialization
- **FAIL_ON_EMPTY_BEANS**: Prevents errors on empty objects
- **WRITE_DATES_AS_TIMESTAMPS**: Uses ISO format for dates
- **INDENT_OUTPUT**: Pretty-printed JSON for debugging

### **4. DTO-Based API Responses**

#### **UserProfileResponseDto Usage**
```java
@GetMapping("/profile")
public ResponseEntity<ApiResponseDto<UserProfileResponseDto>> getProfile(@RequestHeader("Authorization") String token) {
    try {
        String mobile = jwtUtil.getMobileFromToken(token.replace("Bearer ", ""));
        User user = userService.findByMobile(mobile)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Use DTO instead of entity to avoid circular references
        UserProfileResponseDto profileWithActivity = userService.getUserProfileWithActivity(user.getId());
        return ResponseEntity.ok(ApiResponseDto.success("Profile retrieved successfully", profileWithActivity));
    } catch (Exception e) {
        log.error("Failed to get profile for user: {}", e.getMessage(), e);
        return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get profile: " + e.getMessage()));
    }
}
```

#### **Update Profile Response Fix**
```java
@PutMapping("/profile")
public ResponseEntity<ApiResponseDto<UserProfileResponseDto>> updateProfile(@RequestHeader("Authorization") String token,
                                                                         @Valid @RequestBody UserProfileDto profileDto) {
    try {
        String mobile = jwtUtil.getMobileFromToken(token.replace("Bearer ", ""));
        User user = userService.findByMobile(mobile)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        User updatedUser = userService.updateProfile(user.getId(), profileDto);
        
        // Convert to DTO to avoid circular reference issues
        UserProfileResponseDto profileResponse = UserProfileResponseDto.fromUserAndActivity(updatedUser, null);
        return ResponseEntity.ok(ApiResponseDto.success("Profile updated successfully", profileResponse));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to update profile: " + e.getMessage()));
    }
}
```

## 🔒 **Security & Best Practices**

### **1. Entity Serialization Prevention**
- **Never return entities directly** in API responses
- **Always use DTOs** for data transfer
- **Apply Jackson annotations** to prevent serialization issues

### **2. Circular Reference Handling**
- **Use `@JsonManagedReference`** on the owning side
- **Use `@JsonBackReference`** on the inverse side
- **Provide unique names** for complex relationships

### **3. Hibernate Integration**
- **Ignore Hibernate-specific fields** with `@JsonIgnoreProperties`
- **Use DTOs** to avoid lazy loading issues
- **Handle proxy objects** gracefully

## 🧪 **Testing the Fix**

### **1. Test Profile Retrieval**
```bash
curl -X GET "http://localhost:8080/api/users/profile" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected**: 200 OK with profile data (no circular reference errors)

### **2. Test Profile Update**
```bash
curl -X PUT "http://localhost:8080/api/users/profile" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Updated Name", "hobbies": "Reading"}'
```

**Expected**: 200 OK with updated profile data

### **3. Test Profile with Pictures**
```bash
# First upload a profile picture
curl -X POST "http://localhost:8080/api/users/profile-pics/upload" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@profile.jpg" \
  -F "isPrimary=true"

# Then retrieve profile
curl -X GET "http://localhost:8080/api/users/profile" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected**: 200 OK with profile and picture data (no circular reference errors)

## 📊 **Before vs After Comparison**

| Aspect | Before | After |
|--------|--------|-------|
| **JSON Serialization** | Infinite recursion ❌ | Clean serialization ✅ |
| **Circular References** | Not handled ❌ | Properly managed ✅ |
| **API Responses** | Entity serialization ❌ | DTO-based responses ✅ |
| **Error Handling** | 500 Internal Server Error ❌ | Proper error responses ✅ |
| **Performance** | Slow due to recursion ❌ | Fast and efficient ✅ |
| **Maintainability** | Hard to debug ❌ | Easy to maintain ✅ |

## 🔧 **Implementation Details**

### **1. Jackson Annotation Flow**
```java
// 1. User entity is serialized
@JsonManagedReference("user-profile-pics")
private List<UserProfilePic> profilePics;

// 2. Each UserProfilePic is serialized
@JsonBackReference("user-profile-pics")
private User user; // This field is NOT serialized

// 3. Result: Clean JSON without circular references
```

### **2. DTO Conversion Process**
```java
// 1. Entity retrieved from database
User user = userService.findById(userId);

// 2. Converted to DTO
UserProfileResponseDto dto = UserProfileResponseDto.fromUserAndActivity(user, activity);

// 3. DTO returned in API response
return ResponseEntity.ok(ApiResponseDto.success("Success", dto));
```

### **3. Error Prevention**
```java
// 1. Jackson configuration prevents common issues
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

// 2. Circular reference prevention
@JsonManagedReference("user-profile-pics")
@JsonBackReference("user-profile-pics")

// 3. DTO-based responses
UserProfileResponseDto instead of User entity
```

## 🚀 **Deployment Instructions**

### **1. Verify Entity Annotations**
- Ensure `@JsonManagedReference` is on User.profilePics
- Ensure `@JsonBackReference` is on UserProfilePic.user
- Verify `@JsonIgnoreProperties` on both entities

### **2. Check Controller Responses**
- Verify all endpoints return DTOs, not entities
- Check for any remaining direct entity serialization
- Ensure proper error handling

### **3. Test Profile Operations**
```bash
# Test profile retrieval
curl -X GET "http://localhost:8080/api/users/profile" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Test profile update
curl -X PUT "http://localhost:8080/api/users/profile" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Test User"}'
```

### **4. Monitor Logs**
Look for successful profile operations:
```
Profile retrieved successfully for user: 123
Profile updated successfully for user: 123
```

## 🔮 **Future Enhancements**

### **1. Advanced Jackson Configuration**
```java
// Custom serializers for complex objects
@JsonSerialize(using = CustomUserSerializer.class)
public class User {
    // Custom serialization logic
}
```

### **2. Response Caching**
```java
// Cache DTOs to improve performance
@Cacheable("user-profiles")
public UserProfileResponseDto getUserProfileWithActivity(Long userId) {
    // Implementation
}
```

### **3. Validation Enhancement**
```java
// Add validation annotations to DTOs
public class UserProfileResponseDto {
    @NotNull
    private Long id;
    
    @NotBlank
    private String name;
    
    // Validation logic
}
```

## 📚 **References**

- **Jackson Documentation**: JSON processing library
- **Spring Boot**: JSON configuration
- **Hibernate**: Entity relationships
- **JPA**: Entity mapping

## ✅ **Status**

**JSON Serialization Issue**: ✅ **RESOLVED**
**Circular Reference Problem**: ✅ **FIXED**
**Profile Endpoint**: ✅ **WORKING**
**Performance**: ✅ **IMPROVED**
**Code Quality**: ✅ **ENHANCED**

---

*This fix provides a robust solution for JSON serialization issues, preventing circular reference errors while maintaining clean, maintainable code structure.* 