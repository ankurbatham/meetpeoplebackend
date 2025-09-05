# Meet The People - React Messaging UI

A modern, responsive React messaging application built with React, Tailwind CSS, and React Router. This application provides a complete user interface for the Meet The People social networking platform.

## 🌟 Features

### **Authentication Flow**
- **Mobile Number Input**: Enter 10-digit mobile number
- **OTP Verification**: Secure OTP-based authentication
- **JWT Token Management**: Automatic token handling and persistence

### **User Interface**
- **Responsive Design**: Works seamlessly on both desktop and mobile
- **Modern UI**: Clean, intuitive interface with Tailwind CSS
- **Real-time Updates**: Live user activity tracking
- **Profile Management**: Complete user profile with photo carousel

### **Core Functionality**
- **Conversation List**: View all active conversations
- **User Discovery**: Search and connect with people nearby
- **Profile Pictures**: Multiple photos with carousel navigation
- **Online Status**: Real-time user activity indicators

## 🚀 Getting Started

### **Prerequisites**
- Node.js (v16 or higher)
- npm or yarn package manager
- Backend API running on `http://localhost:8080`

### **Installation**

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd meetThePoppleUI
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Start the development server**
   ```bash
   npm start
   ```

4. **Open your browser**
   Navigate to `http://localhost:3000`

### **Build for Production**
```bash
npm run build
```

## 🏗️ Project Structure

```
src/
├── components/           # React components
│   ├── LoginScreen.js   # Mobile number input screen
│   ├── OTPScreen.js     # OTP verification screen
│   ├── HomeScreen.js    # Main conversations list
│   └── UserProfileScreen.js # User profile management
├── contexts/            # React contexts
│   └── AuthContext.js   # Authentication state management
├── services/            # API services
│   └── api.js          # Axios configuration and endpoints
├── App.js              # Main application component
├── index.js            # Application entry point
└── index.css           # Global styles and Tailwind imports
```

## 🔧 Configuration

### **API Configuration**
The application is configured to connect to the backend API at `http://localhost:8080`. You can modify this in `src/services/api.js`:

```javascript
export const api = axios.create({
  baseURL: 'http://localhost:8080/api', // Change this URL
  timeout: 10000,
  // ... other config
});
```

### **Tailwind CSS**
Custom Tailwind configuration is available in `tailwind.config.js` with:
- Custom color palette
- Responsive breakpoints
- Custom animations
- Component utilities

## 📱 Screen Flow

### **1. Login Screen (`/login`)**
- Mobile number input with validation
- Generate OTP functionality
- Responsive design for all devices

### **2. OTP Screen (`/otp`)**
- 6-digit OTP input
- Resend OTP with countdown timer
- Back to login navigation

### **3. Home Screen (`/`)**
- List of existing conversations
- User profile button (top-right)
- Search floating action button (bottom-right)
- Real-time activity tracking

### **4. User Profile Screen (`/profile`)**
- Complete user profile display
- Profile picture carousel with navigation
- Edit/delete picture options
- Profile editing modal (placeholder)

## 🎨 UI Components

### **Custom Button Classes**
- `.btn-primary`: Primary action buttons
- `.btn-secondary`: Secondary action buttons
- `.floating-action-btn`: Floating action buttons

### **Form Elements**
- `.input-field`: Consistent input styling
- `.card`: Card container styling

### **Animations**
- `.animate-fade-in`: Fade in animation
- `.animate-slide-up`: Slide up animation

## 🔐 Authentication

### **Token Management**
- JWT tokens stored in localStorage
- Automatic token inclusion in API requests
- Token expiration handling
- Automatic logout on authentication failure

### **Protected Routes**
- Authentication context provides route protection
- Automatic redirection for unauthenticated users
- Persistent login state

## 📡 API Integration

### **Available Endpoints**
- **Authentication**: OTP generation and verification
- **User Management**: Profile CRUD operations
- **Profile Pictures**: Upload, delete, reorder
- **Messaging**: Send and retrieve messages
- **User Activity**: Online status tracking
- **User Blocking**: Block/unblock functionality

### **Error Handling**
- Comprehensive error handling for all API calls
- User-friendly error messages
- Automatic retry for network issues
- Graceful fallbacks for failed requests

## 🎯 Future Enhancements

### **Planned Features**
- [ ] Real-time messaging with WebSocket
- [ ] Push notifications
- [ ] File upload functionality
- [ ] Advanced search filters
- [ ] User blocking interface
- [ ] Message retention settings

### **Technical Improvements**
- [ ] Unit and integration tests
- [ ] Performance optimization
- [ ] Progressive Web App (PWA) features
- [ ] Offline support
- [ ] Internationalization (i18n)

## 🧪 Testing

### **Manual Testing**
1. **Authentication Flow**
   - Test mobile number validation
   - Verify OTP functionality
   - Test token persistence

2. **Navigation**
   - Test all screen transitions
   - Verify protected route access
   - Test back navigation

3. **Responsiveness**
   - Test on different screen sizes
   - Verify mobile and desktop layouts
   - Test touch interactions

### **API Testing**
Use the provided Postman collection or test with cURL commands from the API documentation.

## 🐛 Troubleshooting

### **Common Issues**

1. **API Connection Failed**
   - Verify backend server is running
   - Check API base URL configuration
   - Ensure CORS is properly configured

2. **Authentication Issues**
   - Clear localStorage and try again
   - Check browser console for errors
   - Verify JWT token format

3. **Styling Issues**
   - Ensure Tailwind CSS is properly imported
   - Check for CSS conflicts
   - Verify PostCSS configuration

### **Debug Mode**
Enable debug logging by setting:
```javascript
localStorage.setItem('debug', 'true');
```

## 📚 Dependencies

### **Core Dependencies**
- **React 18**: Modern React with hooks
- **React Router 6**: Client-side routing
- **Axios**: HTTP client for API calls
- **Tailwind CSS**: Utility-first CSS framework

### **Development Dependencies**
- **PostCSS**: CSS processing
- **Autoprefixer**: CSS vendor prefixing
- **Create React App**: Development tooling

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 📞 Support

For technical support or questions:
- **Email**: support@meetthepeople.com
- **Documentation**: https://docs.meetthepeople.com
- **Issues**: GitHub Issues page

---

**Note**: This is a frontend application that requires a running backend API. Make sure your backend server is properly configured and running before testing the application. 