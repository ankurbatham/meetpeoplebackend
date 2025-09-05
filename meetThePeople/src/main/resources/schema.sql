-- =====================================================
-- Meet The People - Database Schema
-- =====================================================

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mobile VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    gender ENUM('MALE', 'FEMALE', 'OTHER') NOT NULL,
    dob DATE NOT NULL,
    address VARCHAR(200),
    pincode VARCHAR(6),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    hobbies VARCHAR(500),
    about_you VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_mobile (mobile),
    INDEX idx_gender (gender),
    INDEX idx_pincode (pincode),
    INDEX idx_location (latitude, longitude),
    INDEX idx_created_at (created_at)
);

-- Create user_profile_pics table for multiple profile pictures per user
CREATE TABLE IF NOT EXISTS user_profile_pics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    image_path VARCHAR(500) NOT NULL,
    image_name VARCHAR(255),
    image_type VARCHAR(100),
    file_size BIGINT,
    is_primary BOOLEAN DEFAULT FALSE,
    display_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_user_primary (user_id, is_primary),
    INDEX idx_user_order (user_id, display_order)
);

-- Add unique constraint to ensure only one primary picture per user
ALTER TABLE user_profile_pics 
ADD CONSTRAINT uk_user_primary UNIQUE (user_id, is_primary);

-- Create otp_verification table
CREATE TABLE IF NOT EXISTS otp_verification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mobile VARCHAR(10) NOT NULL,
    otp VARCHAR(6) NOT NULL,
    expiry_time TIMESTAMP NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_mobile (mobile),
    INDEX idx_expiry_time (expiry_time),
    INDEX idx_is_used (is_used)
);

-- Create user_activity table for tracking online status
CREATE TABLE IF NOT EXISTS user_activity (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    active_source ENUM('APP', 'WEBSITE') NOT NULL,
    last_active_time TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_last_active_time (last_active_time),
    INDEX idx_active_source (active_source)
);

-- Create user_communication_mapping table
CREATE TABLE IF NOT EXISTS user_communication_mapping (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user1_id BIGINT NOT NULL,
    user2_id BIGINT NOT NULL,
    can_communicate BOOLEAN DEFAULT FALSE,
    established_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user1_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (user2_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_pair (user1_id, user2_id),
    INDEX idx_user1 (user1_id),
    INDEX idx_user2 (user2_id),
    INDEX idx_can_communicate (can_communicate)
);

-- Create user_block_mapping table
CREATE TABLE IF NOT EXISTS user_block_mapping (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    blocker_id BIGINT NOT NULL,
    blocked_id BIGINT NOT NULL,
    blocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reason VARCHAR(500),
    
    FOREIGN KEY (blocker_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (blocked_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_block_pair (blocker_id, blocked_id),
    INDEX idx_blocker (blocker_id),
    INDEX idx_blocked (blocked_id),
    INDEX idx_blocked_at (blocked_at)
);

-- Create messages table
CREATE TABLE IF NOT EXISTS messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    message_type ENUM('TEXT', 'IMAGE', 'VOICE') NOT NULL,
    text_content TEXT,
    media_path VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_sender (sender_id),
    INDEX idx_receiver (receiver_id),
    INDEX idx_conversation (sender_id, receiver_id),
    INDEX idx_created_at (created_at),
    INDEX idx_message_type (message_type)
);

-- Create composite index for efficient conversation queries
CREATE INDEX idx_messages_conversation_created 
ON messages(sender_id, receiver_id, created_at);

-- Create index for retention policy queries
CREATE INDEX idx_messages_retention 
ON messages(sender_id, receiver_id, created_at DESC);

-- =====================================================
-- Additional Indexes for Performance
-- =====================================================

-- Index for user search by gender and pincode
CREATE INDEX idx_users_gender_pincode ON users(gender, pincode);

-- Index for user search by gender only
CREATE INDEX idx_users_gender ON users(gender);

-- Index for distance-based searches
CREATE INDEX idx_users_location_search ON users(latitude, longitude, gender);

-- Index for age-based searches (using dob)
CREATE INDEX idx_users_dob ON users(dob);

-- Index for message retention cleanup
CREATE INDEX idx_messages_cleanup ON messages(sender_id, receiver_id, created_at ASC);

-- =====================================================
-- Sample Data (Optional - for testing)
-- =====================================================

-- Insert sample users (uncomment if needed for testing)
/*
INSERT INTO users (mobile, name, gender, dob, address, pincode, latitude, longitude, hobbies, about_you) VALUES
('9876543210', 'John Doe', 'MALE', '1990-01-01', '123 Main St', '123456', 12.9716, 77.5946, 'Reading, Traveling', 'I love meeting new people'),
('9876543211', 'Jane Smith', 'FEMALE', '1992-05-15', '456 Oak Ave', '123456', 12.9717, 77.5947, 'Cooking, Painting', 'Creative soul looking for connections'),
('9876543212', 'Mike Johnson', 'MALE', '1988-12-10', '789 Pine Rd', '654321', 12.9718, 77.5948, 'Sports, Music', 'Athletic and musical person');
*/

-- =====================================================
-- Database Constraints and Validations
-- =====================================================

-- Ensure mobile number is exactly 10 digits
ALTER TABLE users 
ADD CONSTRAINT chk_mobile_length CHECK (LENGTH(mobile) = 10);

-- Ensure pincode is exactly 6 digits
ALTER TABLE users 
ADD CONSTRAINT chk_pincode_length CHECK (LENGTH(pincode) = 6);

-- Ensure latitude is between -90 and 90
ALTER TABLE users 
ADD CONSTRAINT chk_latitude_range CHECK (latitude >= -90 AND latitude <= 90);

-- Ensure longitude is between -180 and 180
ALTER TABLE users 
ADD CONSTRAINT chk_longitude_range CHECK (longitude >= -180 AND longitude <= 180);

-- Ensure OTP is exactly 6 digits
ALTER TABLE otp_verification 
ADD CONSTRAINT chk_otp_length CHECK (LENGTH(otp) = 6);

-- =====================================================
-- Views for Common Queries
-- =====================================================

-- View for users with online status
CREATE OR REPLACE VIEW user_online_status AS
SELECT 
    u.id,
    u.name,
    u.gender,
    u.dob,
    u.address,
    u.pincode,
    u.latitude,
    u.longitude,
    u.hobbies,
    u.about_you,
    u.created_at,
    u.updated_at,
    CASE 
        WHEN ua.last_active_time IS NOT NULL 
        AND ua.last_active_time > DATE_SUB(NOW(), INTERVAL 60 SECOND) 
        THEN 'online' 
        ELSE 'offline' 
    END AS online_status,
    ua.last_active_time,
    ua.active_source
FROM users u
LEFT JOIN user_activity ua ON u.id = ua.user_id;

-- View for conversation statistics
CREATE OR REPLACE VIEW conversation_stats AS
SELECT 
    sender_id,
    receiver_id,
    COUNT(*) as message_count,
    MAX(created_at) as last_message_time,
    MIN(created_at) as first_message_time
FROM messages
GROUP BY sender_id, receiver_id;

-- =====================================================
-- Stored Procedures (Optional - for advanced operations)
-- =====================================================

DELIMITER //

-- Procedure to clean up old messages based on retention policy
CREATE PROCEDURE IF NOT EXISTS cleanup_old_messages(
    IN retention_count INT,
    IN user1_id BIGINT,
    IN user2_id BIGINT
)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE msg_id BIGINT;
    DECLARE msg_count INT DEFAULT 0;
    
    -- Get total message count between users
    SELECT COUNT(*) INTO msg_count
    FROM messages 
    WHERE (sender_id = user1_id AND receiver_id = user2_id)
       OR (sender_id = user2_id AND receiver_id = user1_id);
    
    -- If we have more messages than retention limit, delete oldest ones
    IF msg_count > retention_count THEN
        -- Create temporary table with message IDs to delete
        CREATE TEMPORARY TABLE temp_messages_to_delete AS
        SELECT id FROM messages 
        WHERE (sender_id = user1_id AND receiver_id = user2_id)
           OR (sender_id = user2_id AND receiver_id = user1_id)
        ORDER BY created_at ASC
        LIMIT (msg_count - retention_count);
        
        -- Delete messages
        DELETE m FROM messages m
        INNER JOIN temp_messages_to_delete t ON m.id = t.id;
        
        -- Clean up temporary table
        DROP TEMPORARY TABLE temp_messages_to_delete;
    END IF;
END //

DELIMITER ;

-- =====================================================
-- Database Maintenance
-- =====================================================

-- Create event to clean up expired OTPs daily
CREATE EVENT IF NOT EXISTS cleanup_expired_otps
ON SCHEDULE EVERY 1 DAY
STARTS CURRENT_TIMESTAMP
DO
    DELETE FROM otp_verification 
    WHERE expiry_time < NOW() OR is_used = TRUE;

-- Create event to update user activity status
CREATE EVENT IF NOT EXISTS update_user_activity_status
ON SCHEDULE EVERY 1 HOUR
STARTS CURRENT_TIMESTAMP
DO
    UPDATE user_activity 
    SET last_active_time = last_active_time 
    WHERE last_active_time < DATE_SUB(NOW(), INTERVAL 60 SECOND);

-- =====================================================
-- Schema Version and Metadata
-- =====================================================

-- Create schema version table
CREATE TABLE IF NOT EXISTS schema_version (
    id INT AUTO_INCREMENT PRIMARY KEY,
    version VARCHAR(20) NOT NULL,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description TEXT
);

-- Insert current schema version
INSERT INTO schema_version (version, description) VALUES 
('1.0.0', 'Initial schema creation with all entities and indexes');

-- =====================================================
-- End of Schema
-- ===================================================== 