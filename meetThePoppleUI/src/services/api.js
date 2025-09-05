import axios from 'axios';

// Create axios instance with base configuration
export const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle common errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expired or invalid
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// API endpoints
export const authAPI = {
  // Generate OTP
  generateOTP: (mobile) => 
    api.post('/auth/generate-otp', { mobile }),

  // Login with OTP
  login: (mobile, otp) => 
    api.post('/auth/login', { mobile, otp }),
};

export const userAPI = {
  // Get current user profile
  getProfile: () => 
    api.get('/users/profile'),

  // Update user profile
  updateProfile: (profileData) => 
    api.put('/users/profile', profileData),

  // Get any user's profile
  getUserProfile: (userId) => 
    api.get(`/users/${userId}/profile`),

  // Get user communication details
  getUserCommunication: (userId) => 
    api.get(`/users/${userId}/communication`),

  // Search users
  searchUsers: (searchParams) => 
    api.post('/users/search', searchParams),
};

export const profilePicsAPI = {
  // Upload profile picture
  uploadProfilePic: (formData) => 
    api.post('/profile-pics/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }),

  // Get user profile pictures
  getProfilePics: (userId) => 
    api.get(`/profile-pics/${userId}`),

  // Set primary profile picture
  setPrimaryPic: (picId) => 
    api.put(`/profile-pics/${picId}/primary`),

  // Delete profile picture
  deleteProfilePic: (picId) => 
    api.delete(`/profile-pics/${picId}`),

  // Reorder profile pictures
  reorderPics: (picIds) => 
    api.put('/profile-pics/reorder', picIds),
};

export const messagesAPI = {
  // Send text message
  sendMessage: (messageData) => 
    api.post('/messages/send', messageData),

  // Send media message
  sendMediaMessage: (formData) => 
    api.post('/messages/send-media', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }),

  // Get conversation
  getConversation: (otherUserId) => 
    api.get(`/messages/conversation/${otherUserId}`),

  // Delete message
  deleteMessage: (messageId) => 
    api.delete(`/messages/${messageId}`),
};

export const activityAPI = {
  // Capture user activity
  captureActivity: (source) => 
    api.post(`/activity/capture?source=${source}`),

  // Get activity status
  getActivityStatus: () => 
    api.get('/activity/status'),
};

export const blockAPI = {
  // Block user
  blockUser: (userId) => 
    api.post(`/block/${userId}`),

  // Unblock user
  unblockUser: (userId) => 
    api.delete(`/block/${userId}`),

  // Get blocked users list
  getBlockedUsers: () => 
    api.get('/block/list'),

  // Check if user is blocked
  checkBlockStatus: (userId) => 
    api.get(`/block/check/${userId}`),
};

export const messageRetentionAPI = {
  // Get retention configuration
  getRetentionConfig: () => 
    api.get('/message-retention/config'),

  // Update retention configuration
  updateRetentionConfig: (params) => 
    api.put('/message-retention/config', null, { params }),

  // Get retention statistics
  getRetentionStats: (otherUserId) => 
    api.get(`/message-retention/stats/${otherUserId}`),

  // Manual conversation cleanup
  cleanupConversation: (otherUserId) => 
    api.post(`/message-retention/cleanup/${otherUserId}`),
};

export const healthAPI = {
  // Health check
  checkHealth: () => 
    api.get('/health'),
}; 