# Message Retention Feature Documentation

## Overview
The Message Retention feature automatically manages message storage by keeping only the last N messages (configurable, default 3) between any two users. This helps control database growth, improve performance, and manage storage costs while maintaining recent conversation history.

## Key Features

### ✅ **Configurable Retention Count**
- **Default**: 3 messages per conversation
- **Range**: 1 to 100 messages
- **Runtime Configuration**: Can be updated without restart

### ✅ **Automatic Cleanup**
- **Real-time**: Enforced after each message sent
- **Scheduled**: Hourly, daily, and weekly cleanup tasks
- **Media Cleanup**: Associated media files are automatically deleted

### ✅ **Smart Retention Logic**
- **Bidirectional**: Applies to both sender and receiver
- **Conversation-based**: Per user pair, not per individual user
- **Ordered**: Keeps the most recent messages

## Configuration

### Application Properties
```properties
# Message Retention Configuration
app.message.retention.count=3
app.message.retention.enabled=true
```

### Runtime Configuration
```bash
# Update retention settings
PUT /api/message-retention/config?retentionCount=5&enabled=true

# Get current configuration
GET /api/message-retention/config
```

## Implementation Details

### 1. MessageRetentionConfig
Configuration class that reads retention settings from `application.properties`:
```java
@Configuration
@ConfigurationProperties(prefix = "app.message.retention")
public class MessageRetentionConfig {
    private int count = 3;        // Default retention count
    private boolean enabled = true; // Whether retention is enabled
}
```

### 2. MessageRetentionService
Core service that handles retention logic:
```java
@Service
@Transactional
public class MessageRetentionService {
    
    // Enforces retention policy for a conversation
    public void enforceRetentionPolicy(Long userId1, Long userId2)
    
    // Gets messages with retention applied
    public List<Message> getMessagesWithRetention(Long userId, Long otherUserId)
    
    // Provides retention statistics
    public MessageRetentionStats getRetentionStats(Long userId1, Long userId2)
}
```

### 3. MessageRetentionScheduler
Automated cleanup tasks:
```java
@Service
public class MessageRetentionScheduler {
    
    @Scheduled(fixedRate = 3600000)        // Every hour
    public void scheduledMessageCleanup()
    
    @Scheduled(cron = "0 0 2 * * ?")      // Daily at 2 AM
    public void dailyMessageCleanup()
    
    @Scheduled(cron = "0 0 3 ? * SUN")    // Weekly on Sunday at 3 AM
    public void weeklyMessageCleanup()
}
```

## API Endpoints

### 1. Get Retention Configuration
```http
GET /api/message-retention/config
Authorization: Bearer {token}
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

### 2. Update Retention Configuration
```http
PUT /api/message-retention/config?retentionCount=5&enabled=true
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "message": "Retention configuration updated: enabled=true, count=5",
  "data": null
}
```

### 3. Get Retention Statistics
```http
GET /api/message-retention/stats/{otherUserId}
Authorization: Bearer {token}
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

### 4. Manual Conversation Cleanup
```http
POST /api/message-retention/cleanup/{otherUserId}
Authorization: Bearer {token}
```

### 5. Cleanup All Conversations
```http
POST /api/message-retention/cleanup-all
Authorization: Bearer {token}
```

## How It Works

### 1. Message Sending Flow
```
User sends message → Message saved → Retention policy enforced → Old messages deleted
```

### 2. Retention Logic
```
1. Count total messages between two users
2. If count > retention limit:
   - Sort messages by creation time (oldest first)
   - Delete oldest messages (count - retention limit)
   - Delete associated media files
3. Keep only the most recent N messages
```

### 3. Automatic Cleanup
- **Hourly**: Light cleanup for active conversations
- **Daily**: Comprehensive cleanup at 2 AM
- **Weekly**: Deep cleanup and optimization on Sundays

## Database Schema

### Enhanced Message Repository
```java
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    // Find messages with pagination for retention
    List<Message> findMessagesBetweenUsersWithLimit(Long userId, Long otherUserId, Pageable pageable);
    
    // Find message IDs ordered by creation time
    List<Long> findMessageIdsBetweenUsersOrderedAsc(Long userId, Long otherUserId);
    
    // Find distinct user pairs for cleanup
    List<Object[]> findDistinctUserPairs();
}
```

## Performance Considerations

### 1. Database Indexes
```sql
-- Recommended indexes for optimal performance
CREATE INDEX idx_messages_sender_receiver ON messages(sender_id, receiver_id);
CREATE INDEX idx_messages_created_at ON messages(created_at);
CREATE INDEX idx_messages_conversation ON messages(sender_id, receiver_id, created_at);
```

### 2. Batch Operations
- Messages are deleted in batches to avoid long transactions
- Media files are cleaned up efficiently
- Database operations are optimized for large datasets

### 3. Memory Management
- Only necessary message data is loaded into memory
- Pagination is used for large conversations
- Cleanup tasks run during low-traffic periods

## Security & Privacy

### 1. Authentication Required
- All retention endpoints require valid JWT token
- Users can only manage retention for their own conversations

### 2. Data Integrity
- Media files are properly cleaned up
- Database constraints maintain referential integrity
- Transaction rollback on errors

### 3. Audit Trail
- Retention operations are logged
- Configuration changes are tracked
- Cleanup statistics are maintained

## Monitoring & Analytics

### 1. Retention Statistics
- Total messages per conversation
- Messages deleted during cleanup
- Cleanup frequency and success rates

### 2. Performance Metrics
- Cleanup execution time
- Database operation performance
- Storage space savings

### 3. Error Tracking
- Failed cleanup operations
- Media file deletion errors
- Configuration update failures

## Usage Examples

### 1. Set Retention to 5 Messages
```bash
curl -X PUT "http://localhost:8080/api/message-retention/config?retentionCount=5&enabled=true" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 2. Check Retention Stats
```bash
curl -X GET "http://localhost:8080/api/message-retention/stats/123" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 3. Manual Cleanup
```bash
curl -X POST "http://localhost:8080/api/message-retention/cleanup/123" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Frontend Integration

### 1. Display Retention Status
```javascript
function RetentionStatus({ conversationId }) {
  const [stats, setStats] = useState(null);
  
  useEffect(() => {
    fetchRetentionStats(conversationId)
      .then(setStats);
  }, [conversationId]);
  
  if (!stats) return <div>Loading...</div>;
  
  return (
    <div className="retention-status">
      <span>Messages: {stats.totalMessages}/{stats.retentionCount}</span>
      {stats.needsCleanup && (
        <span className="cleanup-needed">Cleanup needed</span>
      )}
    </div>
  );
}
```

### 2. Retention Configuration UI
```javascript
function RetentionConfig() {
  const [config, setConfig] = useState({ enabled: true, count: 3 });
  
  const updateConfig = async (newConfig) => {
    await fetch('/api/message-retention/config', {
      method: 'PUT',
      headers: { 'Authorization': `Bearer ${token}` },
      body: JSON.stringify(newConfig)
    });
    setConfig(newConfig);
  };
  
  return (
    <div className="retention-config">
      <label>
        <input 
          type="checkbox" 
          checked={config.enabled}
          onChange={(e) => updateConfig({...config, enabled: e.target.checked})}
        />
        Enable Message Retention
      </label>
      <label>
        Retention Count:
        <input 
          type="number" 
          min="1" 
          max="100"
          value={config.count}
          onChange={(e) => updateConfig({...config, count: parseInt(e.target.value)})}
        />
      </label>
    </div>
  );
}
```

## Troubleshooting

### 1. Common Issues

#### Retention Not Working
- Check if `app.message.retention.enabled=true` in properties
- Verify scheduled tasks are running
- Check database indexes are properly created

#### Media Files Not Deleted
- Ensure FileUtil.deleteFile() is working
- Check file permissions
- Verify media paths are correct

#### Performance Issues
- Monitor database query performance
- Check if indexes are being used
- Consider adjusting cleanup frequency

### 2. Debug Mode
Enable debug logging:
```properties
logging.level.com.meetThePeople.service.MessageRetentionService=DEBUG
logging.level.com.meetThePeople.service.MessageRetentionScheduler=DEBUG
```

### 3. Manual Verification
```sql
-- Check message counts per conversation
SELECT 
    sender_id, 
    receiver_id, 
    COUNT(*) as message_count
FROM messages 
GROUP BY sender_id, receiver_id
HAVING message_count > 3;

-- Check for orphaned media files
SELECT media_path FROM messages WHERE media_path IS NOT NULL;
```

## Future Enhancements

### 1. Advanced Retention Policies
- **Time-based**: Keep messages for X days
- **Size-based**: Keep messages under X MB
- **Importance-based**: Keep important messages longer

### 2. Message Archiving
- Archive old messages instead of deletion
- Compress archived messages
- Restore archived conversations

### 3. User Preferences
- Per-user retention settings
- Conversation-specific retention rules
- Retention override for important conversations

### 4. Analytics Dashboard
- Real-time retention statistics
- Storage savings visualization
- Cleanup performance metrics

## Best Practices

### 1. Configuration
- Start with conservative retention counts (3-5)
- Monitor storage usage and adjust accordingly
- Test retention policies in development first

### 2. Monitoring
- Set up alerts for cleanup failures
- Monitor database performance during cleanup
- Track storage space savings

### 3. Backup Strategy
- Ensure important messages are backed up
- Consider message export functionality
- Implement retention policy documentation

### 4. User Communication
- Inform users about retention policies
- Provide message export options
- Allow users to save important messages

This message retention feature provides a robust, configurable solution for managing message storage while maintaining conversation history and system performance. 