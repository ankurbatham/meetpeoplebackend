# Meet The People - Spring Boot Backend

A complete Spring Boot backend application for a social networking platform with user matching, messaging, and profile management features.

## Features

- **OTP-based Authentication**: Secure login with mobile number and OTP
- **User Profile Management**: Complete profile creation and editing
- **User Search**: Advanced search with filters (gender, pincode, age, distance)
- **Messaging System**: Text, image, and voice messaging with communication rules
- **User Blocking**: Block/unblock users functionality
- **Activity Tracking**: Track user activity from app or website
- **File Management**: Profile pictures and media file handling

## Technology Stack

- **Backend**: Java 17, Spring Boot 3.5.5
- **Database**: MySQL 8.0
- **ORM**: Spring Data JPA with Hibernate
- **Security**: Spring Security with JWT
- **File Storage**: Local filesystem
- **Build Tool**: Maven

## Database Schema

### Tables
1. **users** - User profiles and information
2. **otp_verification** - OTP management
3. **user_activity** - User activity tracking
4. **user_communication_mapping** - Communication permissions
5. **user_block_mapping** - User blocking relationships
6. **messages** - Message storage

## API Endpoints

### Authentication
- `POST /api/auth/generate-otp` - Generate OTP for mobile number
- `POST /api/auth/login` - Login with mobile and OTP

### User Management
- `GET /api/users/profile` - Get user profile
- `PUT /api/users/profile` - Update user profile
- `POST /api/users/profile-picture` - Upload profile picture
- `DELETE /api/users/profile-picture` - Delete profile picture
- `POST /api/users/search` - Search users with filters

### Activity Tracking
- `POST /api/activity/capture` - Capture user activity
- `GET /api/activity/status` - Get user activity status

### Messaging
- `POST /api/messages/send` - Send text message
- `POST /api/messages/send-media` - Send media message
- `GET /api/messages/conversation/{userId}` - Get conversation
- `DELETE /api/messages/{messageId}` - Delete message

### User Blocking
- `POST /api/block/{userId}` - Block user
- `DELETE /api/block/{userId}` - Unblock user
- `GET /api/block/list` - Get blocked users list
- `GET /api/block/check/{userId}` - Check if user is blocked

## Setup Instructions

### Prerequisites
- Java 17 or higher
- MySQL 8.0 or higher
- Maven 3.6 or higher

### Database Setup
1. Create MySQL database:
```sql
CREATE DATABASE meet_the_people;
```

2. Update `application.properties` with your database credentials:
```properties
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Application Setup
1. Clone the repository
2. Navigate to project directory
3. Run the application:
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### API Testing

#### 1. Generate OTP
```bash
curl -X POST http://localhost:8080/api/auth/generate-otp \
  -H "Content-Type: application/json" \
  -d '{"mobile": "1234567890"}'
```

#### 2. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"mobile": "1234567890", "otp": "123456"}'
```

#### 3. Update Profile
```bash
curl -X PUT http://localhost:8080/api/users/profile \
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

#### 4. Search Users
```bash
curl -X POST http://localhost:8080/api/users/search \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "gender": "FEMALE",
    "pincode": "123456",
    "ageGroup": "25-35",
    "maxDistanceKm": 10.0,
    "userLatitude": 12.9716,
    "userLongitude": 77.5946
  }'
```

## Key Features Implementation

### Distance Calculation
Uses Haversine formula to calculate distances between users based on latitude/longitude coordinates.

### Communication Rules
- Users can initially send only 1 message to another user
- If the receiver replies, both can communicate freely
- Communication mapping is maintained to track permissions

### File Storage
- Profile pictures, images, and voice messages are stored locally
- Files are organized in separate directories
- Unique filenames are generated to prevent conflicts

### Security
- JWT-based authentication
- CORS enabled for cross-origin requests
- Input validation and sanitization
- Exception handling and error responses

## Configuration

Key configuration properties in `application.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/meet_the_people
spring.datasource.username=root
spring.datasource.password=password

# JWT
app.jwt.secret=your_jwt_secret_key
app.jwt.expiration=86400000

# OTP
app.otp.expiry-minutes=5
app.otp.length=6

# File Upload
app.file.upload.profile-pictures=./uploads/profile-pictures/
app.file.upload.voice-messages=./uploads/voice-messages/
app.file.upload.images=./uploads/images/
```

## Error Handling

The application includes comprehensive error handling:
- Validation errors for request parameters
- Database constraint violations
- File operation errors
- Authentication and authorization errors
- Custom business logic exceptions

All errors return standardized JSON responses with appropriate HTTP status codes.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License. 