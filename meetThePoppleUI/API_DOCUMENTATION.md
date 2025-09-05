# Meet The People - API Documentation

## 📋 Table of Contents
1. [Overview](#overview)
2. [Authentication](#authentication)
3. [Base URL & Headers](#base-url--headers)
4. [Error Handling](#error-handling)
5. [API Endpoints](#api-endpoints)
6. [Data Models](#data-models)
7. [Rate Limiting](#rate-limiting)
8. [Testing](#testing)

---

## 🌟 Overview

Meet The People is a social networking platform that allows users to discover, connect, and communicate with other users based on location, interests, and preferences. The API provides comprehensive functionality for user management, messaging, profile management, and social interactions.

### **Key Features:**
- **User Authentication**: OTP-based mobile verification
- **Profile Management**: Complete user profiles with multiple photos
- **User Discovery**: Advanced search with filters (gender, location, age, distance)
- **Messaging System**: Text, image, and voice messaging
- **Online Status**: Real-time user activity tracking
- **User Blocking**: Privacy and safety controls
- **Message Retention**: Configurable message storage policies

---

## 🔐 Authentication

The API uses JWT (JSON Web Token) authentication. All protected endpoints require a valid JWT token in the Authorization header.

### **Authentication Flow:**
1. **Generate OTP**: Send mobile number to receive OTP
2. **Verify OTP**: Submit OTP to get JWT token
3. **Use Token**: Include token in subsequent requests

### **Token Format:**
```
Authorization: Bearer <JWT_TOKEN>
```

### **Token Expiry:**
- **Default**: 24 hours (86,400,000 milliseconds)
- **Configurable**: Set in `application.properties`

---

## 🌐 Base URL & Headers

### **Base URL:**
```
http://localhost:8080/api
```

### **Required Headers:**
```http
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>  # For protected endpoints
```

### **Optional Headers:**
```http
Accept: application/json
User-Agent: MeetThePeople/1.0
```

---

## ⚠️ Error Handling

### **Standard Error Response Format:**
```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

### **HTTP Status Codes:**
- **200**: Success
- **201**: Created
- **400**: Bad Request (validation errors)
- **401**: Unauthorized (invalid/missing token)
- **403**: Forbidden (insufficient permissions)
- **404**: Not Found
- **500**: Internal Server Error

### **Common Error Messages:**
- `"User not found"`
- `"Invalid or expired OTP"`
- `"Cannot send message. Communication not established."`
- `"Unauthorized to delete this message"`
- `"User is blocked"`

---

## 🚀 API Endpoints

### **1. Authentication Endpoints**

#### **1.1 Generate OTP**
```http
POST /auth/generate-otp
```

**Request Body:**
```json
{
  "mobile": "9876543210"
}
```

**Response:**
```json
{
  "success": true,
  "message": "OTP generated successfully",
  "data": "123456"
}
```

**Validation Rules:**
- Mobile number must be exactly 10 digits
- Mobile number must be valid Indian format

---

#### **1.2 Login with OTP**
```http
POST /auth/login
```

**Request Body:**
```json
{
  "mobile": "9876543210",
  "otp": "123456"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "mobile": "9876543210",
      "name": "User",
      "createdAt": "2024-01-15T10:30:00"
    }
  }
}
```

**Notes:**
- Creates new user if mobile number doesn't exist
- Returns JWT token for subsequent requests
- User profile can be updated after first login

---

### **2. User Profile Endpoints**

#### **2.1 Get Current User Profile**
```http
GET /users/profile
Authorization: Bearer <JWT_TOKEN>
```

**Response:**
```json
{
  "success": true,
  "message": "Profile retrieved successfully",
  "data": {
    "id": 1,
    "mobile": "9876543210",
    "name": "John Doe",
    "gender": "MALE",
    "dob": "1990-01-01",
    "address": "123 Main St, Bangalore",
    "pincode": "560001",
    "latitude": 12.9716,
    "longitude": 77.5946,
    "hobbies": "Reading, Traveling, Photography",
    "aboutYou": "I love meeting new people and exploring new places",
    "profilePics": [
      {
        "id": 1,
        "imagePath": "/uploads/profile-pics/abc123.jpg",
        "imageName": "profile.jpg",
        "imageType": "image/jpeg",
        "fileSize": 1024000,
        "isPrimary": true,
        "displayOrder": 0,
        "createdAt": "2024-01-15T10:30:00",
        "updatedAt": "2024-01-15T10:30:00"
      }
    ],
    "onlineStatus": "online",
    "lastActiveTime": "2024-01-15T10:30:00",
    "lastActiveSource": "APP",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
}
```

---

#### **2.2 Update User Profile**
```http
PUT /users/profile
Authorization: Bearer <JWT_TOKEN>
```

**Request Body:**
```json
{
  "name": "John Doe",
  "gender": "MALE",
  "dob": "1990-01-01",
  "address": "123 Main St, Bangalore",
  "pincode": "560001",
  "latitude": 12.9716,
  "longitude": 77.5946,
  "hobbies": "Reading, Traveling, Photography",
  "aboutYou": "I love meeting new people and exploring new places"
}
```

**Validation Rules:**
- **name**: 2-50 characters, required
- **gender**: MALE, FEMALE, or OTHER, required
- **dob**: Must be in the past, required
- **address**: Max 200 characters
- **pincode**: Exactly 6 digits
- **latitude**: Between -90 and 90
- **longitude**: Between -180 and 180
- **hobbies**: Max 500 characters
- **aboutYou**: Max 1000 characters

**Response:**
```json
{
  "success": true,
  "message": "Profile updated successfully",
  "data": {
    "id": 1,
    "mobile": "9876543210",
    "name": "John Doe",
    "gender": "MALE",
    "dob": "1990-01-01",
    "address": "123 Main St, Bangalore",
    "pincode": "560001",
    "latitude": 12.9716,
    "longitude": 77.5946,
    "hobbies": "Reading, Traveling, Photography",
    "aboutYou": "I love meeting new people and exploring new places",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
}
```

---

#### **2.3 Get Any User's Profile**
```http
GET /users/{userId}/profile
Authorization: Bearer <JWT_TOKEN>
```

**Response:** Same format as current user profile

---

#### **2.4 Get User Communication Details**
```http
GET /users/{userId}/communication
Authorization: Bearer <JWT_TOKEN>
```

**Response:**
```json
{
  "success": true,
  "message": "User communication details retrieved successfully",
  "data": {
    "userId": 2,
    "userName": "Jane Smith",
    "userMobile": "9876543211",
    "userGender": "FEMALE",
    "userAddress": "456 Oak Ave, Bangalore",
    "userPincode": "560001",
    "userLatitude": 12.9717,
    "userLongitude": 77.5947,
    "userHobbies": "Cooking, Painting, Reading",
    "userAboutYou": "Creative soul looking for meaningful connections",
    "userProfilePics": [...],
    "userCreatedAt": "2024-01-15T09:00:00",
    "userUpdatedAt": "2024-01-15T10:30:00",
    "onlineStatus": "online",
    "lastActiveTime": "2024-01-15T10:45:00",
    "lastActiveSource": "APP",
    "communicationId": 1,
    "canCommunicate": true,
    "communicationEstablishedAt": "2024-01-15T10:00:00",
    "communicationUpdatedAt": "2024-01-15T10:00:00",
    "lastMessageId": 5,
    "lastMessageType": "TEXT",
    "lastMessageContent": "Hello! How are you doing?",
    "lastMessageMediaPath": null,
    "lastMessageTime": "2024-01-15T10:30:00",
    "isLastMessageFromMe": true
  }
}
```

**Features:**
- **Complete user profile** with all details
- **Online status** (online/offline with 60-second threshold)
- **Communication mapping** details (can communicate, established date)
- **Last message information** (content, type, timestamp, sender)
- **Profile pictures** with metadata

---

### **3. Profile Picture Management**

#### **3.1 Upload Profile Picture**
```http
POST /profile-pics/upload
Authorization: Bearer <JWT_TOKEN>
Content-Type: multipart/form-data
```

**Form Data:**
- `file`: Image file (JPEG, PNG, GIF)
- `isPrimary`: boolean (optional, default: false)

**Response:**
```json
{
  "success": true,
  "message": "Profile picture uploaded successfully",
  "data": {
    "id": 2,
    "imagePath": "/uploads/profile-pics/def456.jpg",
    "imageName": "new_profile.jpg",
    "imageType": "image/jpeg",
    "fileSize": 2048000,
    "isPrimary": false,
    "displayOrder": 1,
    "createdAt": "2024-01-15T11:00:00",
    "updatedAt": "2024-01-15T11:00:00"
  }
}
```

**File Requirements:**
- **Max Size**: 10MB
- **Formats**: JPEG, PNG, GIF
- **Auto-naming**: Unique filenames generated

---

#### **3.2 Get User Profile Pictures**
```http
GET /profile-pics/{userId}
Authorization: Bearer <JWT_TOKEN>
```

**Response:**
```json
{
  "success": true,
  "message": "Profile pictures retrieved successfully",
  "data": [
    {
      "id": 1,
      "imagePath": "/uploads/profile-pics/abc123.jpg",
      "imageName": "profile.jpg",
      "imageType": "image/jpeg",
      "fileSize": 1024000,
      "isPrimary": true,
      "displayOrder": 0,
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00"
    }
  ]
}
```

---

#### **3.3 Set Primary Profile Picture**
```http
PUT /profile-pics/{picId}/primary
Authorization: Bearer <JWT_TOKEN>
```

**Response:**
```json
{
  "success": true,
  "message": "Primary profile picture set successfully",
  "data": {
    "id": 2,
    "isPrimary": true,
    "displayOrder": 1
  }
}
```

---

#### **3.4 Delete Profile Picture**
```http
DELETE /profile-pics/{picId}
Authorization: Bearer <JWT_TOKEN>
```

**Response:**
```json
{
  "success": true,
  "message": "Profile picture deleted successfully",
  "data": null
}
```

---

#### **3.5 Reorder Profile Pictures**
```http
PUT /profile-pics/reorder
Authorization: Bearer <JWT_TOKEN>
```

**Request Body:**
```json
[1, 3, 2, 4]
```

**Response:**
```json
{
  "success": true,
  "message": "Display order updated successfully",
  "data": null
}
```

---

### **4. User Search Endpoints**

#### **4.1 Search Users**
```http
POST /users/search
Authorization: Bearer <JWT_TOKEN>
```

**Request Body:**
```json
{
  "gender": "FEMALE",
  "pincode": "560001",
  "ageGroup": "25-35",
  "maxDistanceKm": 10.0,
  "userLatitude": 12.9716,
  "userLongitude": 77.5946
}
```

**Search Parameters:**
- **gender**: MALE, FEMALE, OTHER (optional)
- **pincode**: 6-digit pincode (optional)
- **ageGroup**: "18-25", "26-35", "36-45", "46+" (optional)
- **maxDistanceKm**: Maximum distance in kilometers (optional)
- **userLatitude**: Current user's latitude (required if distance filter)
- **userLongitude**: Current user's longitude (required if distance filter)

**Response:**
```json
{
  "success": true,
  "message": "Users found successfully",
  "data": [
    {
      "id": 2,
      "name": "Jane Smith",
      "gender": "FEMALE",
      "dob": "1992-05-15",
      "address": "456 Oak Ave, Bangalore",
      "pincode": "560001",
      "latitude": 12.9717,
      "longitude": 77.5947,
      "hobbies": "Cooking, Painting, Reading",
      "aboutYou": "Creative soul looking for meaningful connections",
      "profilePics": [...],
      "onlineStatus": "online",
      "lastActiveTime": "2024-01-15T10:45:00",
      "lastActiveSource": "APP",
      "createdAt": "2024-01-15T09:00:00",
      "updatedAt": "2024-01-15T10:30:00"
    }
  ]
}
```

**Search Logic:**
1. **Gender + Pincode**: Most specific search
2. **Gender Only**: Broader search by gender
3. **No Filters**: All users (except blocked)
4. **Age Filter**: Applied to results
5. **Distance Filter**: Applied with location sorting

---

### **5. Messaging Endpoints**

#### **5.1 Send Text Message**
```http
POST /messages/send
Authorization: Bearer <JWT_TOKEN>
```

**Request Body:**
```json
{
  "receiverId": 2,
  "messageType": "TEXT",
  "textContent": "Hello! How are you doing today?",
  "mediaPath": null
}
```

**Message Types:**
- **TEXT**: Text-only messages
- **IMAGE**: Image messages with optional text
- **VOICE**: Voice messages with optional text

**Response:**
```json
{
  "success": true,
  "message": "Message sent successfully",
  "data": {
    "id": 1,
    "sender": {
      "id": 1,
      "name": "John Doe"
    },
    "receiver": {
      "id": 2,
      "name": "Jane Smith"
    },
    "messageType": "TEXT",
    "textContent": "Hello! How are you doing today?",
    "mediaPath": null,
    "createdAt": "2024-01-15T11:00:00"
  }
}
```

---

#### **5.2 Send Media Message**
```http
POST /messages/send-media
Authorization: Bearer <JWT_TOKEN>
Content-Type: multipart/form-data
```

**Form Data:**
- `receiverId`: User ID to send message to
- `messageType`: IMAGE or VOICE
- `textContent`: Optional text content
- `mediaFile`: Media file (image or audio)

**File Requirements:**
- **Images**: JPEG, PNG, GIF (max 10MB)
- **Voice**: MP3, WAV, M4A (max 25MB)

---

#### **5.3 Get Conversation**
```http
GET /messages/conversation/{otherUserId}
Authorization: Bearer <JWT_TOKEN>
```

**Response:**
```json
{
  "success": true,
  "message": "Conversation retrieved successfully",
  "data": [
    {
      "id": 1,
      "sender": {
        "id": 1,
        "name": "John Doe"
      },
      "receiver": {
        "id": 2,
        "name": "Jane Smith"
      },
      "messageType": "TEXT",
      "textContent": "Hello! How are you doing today?",
      "mediaPath": null,
      "createdAt": "2024-01-15T11:00:00"
    }
  ]
}
```

**Notes:**
- Messages are ordered by creation time (newest first)
- Only last N messages are returned (based on retention policy)
- Media files are accessible via mediaPath

---

#### **5.4 Delete Message**
```http
DELETE /messages/{messageId}
Authorization: Bearer <JWT_TOKEN>
```

**Response:**
```json
{
  "success": true,
  "message": "Message deleted successfully",
  "data": null
}
```

**Restrictions:**
- Only sender can delete messages
- Associated media files are automatically deleted

---

### **6. User Activity Endpoints**

#### **6.1 Capture User Activity**
```http
POST /activity/capture
Authorization: Bearer <JWT_TOKEN>
```

**Query Parameters:**
- `source`: APP or WEBSITE

**Response:**
```json
{
  "success": true,
  "message": "Activity captured successfully",
  "data": null
}
```

**Usage:**
- Call this endpoint periodically to maintain online status
- Recommended frequency: Every 30-60 seconds
- Source indicates platform (APP or WEBSITE)

---

#### **6.2 Get Activity Status**
```http
GET /activity/status
Authorization: Bearer <JWT_TOKEN>
```

**Response:**
```json
{
  "success": true,
  "message": "Activity status retrieved successfully",
  "data": {
    "id": 1,
    "user": {
      "id": 1,
      "name": "John Doe"
    },
    "activeSource": "APP",
    "lastActiveTime": "2024-01-15T11:00:00",
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

---

### **7. User Blocking Endpoints**

#### **7.1 Block User**
```http
POST /block/{userId}
Authorization: Bearer <JWT_TOKEN>
```

**Response:**
```json
{
  "success": true,
  "message": "User blocked successfully",
  "data": null
}
```

**Effects:**
- Blocked user cannot send messages
- Blocked user cannot see blocker's profile
- Blocked user appears in blocked users list

---

#### **7.2 Unblock User**
```http
DELETE /block/{userId}
Authorization: Bearer <JWT_TOKEN>
```

**Response:**
```json
{
  "success": true,
  "message": "User unblocked successfully",
  "data": null
}
```

---

#### **7.3 Get Blocked Users List**
```http
GET /block/list
Authorization: Bearer <JWT_TOKEN>
```

**Response:**
```json
{
  "success": true,
  "message": "Blocked users retrieved successfully",
  "data": [
    {
      "id": 1,
      "blocker": {
        "id": 1,
        "name": "John Doe"
      },
      "blocked": {
        "id": 3,
        "name": "Mike Johnson"
      },
      "blockedAt": "2024-01-15T10:00:00",
      "reason": "Inappropriate behavior"
    }
  ]
}
```

---

#### **7.4 Check if User is Blocked**
```http
GET /block/check/{userId}
Authorization: Bearer <JWT_TOKEN>
```

**Response:**
```json
{
  "success": true,
  "message": "Block status checked successfully",
  "data": {
    "isBlocked": true,
    "blockedAt": "2024-01-15T10:00:00",
    "reason": "Inappropriate behavior"
  }
}
```

---

### **8. Message Retention Management**

#### **8.1 Get Retention Configuration**
```http
GET /message-retention/config
Authorization: Bearer <JWT_TOKEN>
```

**Response:**
```json
{
  "success": true,
  "message": "Retention configuration retrieved successfully",
  "data": {
    "enabled": true,
    "retentionCount": 3
  }
}
```

---

#### **8.2 Update Retention Configuration**
```http
PUT /message-retention/config
Authorization: Bearer <JWT_TOKEN>
```

**Query Parameters:**
- `retentionCount`: Number of messages to keep (1-100)
- `enabled`: true/false to enable/disable retention

**Response:**
```json
{
  "success": true,
  "message": "Retention configuration updated: enabled=true, count=5",
  "data": null
}
```

---

#### **8.3 Get Retention Statistics**
```http
GET /message-retention/stats/{otherUserId}
Authorization: Bearer <JWT_TOKEN>
```

**Response:**
```json
{
  "success": true,
  "message": "Retention statistics retrieved successfully",
  "data": {
    "totalMessages": 15,
    "retentionCount": 3,
    "messagesToDelete": 12,
    "needsCleanup": true
  }
}
```

---

#### **8.4 Manual Conversation Cleanup**
```http
POST /message-retention/cleanup/{otherUserId}
Authorization: Bearer <JWT_TOKEN>
```

**Response:**
```json
{
  "success": true,
  "message": "Conversation cleanup completed successfully",
  "data": null
}
```

---

### **9. Health Check**

#### **9.1 Service Health**
```http
GET /health
```

**Response:**
```json
{
  "success": true,
  "message": "Service is healthy",
  "data": {
    "status": "UP",
    "timestamp": "2024-01-15T11:00:00",
    "service": "Meet The People API",
    "version": "1.0.0"
  }
}
```

---

## 📊 Data Models

### **User Entity**
```json
{
  "id": "Long",
  "mobile": "String (10 digits)",
  "name": "String (2-50 chars)",
  "gender": "ENUM (MALE, FEMALE, OTHER)",
  "dob": "Date (past date)",
  "address": "String (max 200 chars)",
  "pincode": "String (6 digits)",
  "latitude": "Double (-90 to 90)",
  "longitude": "Double (-180 to 180)",
  "hobbies": "String (max 500 chars)",
  "aboutYou": "String (max 1000 chars)",
  "createdAt": "Timestamp",
  "updatedAt": "Timestamp"
}
```

### **Message Entity**
```json
{
  "id": "Long",
  "sender": "User",
  "receiver": "User",
  "messageType": "ENUM (TEXT, IMAGE, VOICE)",
  "textContent": "String (TEXT)",
  "mediaPath": "String (IMAGE, VOICE)",
  "createdAt": "Timestamp"
}
```

### **UserActivity Entity**
```json
{
  "id": "Long",
  "user": "User",
  "activeSource": "ENUM (APP, WEBSITE)",
  "lastActiveTime": "Timestamp",
  "createdAt": "Timestamp"
}
```

---

## 🚦 Rate Limiting

### **Current Limits:**
- **OTP Generation**: 3 requests per 5 minutes per mobile number
- **Login Attempts**: 5 attempts per 15 minutes per mobile number
- **Message Sending**: 100 messages per hour per user
- **Profile Updates**: 10 updates per hour per user

### **Rate Limit Headers:**
```http
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1642233600
```

---

## 🧪 Testing

### **1. Postman Collection**
Import the following collection for testing:
```json
{
  "info": {
    "name": "Meet The People API",
    "description": "Complete API testing collection"
  },
  "item": [
    {
      "name": "Authentication",
      "item": [
        {
          "name": "Generate OTP",
          "request": {
            "method": "POST",
            "url": "{{baseUrl}}/auth/generate-otp",
            "body": {
              "mode": "raw",
              "raw": "{\n  \"mobile\": \"9876543210\"\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            }
          }
        }
      ]
    }
  ]
}
```

### **2. cURL Examples**

#### **Generate OTP:**
```bash
curl -X POST "http://localhost:8080/api/auth/generate-otp" \
  -H "Content-Type: application/json" \
  -d '{"mobile": "9876543210"}'
```

#### **Login with OTP:**
```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"mobile": "9876543210", "otp": "123456"}'
```

#### **Update Profile:**
```bash
curl -X PUT "http://localhost:8080/api/users/profile" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "John Doe",
    "gender": "MALE",
    "dob": "1990-01-01",
    "address": "123 Main St",
    "pincode": "123456",
    "latitude": 12.9716,
    "longitude": 77.5946,
    "hobbies": "Reading, Traveling",
    "aboutYou": "I love meeting new people"
  }'
```

#### **Search Users:**
```bash
curl -X POST "http://localhost:8080/api/users/search" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "gender": "FEMALE",
    "maxDistanceKm": 10.0,
    "userLatitude": 12.9716,
    "userLongitude": 77.5946
  }'
```

#### **Get User Communication Details:**
```bash
curl -X GET "http://localhost:8080/api/users/123/communication" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### **3. Test Data Setup**

#### **Create Test Users:**
```sql
-- Insert test users
INSERT INTO users (mobile, name, gender, dob, address, pincode, latitude, longitude, hobbies, about_you) VALUES
('9876543210', 'John Doe', 'MALE', '1990-01-01', '123 Main St', '123456', 12.9716, 77.5946, 'Reading, Traveling', 'I love meeting new people'),
('9876543211', 'Jane Smith', 'FEMALE', '1992-05-15', '456 Oak Ave', '123456', 12.9717, 77.5947, 'Cooking, Painting', 'Creative soul looking for connections'),
('9876543212', 'Mike Johnson', 'MALE', '1988-12-10', '789 Pine Rd', '654321', 12.9718, 77.5948, 'Sports, Music', 'Athletic and musical person');
```

---

## 📝 Notes & Best Practices

### **1. API Usage Guidelines:**
- **Always handle errors gracefully** in your application
- **Implement proper retry logic** for transient failures
- **Cache user profiles** to reduce API calls
- **Use pagination** for large result sets (when implemented)
- **Implement proper logging** for debugging

### **2. Security Considerations:**
- **Never store JWT tokens** in localStorage (use httpOnly cookies)
- **Validate all inputs** on both client and server
- **Implement proper CORS** configuration
- **Use HTTPS** in production
- **Regular token rotation** for security

### **3. Performance Tips:**
- **Batch API calls** when possible
- **Use appropriate indexes** (already implemented in schema)
- **Implement caching** for frequently accessed data
- **Monitor API response times** and optimize slow endpoints

### **4. Mobile App Integration:**
- **Implement offline support** for basic functionality
- **Use push notifications** for new messages
- **Implement proper error handling** for network issues
- **Optimize image uploads** with compression

---

## 🔄 API Versioning

### **Current Version: v1**
- **Base Path**: `/api`
- **Version Header**: `X-API-Version: 1`

### **Future Versions:**
- **v2**: Will include breaking changes
- **Migration Guide**: Will be provided for version upgrades
- **Deprecation Policy**: 6 months notice for deprecated endpoints

---

## 📞 Support & Contact

### **Technical Support:**
- **Email**: support@meetthepeople.com
- **Documentation**: https://docs.meetthepeople.com
- **GitHub Issues**: https://github.com/meetthepeople/api/issues

### **API Status:**
- **Status Page**: https://status.meetthepeople.com
- **Uptime**: 99.9% SLA
- **Response Time**: < 200ms average

---

## 📚 Additional Resources

### **SDKs & Libraries:**
- **JavaScript/TypeScript**: `npm install meetthepeople-sdk`
- **Python**: `pip install meetthepeople-python`
- **Java**: Maven dependency available
- **Swift**: CocoaPods integration
- **Kotlin**: Gradle dependency

### **Code Examples:**
- **GitHub Repository**: https://github.com/meetthepeople/examples
- **Sample Apps**: iOS, Android, Web examples
- **Integration Guides**: Step-by-step tutorials

---

*This documentation is maintained by the Meet The People development team. Last updated: January 2024* 