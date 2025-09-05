import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { userAPI, profilePicsAPI } from '../services/api';

const UserProfileScreen = () => {
  const [profile, setProfile] = useState(null);
  const [profilePics, setProfilePics] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [currentPicIndex, setCurrentPicIndex] = useState(0);
  const [showEditModal, setShowEditModal] = useState(false);
  const [downloadingPics, setDownloadingPics] = useState(new Set());
  const [editForm, setEditForm] = useState({});
  const [editLoading, setEditLoading] = useState(false);
  const [uploadingPic, setUploadingPic] = useState(false);
  const [selectedFile, setSelectedFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState('');
  const navigate = useNavigate();
  const { user, updateUser } = useAuth();

  // Simple function to fetch profile and profile pictures
  const fetchProfileData = async () => {
    try {
      setLoading(true);
      setError('');

      // Fetch profile
      const profileResponse = await userAPI.getProfile();
      if (profileResponse.data.success) {
        const profileData = profileResponse.data.data;
        setProfile(profileData);
        updateUser(profileData);
        
        // Initialize edit form
        setEditForm({
          name: profileData.name || '',
          gender: profileData.gender || '',
          dob: profileData.dob ? profileData.dob.split('T')[0] : '',
          address: profileData.address || '',
          pincode: profileData.pincode || '',
          latitude: profileData.latitude || '',
          longitude: profileData.longitude || '',
          hobbies: profileData.hobbies || '',
          aboutYou: profileData.aboutYou || ''
        });
      }

      // Fetch profile pictures
      const picsResponse = await profilePicsAPI.getProfilePics(user.id);
      if (picsResponse.data.success) {
        setProfilePics(picsResponse.data.data || []);
      }

    } catch (error) {
      console.error('Error fetching profile data:', error);
      setError('Failed to load profile data');
    } finally {
      setLoading(false);
    }
  };

  // Load profile data when component mounts
  useEffect(() => {
    if (user?.id) {
      fetchProfileData();
    }
  }, [user?.id]);

  // Handle form input changes
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setEditForm(prev => ({
      ...prev,
      [name]: value
    }));
  };

  // Handle file selection for profile picture upload
  const handleFileSelect = (e) => {
    const file = e.target.files[0];
    if (file) {
      // Validate file type
      if (!file.type.startsWith('image/')) {
        alert('Please select an image file');
        return;
      }
      
      // Validate file size (10MB limit)
      if (file.size > 10 * 1024 * 1024) {
        alert('File size must be less than 10MB');
        return;
      }

      setSelectedFile(file);
      
      // Create preview URL
      const url = URL.createObjectURL(file);
      setPreviewUrl(url);
    }
  };

  // Upload profile picture
  const handleUploadPic = async () => {
    if (!selectedFile) return;

    try {
      setUploadingPic(true);
      
      const formData = new FormData();
      formData.append('file', selectedFile);
      formData.append('isPrimary', profilePics.length === 0);

      const response = await profilePicsAPI.uploadProfilePic(formData);
      
      if (response.data.success) {
        const newPic = response.data.data;
        setProfilePics(prev => [...prev, newPic]);
        
        // Clear file selection
        setSelectedFile(null);
        setPreviewUrl('');
        const fileInput = document.getElementById('fileInput');
        if (fileInput) fileInput.value = '';
        
        alert('Profile picture uploaded successfully!');
      } else {
        alert('Failed to upload profile picture');
      }
    } catch (error) {
      console.error('Error uploading profile picture:', error);
      alert('Failed to upload profile picture: ' + (error.response?.data?.message || error.message));
    } finally {
      setUploadingPic(false);
    }
  };

  // Save profile changes
  const handleSaveProfile = async () => {
    try {
      setEditLoading(true);
      
      // Validate required fields
      if (!editForm.name || !editForm.gender || !editForm.dob) {
        alert('Please fill in all required fields (Name, Gender, Date of Birth)');
        return;
      }

      const response = await userAPI.updateProfile(editForm);
      
      if (response.data.success) {
        const updatedProfile = response.data.data;
        setProfile(updatedProfile);
        updateUser(updatedProfile);
        setShowEditModal(false);
        alert('Profile updated successfully!');
      } else {
        alert('Failed to update profile: ' + (response.data.message || 'Unknown error'));
      }
    } catch (error) {
      console.error('Error updating profile:', error);
      alert('Failed to update profile: ' + (error.response?.data?.message || error.message));
    } finally {
      setEditLoading(false);
    }
  };

  // Download profile picture
  const downloadProfilePic = async (picId, imagePath, imageName) => {
    if (downloadingPics.has(picId)) return;

    try {
      setDownloadingPics(prev => new Set(prev).add(picId));
      
      const link = document.createElement('a');
      link.href = imagePath;
      link.download = imageName || `profile-pic-${picId}.jpg`;
      link.target = '_blank';
      
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      
    } catch (error) {
      console.error('Error downloading profile picture:', error);
    } finally {
      setDownloadingPics(prev => {
        const newSet = new Set(prev);
        newSet.delete(picId);
        return newSet;
      });
    }
  };

  // Profile picture actions
  const handleDeletePicture = async (picId) => {
    if (!window.confirm('Are you sure you want to delete this picture?')) {
      return;
    }

    try {
      await profilePicsAPI.deleteProfilePic(picId);
      setProfilePics(prev => prev.filter(pic => pic.id !== picId));
      
      // Adjust current index if needed
      if (currentPicIndex >= profilePics.length - 1) {
        setCurrentPicIndex(Math.max(0, profilePics.length - 2));
      }
      
    } catch (error) {
      console.error('Error deleting picture:', error);
      alert('Failed to delete picture');
    }
  };

  const handleSetPrimary = async (picId) => {
    try {
      await profilePicsAPI.setPrimaryPic(picId);
      setProfilePics(prev => 
        prev.map(pic => ({
          ...pic,
          isPrimary: pic.id === picId
        }))
      );
    } catch (error) {
      console.error('Error setting primary picture:', error);
      alert('Failed to set primary picture');
    }
  };

  // Navigation functions
  const nextPicture = () => {
    setCurrentPicIndex(prev => 
      prev === profilePics.length - 1 ? 0 : prev + 1
    );
  };

  const prevPicture = () => {
    setCurrentPicIndex(prev => 
      prev === 0 ? profilePics.length - 1 : prev - 1
    );
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'Not set';
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading profile...</p>
        </div>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg className="w-8 h-8 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
            </svg>
          </div>
          <h3 className="text-lg font-medium text-gray-900 mb-2">Profile Not Loaded</h3>
          <p className="text-gray-500 mb-4">The profile data could not be loaded.</p>
          <button
            onClick={fetchProfileData}
            className="btn-primary"
          >
            Retry
          </button>
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
            <button
              onClick={() => navigate('/')}
              className="p-2 text-gray-600 hover:text-primary-600 hover:bg-primary-50 rounded-lg transition-colors"
            >
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
              </svg>
            </button>
            <h1 className="text-xl font-semibold text-gray-900">Profile</h1>
          </div>
          
          <button
            onClick={() => setShowEditModal(true)}
            className="btn-primary"
          >
            Edit Profile
          </button>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-4xl mx-auto px-4 py-6">
        {error && (
          <div className="mb-6 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
            {error}
          </div>
        )}

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Profile Pictures Section */}
          <div className="space-y-6">
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-semibold text-gray-900">Profile Pictures</h2>
              <button
                onClick={() => document.getElementById('fileInput')?.click()}
                className="btn-primary text-sm"
                disabled={uploadingPic}
              >
                {uploadingPic ? 'Uploading...' : 'Add Picture'}
              </button>
            </div>
            
            {/* Hidden file input */}
            <input
              id="fileInput"
              type="file"
              accept="image/*"
              onChange={handleFileSelect}
              className="hidden"
            />
            
            {/* File preview and upload */}
            {selectedFile && (
              <div className="card">
                <div className="flex items-center space-x-4">
                  <img
                    src={previewUrl}
                    alt="Preview"
                    className="w-20 h-20 object-cover rounded-lg"
                  />
                  <div className="flex-1">
                    <p className="text-sm font-medium text-gray-900">{selectedFile.name}</p>
                    <p className="text-xs text-gray-500">
                      {(selectedFile.size / 1024 / 1024).toFixed(2)} MB
                    </p>
                  </div>
                  <div className="flex space-x-2">
                    <button
                      onClick={handleUploadPic}
                      disabled={uploadingPic}
                      className="btn-primary text-sm"
                    >
                      {uploadingPic ? 'Uploading...' : 'Upload'}
                    </button>
                    <button
                      onClick={() => {
                        setSelectedFile(null);
                        setPreviewUrl('');
                        document.getElementById('fileInput').value = '';
                      }}
                      className="btn-secondary text-sm"
                    >
                      Cancel
                    </button>
                  </div>
                </div>
              </div>
            )}
            
            {profilePics.length === 0 ? (
              <div className="card text-center py-12">
                <div className="w-16 h-16 bg-gray-200 rounded-full flex items-center justify-center mx-auto mb-4">
                  <svg className="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 002 2z" />
                  </svg>
                </div>
                <h3 className="text-lg font-medium text-gray-900 mb-2">No profile pictures</h3>
                <p className="text-gray-500">Add your first profile picture to get started</p>
              </div>
            ) : (
              <div className="card">
                {/* Picture Carousel */}
                <div className="relative">
                  <div className="aspect-square bg-gray-100 rounded-lg overflow-hidden">
                    <img
                      src={profilePics[currentPicIndex]?.imagePath}
                      alt={`Profile ${currentPicIndex + 1}`}
                      className="w-full h-full object-cover"
                      onError={(e) => {
                        e.target.style.display = 'none';
                        e.target.nextSibling.style.display = 'flex';
                      }}
                    />
                    <div className="w-full h-full bg-primary-100 flex items-center justify-center text-primary-600 font-semibold text-4xl">
                      {profile?.name?.charAt(0) || 'U'}
                    </div>
                  </div>

                  {/* Navigation Arrows */}
                  {profilePics.length > 1 && (
                    <>
                      <button
                        onClick={prevPicture}
                        className="absolute left-2 top-1/2 transform -translate-y-1/2 w-10 h-10 bg-black bg-opacity-50 text-white rounded-full flex items-center justify-center hover:bg-opacity-70 transition-all"
                      >
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                        </svg>
                      </button>
                      <button
                        onClick={nextPicture}
                        className="absolute right-2 top-1/2 transform -translate-y-1/2 w-10 h-10 bg-black bg-opacity-50 text-white rounded-full flex items-center justify-center hover:bg-opacity-70 transition-all"
                      >
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                        </svg>
                      </button>
                    </>
                  )}

                  {/* Picture Counter */}
                  <div className="absolute bottom-2 left-1/2 transform -translate-x-1/2 bg-black bg-opacity-50 text-white px-2 py-1 rounded text-sm">
                    {currentPicIndex + 1} / {profilePics.length}
                  </div>
                </div>

                {/* Picture Actions */}
                <div className="mt-4 flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    <button
                      onClick={() => handleDeletePicture(profilePics[currentPicIndex]?.id)}
                      className="bg-red-600 hover:bg-red-700 text-white font-medium py-2 px-3 rounded-lg transition-colors text-sm"
                    >
                      Delete
                    </button>
                    <button
                      onClick={() => downloadProfilePic(
                        profilePics[currentPicIndex]?.id,
                        profilePics[currentPicIndex]?.imagePath,
                        profilePics[currentPicIndex]?.imageName
                      )}
                      disabled={downloadingPics.has(profilePics[currentPicIndex]?.id)}
                      className="bg-green-600 hover:bg-green-700 text-white font-medium py-2 px-3 rounded-lg transition-colors text-sm disabled:opacity-50"
                    >
                      {downloadingPics.has(profilePics[currentPicIndex]?.id) ? 'Downloading...' : 'Download'}
                    </button>
                  </div>
                  
                  {!profilePics[currentPicIndex]?.isPrimary && (
                    <button
                      onClick={() => handleSetPrimary(profilePics[currentPicIndex]?.id)}
                      className="text-primary-600 hover:text-primary-700 text-sm font-medium"
                    >
                      Set as Primary
                    </button>
                  )}
                </div>

                {/* Thumbnail Navigation */}
                {profilePics.length > 1 && (
                  <div className="mt-4 flex space-x-2 overflow-x-auto">
                    {profilePics.map((pic, index) => (
                      <button
                        key={pic.id}
                        onClick={() => setCurrentPicIndex(index)}
                        className={`flex-shrink-0 w-16 h-16 rounded-lg overflow-hidden border-2 ${
                          index === currentPicIndex 
                            ? 'border-primary-600' 
                            : 'border-gray-200'
                        }`}
                      >
                        <img
                          src={pic.imagePath}
                          alt={`Thumbnail ${index + 1}`}
                          className="w-full h-full object-cover"
                        />
                      </button>
                    ))}
                  </div>
                )}
              </div>
            )}
          </div>

          {/* Profile Details Section */}
          <div className="space-y-6">
            <h2 className="text-lg font-semibold text-gray-900">Profile Details</h2>
            
            <div className="card space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Name</label>
                <p className="text-gray-900">{profile?.name || 'Not set'}</p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Mobile</label>
                <p className="text-gray-900">+91 {profile?.mobile || 'Not set'}</p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Gender</label>
                <p className="text-gray-900">{profile?.gender || 'Not set'}</p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Date of Birth</label>
                <p className="text-gray-900">{formatDate(profile?.dob)}</p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Address</label>
                <p className="text-gray-900">{profile?.address || 'Not set'}</p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Pincode</label>
                <p className="text-gray-900">{profile?.pincode || 'Not set'}</p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Hobbies</label>
                <p className="text-gray-900">{profile?.hobbies || 'Not set'}</p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">About You</label>
                <p className="text-gray-900">{profile?.aboutYou || 'Not set'}</p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Member Since</label>
                <p className="text-gray-900">{formatDate(profile?.createdAt)}</p>
              </div>
            </div>
          </div>
        </div>
      </main>

      {/* Edit Profile Modal */}
      {showEditModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg p-6 max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-xl font-semibold text-gray-900">Edit Profile</h3>
              <button
                onClick={() => setShowEditModal(false)}
                className="text-gray-400 hover:text-gray-600"
              >
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            <form onSubmit={(e) => { e.preventDefault(); handleSaveProfile(); }} className="space-y-6">
              {/* Basic Information */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Name <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    name="name"
                    value={editForm.name}
                    onChange={handleInputChange}
                    className="input-field"
                    placeholder="Enter your name"
                    required
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Gender <span className="text-red-500">*</span>
                  </label>
                  <select
                    name="gender"
                    value={editForm.gender}
                    onChange={handleInputChange}
                    className="input-field"
                    required
                  >
                    <option value="">Select gender</option>
                    <option value="MALE">Male</option>
                    <option value="FEMALE">Female</option>
                    <option value="OTHER">Other</option>
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Date of Birth <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="date"
                    name="dob"
                    value={editForm.dob}
                    onChange={handleInputChange}
                    className="input-field"
                    required
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Pincode
                  </label>
                  <input
                    type="text"
                    name="pincode"
                    value={editForm.pincode}
                    onChange={handleInputChange}
                    className="input-field"
                    placeholder="6-digit pincode"
                    maxLength={6}
                    pattern="\d{6}"
                  />
                </div>
              </div>

              {/* Address */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Address
                </label>
                <textarea
                  name="address"
                  value={editForm.address}
                  onChange={handleInputChange}
                  className="input-field"
                  placeholder="Enter your address"
                  rows={3}
                  maxLength={200}
                />
              </div>

              {/* Coordinates */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Latitude
                  </label>
                  <input
                    type="number"
                    name="latitude"
                    value={editForm.latitude}
                    onChange={handleInputChange}
                    className="input-field"
                    placeholder="e.g., 12.9716"
                    step="any"
                    min="-90"
                    max="90"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Longitude
                  </label>
                  <input
                    type="number"
                    name="longitude"
                    value={editForm.longitude}
                    onChange={handleInputChange}
                    className="input-field"
                    placeholder="e.g., 77.5946"
                    step="any"
                    min="-180"
                    max="180"
                  />
                </div>
              </div>

              {/* Hobbies and About */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Hobbies
                </label>
                <textarea
                  name="hobbies"
                  value={editForm.hobbies}
                  onChange={handleInputChange}
                  className="input-field"
                  placeholder="Enter your hobbies (e.g., Reading, Traveling, Photography)"
                  rows={2}
                  maxLength={500}
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  About You
                </label>
                <textarea
                  name="aboutYou"
                  value={editForm.aboutYou}
                  onChange={handleInputChange}
                  className="input-field"
                  placeholder="Tell us about yourself..."
                  rows={4}
                  maxLength={1000}
                />
              </div>

              {/* Action Buttons */}
              <div className="flex justify-end space-x-3 pt-4 border-t border-gray-200">
                <button
                  type="button"
                  onClick={() => setShowEditModal(false)}
                  className="btn-secondary"
                  disabled={editLoading}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={editLoading}
                  className="btn-primary"
                >
                  {editLoading ? (
                    <div className="flex items-center">
                      <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white mr-2"></div>
                      Saving...
                    </div>
                  ) : (
                    'Save Changes'
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default UserProfileScreen; 