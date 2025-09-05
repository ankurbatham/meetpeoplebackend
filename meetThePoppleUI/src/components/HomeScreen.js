import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { activityAPI } from '../services/api';

// Global flag to prevent multiple activity capture intervals
let globalActivityCaptureActive = false;

const HomeScreen = () => {
  const [conversations, setConversations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const activityIntervalRef = useRef(null);
  const isInitializedRef = useRef(false);

  useEffect(() => {
    // Prevent duplicate initialization in StrictMode
    if (isInitializedRef.current) {
      return;
    }
    
    isInitializedRef.current = true;
    fetchConversations();
    startActivityCapture();

    // Cleanup function to clear interval when component unmounts
    return () => {
      stopActivityCapture();
      isInitializedRef.current = false;
    };
  }, []);

  const fetchConversations = async () => {
    try {
      setLoading(true);
      // For demo purposes, we'll create some mock conversations
      // In real app, you'd fetch from the API
      const mockConversations = [
        {
          id: 1,
          userId: 2,
          userName: 'Jane Smith',
          userMobile: '9876543211',
          userGender: 'FEMALE',
          userAddress: '456 Oak Ave, Bangalore',
          userPincode: '560001',
          userLatitude: 12.9717,
          userLongitude: 77.5947,
          userHobbies: 'Cooking, Painting, Reading',
          userAboutYou: 'Creative soul looking for meaningful connections',
          userProfilePics: [
            {
              id: 1,
              imagePath: '/uploads/profile-pics/jane.jpg',
              imageName: 'jane.jpg',
              imageType: 'image/jpeg',
              fileSize: 1024000,
              isPrimary: true,
              displayOrder: 0,
              createdAt: '2024-01-15T09:00:00',
              updatedAt: '2024-01-15T10:30:00'
            }
          ],
          userCreatedAt: '2024-01-15T09:00:00',
          userUpdatedAt: '2024-01-15T10:30:00',
          onlineStatus: 'online',
          lastActiveTime: '2024-01-15T10:45:00',
          lastActiveSource: 'APP',
          communicationId: 1,
          canCommunicate: true,
          communicationEstablishedAt: '2024-01-15T10:00:00',
          communicationUpdatedAt: '2024-01-15T10:00:00',
          lastMessageId: 5,
          lastMessageType: 'TEXT',
          lastMessageContent: 'Hello! How are you doing?',
          lastMessageMediaPath: null,
          lastMessageTime: '2024-01-15T10:30:00',
          isLastMessageFromMe: true
        },
        {
          id: 2,
          userId: 3,
          userName: 'Mike Johnson',
          userMobile: '9876543212',
          userGender: 'MALE',
          userAddress: '789 Pine Rd, Bangalore',
          userPincode: '560001',
          userLatitude: 12.9718,
          userLongitude: 77.5948,
          userHobbies: 'Sports, Music',
          userAboutYou: 'Athletic and musical person',
          userProfilePics: [
            {
              id: 2,
              imagePath: '/uploads/profile-pics/mike.jpg',
              imageName: 'mike.jpg',
              imageType: 'image/jpeg',
              fileSize: 1024000,
              isPrimary: true,
              displayOrder: 0,
              createdAt: '2024-01-15T08:00:00',
              updatedAt: '2024-01-15T09:30:00'
            }
          ],
          userCreatedAt: '2024-01-15T08:00:00',
          userUpdatedAt: '2024-01-15T09:30:00',
          onlineStatus: 'offline',
          lastActiveTime: '2024-01-15T09:15:00',
          lastActiveSource: 'APP',
          communicationId: 2,
          canCommunicate: true,
          communicationEstablishedAt: '2024-01-15T09:00:00',
          communicationUpdatedAt: '2024-01-15T09:00:00',
          lastMessageId: 3,
          lastMessageType: 'TEXT',
          lastMessageContent: 'Great to meet you!',
          lastMessageMediaPath: null,
          lastMessageTime: '2024-01-15T09:10:00',
          isLastMessageFromMe: false
        }
      ];

      setConversations(mockConversations);
    } catch (error) {
      console.error('Error fetching conversations:', error);
      setError('Failed to load conversations');
    } finally {
      setLoading(false);
    }
  };

  const startActivityCapture = () => {
    // Prevent multiple activity capture intervals across the app
    if (globalActivityCaptureActive) {
      console.log('Activity capture already active, skipping...');
      return;
    }

    console.log('Starting activity capture...', new Date().toISOString());

    // Clear any existing interval first
    if (activityIntervalRef.current) {
      clearInterval(activityIntervalRef.current);
      activityIntervalRef.current = null;
    }

    // Set global flag
    globalActivityCaptureActive = true;

    // Capture user activity every 30 seconds
    activityIntervalRef.current = setInterval(async () => {
      try {
        console.log('Capturing user activity...', new Date().toISOString()); // Debug log with timestamp
        await activityAPI.captureActivity('APP');
      } catch (error) {
        console.error('Error capturing activity:', error);
      }
    }, 30000);

    // Initial activity capture - only if not already captured recently
    const lastCapture = sessionStorage.getItem('lastActivityCapture');
    const now = Date.now();
    const timeSinceLastCapture = lastCapture ? now - parseInt(lastCapture) : 60000; // Default to 1 minute

    if (timeSinceLastCapture > 5000) { // Only capture if more than 5 seconds have passed
      console.log('Initial activity capture...', new Date().toISOString());
      sessionStorage.setItem('lastActivityCapture', now.toString());
      activityAPI.captureActivity('APP').catch(error => {
        console.error('Error with initial activity capture:', error);
      });
    } else {
      console.log('Skipping initial activity capture - too recent');
    }
  };

  const stopActivityCapture = () => {
    if (activityIntervalRef.current) {
      clearInterval(activityIntervalRef.current);
      activityIntervalRef.current = null;
      globalActivityCaptureActive = false;
      console.log('Activity capture stopped...', new Date().toISOString());
    }
  };

  const formatTime = (timestamp) => {
    const date = new Date(timestamp);
    const now = new Date();
    const diffInHours = (now - date) / (1000 * 60 * 60);

    if (diffInHours < 1) {
      const diffInMinutes = Math.floor((now - date) / (1000 * 60));
      return diffInMinutes < 1 ? 'Just now' : `${diffInMinutes}m ago`;
    } else if (diffInHours < 24) {
      return `${Math.floor(diffInHours)}h ago`;
    } else {
      return date.toLocaleDateString();
    }
  };

  const getLastMessagePreview = (conversation) => {
    if (!conversation.lastMessageContent) return 'No messages yet';
    
    const prefix = conversation.isLastMessageFromMe ? 'You: ' : '';
    const content = conversation.lastMessageContent;
    
    if (conversation.lastMessageType === 'IMAGE') {
      return `${prefix}📷 Image`;
    } else if (conversation.lastMessageType === 'VOICE') {
      return `${prefix}🎤 Voice message`;
    } else {
      return `${prefix}${content.length > 30 ? content.substring(0, 30) + '...' : content}`;
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading conversations...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-4xl mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 bg-primary-600 rounded-full flex items-center justify-center">
              <span className="text-white font-bold text-lg">
                {user?.name?.charAt(0) || 'U'}
              </span>
            </div>
            <div>
              <h1 className="text-xl font-semibold text-gray-900">
                Meet The People
              </h1>
              <p className="text-sm text-gray-500">
                Welcome back, {user?.name || 'User'}
              </p>
            </div>
          </div>
          
          <div className="flex items-center space-x-3">
            <button
              onClick={() => navigate('/profile')}
              className="p-2 text-gray-600 hover:text-primary-600 hover:bg-primary-50 rounded-lg transition-colors"
            >
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
            </button>
            
            <button
              onClick={logout}
              className="p-2 text-gray-600 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
            >
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
              </svg>
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-4xl mx-auto px-4 py-6">
        {error && (
          <div className="mb-6 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
            {error}
          </div>
        )}

        {/* Conversations List */}
        <div className="space-y-4">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">
            Conversations ({conversations.length})
          </h2>
          
          {conversations.length === 0 ? (
            <div className="text-center py-12">
              <div className="w-16 h-16 bg-gray-200 rounded-full flex items-center justify-center mx-auto mb-4">
                <svg className="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                </svg>
              </div>
              <h3 className="text-lg font-medium text-gray-900 mb-2">No conversations yet</h3>
              <p className="text-gray-500">Start connecting with people around you!</p>
            </div>
          ) : (
            conversations.map((conversation) => (
              <div
                key={conversation.id}
                className="card hover:shadow-md transition-shadow cursor-pointer"
                onClick={() => {
                  // Navigate to conversation (to be implemented)
                  console.log('Navigate to conversation with:', conversation.userName);
                }}
              >
                <div className="flex items-center space-x-4">
                  {/* Profile Picture */}
                  <div className="relative">
                    <div className="w-12 h-12 bg-gray-200 rounded-full flex items-center justify-center overflow-hidden">
                      {conversation.userProfilePics?.[0] ? (
                        <img
                          src={conversation.userProfilePics[0].imagePath}
                          alt={conversation.userName}
                          className="w-full h-full object-cover"
                          onError={(e) => {
                            e.target.style.display = 'none';
                            e.target.nextSibling.style.display = 'flex';
                          }}
                        />
                      ) : null}
                      <div className="w-full h-full bg-primary-100 flex items-center justify-center text-primary-600 font-semibold">
                        {conversation.userName?.charAt(0) || 'U'}
                      </div>
                    </div>
                    
                    {/* Online Status Indicator */}
                    <div className={`absolute -bottom-1 -right-1 w-4 h-4 rounded-full border-2 border-white ${
                      conversation.onlineStatus === 'online' ? 'bg-green-500' : 'bg-gray-400'
                    }`}></div>
                  </div>

                  {/* Conversation Details */}
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between mb-1">
                      <h3 className="text-sm font-semibold text-gray-900 truncate">
                        {conversation.userName}
                      </h3>
                      <span className="text-xs text-gray-500">
                        {formatTime(conversation.lastMessageTime)}
                      </span>
                    </div>
                    
                    <p className="text-sm text-gray-600 truncate">
                      {getLastMessagePreview(conversation)}
                    </p>
                    
                    <div className="flex items-center space-x-2 mt-1">
                      <span className="text-xs text-gray-500">
                        {conversation.userAddress}
                      </span>
                      {conversation.userHobbies && (
                        <span className="text-xs text-gray-400">
                          • {conversation.userHobbies.split(',')[0]}
                        </span>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      </main>

      {/* Floating Action Button - Search */}
      <button
        onClick={() => {
          // Navigate to search (to be implemented)
          console.log('Navigate to search');
        }}
        className="floating-action-btn"
        aria-label="Search people"
      >
        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
        </svg>
      </button>
    </div>
  );
};

export default HomeScreen; 