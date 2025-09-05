# Online Status Feature Documentation

## Overview
The online status feature allows users to see whether other users are currently online or offline based on their recent activity. A user is considered "online" if they have been active within the last 60 seconds.

## Implementation Details

### 1. UserActivity Entity
- Tracks user activity from both APP and WEBSITE sources
- Records `lastActiveTime` timestamp
- Stores `activeSource` (APP or WEBSITE)

### 2. Online Status Logic
- **Online**: User has been active within the last 60 seconds
- **Offline**: User has not been active for more than 60 seconds
- **No Activity**: User has no recorded activity (treated as offline)

### 3. API Endpoints

#### Search Users with Online Status
```
POST /users/search
```
**Response**: List of users with online status information
```json
{
  "success": true,
  "message": "Users found successfully",
  "data": [
    {
      "id": 1,
      "name": "John Doe",
      "gender": "MALE",
      "onlineStatus": "online",
      "lastActiveTime": "2024-01-15T10:30:00",
      "lastActiveSource": "APP",
      "profilePics": [...]
    }
  ]
}
```

#### Get User Profile with Online Status
```
GET /users/profile
GET /users/{userId}/profile
```
**Response**: User profile with online status information
```json
{
  "success": true,
  "message": "Profile retrieved successfully",
  "data": {
    "id": 1,
    "name": "John Doe",
    "gender": "MALE",
    "onlineStatus": "online",
    "lastActiveTime": "2024-01-15T10:30:00",
    "lastActiveSource": "APP",
    "profilePics": [...],
    "mobile": "1234567890",
    "dob": "1990-01-01",
    "address": "123 Main St",
    "pincode": "123456",
    "latitude": 12.9716,
    "longitude": 77.5946,
    "hobbies": "Reading, Traveling",
    "aboutYou": "I love meeting new people",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
}
```

### 4. Activity Capture
Users can capture their activity using:
```
POST /activity/capture?source=APP
POST /activity/capture?source=WEBSITE
```

### 5. Online Status Calculation
The system uses `OnlineStatusUtil` class to determine online status:

```java
// Check if user is online (within 60 seconds)
boolean isOnline = OnlineStatusUtil.isOnline(lastActiveTime);

// Get status as string
String status = OnlineStatusUtil.getOnlineStatus(lastActiveTime);

// Get custom threshold
String status = OnlineStatusUtil.getOnlineStatus(lastActiveTime, 120); // 2 minutes
```

## Configuration

### Online Status Threshold
- **Default**: 60 seconds
- **Configurable**: Can be modified in `OnlineStatusUtil.ONLINE_THRESHOLD_SECONDS`

### Activity Sources
- **APP**: Mobile application activity
- **WEBSITE**: Web browser activity

## Usage Examples

### 1. Search for Online Users
```bash
curl -X POST http://localhost:8080/users/search \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "gender": "FEMALE",
    "maxDistanceKm": 10.0
  }'
```

### 2. Get User Profile with Online Status
```bash
# Get current user's profile
curl -X GET http://localhost:8080/users/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Get specific user's profile
curl -X GET http://localhost:8080/users/123/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 3. Capture User Activity
```bash
curl -X POST http://localhost:8080/activity/capture?source=APP \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Frontend Integration

### Display Online Status in User Cards
```javascript
// Example React component for search results
function UserCard({ user }) {
  const getStatusColor = (status) => {
    return status === 'online' ? 'green' : 'gray';
  };
  
  return (
    <div className="user-card">
      <h3>{user.name}</h3>
      <span 
        className="status-indicator"
        style={{ color: getStatusColor(user.onlineStatus) }}
      >
        {user.onlineStatus}
      </span>
      {user.onlineStatus === 'online' && (
        <span className="last-seen">
          Last active: {formatTime(user.lastActiveTime)}
        </span>
      )}
      <div className="profile-pics">
        {user.profilePics?.map(pic => (
          <img key={pic.id} src={pic.imagePath} alt="Profile" />
        ))}
      </div>
    </div>
  );
}

// Example React component for user profile
function UserProfile({ user }) {
  return (
    <div className="user-profile">
      <h2>{user.name}</h2>
      <div className="online-status">
        <span className={`status-dot ${user.onlineStatus}`}></span>
        <span>{user.onlineStatus}</span>
        {user.lastActiveSource && (
          <span className="source">via {user.lastActiveSource}</span>
        )}
      </div>
      <div className="profile-info">
        <p>Age: {calculateAge(user.dob)}</p>
        <p>Location: {user.address}</p>
        <p>Hobbies: {user.hobbies}</p>
        <p>About: {user.aboutYou}</p>
      </div>
    </div>
  );
}
```

### Real-time Updates
For real-time online status updates, consider:
1. **WebSocket connections** for live status changes
2. **Periodic polling** every 30-60 seconds
3. **Push notifications** when users come online

## Performance Considerations

### Database Indexes
- `user_id` index on `user_activity` table
- Composite index on `(user_id, last_active_time)`

### Caching
- Cache online status for frequently accessed users
- Use Redis for distributed caching in production

### Query Optimization
- Batch activity updates
- Use database views for complex status queries

## Security

### Authentication
- All endpoints require valid JWT token
- Users can only see online status of users they have permission to view

### Privacy
- Online status is visible to all users (configurable)
- Consider adding privacy settings for online status visibility

## Monitoring and Analytics

### Metrics to Track
- Number of online users
- Activity patterns by time of day
- Most active users
- Platform usage (APP vs WEBSITE)

### Logging
- Log significant status changes
- Track activity capture frequency
- Monitor system performance

## Future Enhancements

### 1. Custom Status Messages
- "Busy", "Away", "Do Not Disturb"
- Custom status text

### 2. Status History
- Track status changes over time
- Activity timeline

### 3. Status Notifications
- Notify when specific users come online
- Status change alerts

### 4. Advanced Filtering
- Search by online status
- Filter by last active time
- Activity source filtering 