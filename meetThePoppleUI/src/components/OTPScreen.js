import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { authAPI } from '../services/api';

const OTPScreen = () => {
  const [otp, setOtp] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [resendDisabled, setResendDisabled] = useState(false);
  const [countdown, setCountdown] = useState(0);
  const navigate = useNavigate();
  const { login } = useAuth();

  const mobileNumber = sessionStorage.getItem('mobileNumber');

  useEffect(() => {
    if (!mobileNumber) {
      navigate('/login');
      return;
    }

    // Start countdown for resend button
    setCountdown(30);
    const timer = setInterval(() => {
      setCountdown((prev) => {
        if (prev <= 1) {
          setResendDisabled(false);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [mobileNumber, navigate]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!otp || otp.length !== 6) {
      setError('Please enter a valid 6-digit OTP');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const response = await authAPI.login(mobileNumber, otp);
      
      if (response.data.success) {
        const { token, user } = response.data.data;
        login(user, token);
        sessionStorage.removeItem('mobileNumber');
        navigate('/');
      } else {
        setError(response.data.message || 'Invalid OTP');
      }
    } catch (error) {
      console.error('Error logging in:', error);
      setError(
        error.response?.data?.message || 
        'Failed to verify OTP. Please try again.'
      );
    } finally {
      setLoading(false);
    }
  };

  const handleResendOTP = async () => {
    if (resendDisabled) return;

    setResendDisabled(true);
    setCountdown(30);
    setError('');

    try {
      const response = await authAPI.generateOTP(mobileNumber);
      
      if (response.data.success) {
        setError('');
      } else {
        setError(response.data.message || 'Failed to resend OTP');
      }
    } catch (error) {
      console.error('Error resending OTP:', error);
      setError(
        error.response?.data?.message || 
        'Failed to resend OTP. Please try again.'
      );
    }
  };

  const handleBackToLogin = () => {
    sessionStorage.removeItem('mobileNumber');
    navigate('/login');
  };

  if (!mobileNumber) {
    return null;
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary-50 to-primary-100 px-4">
      <div className="max-w-md w-full space-y-8">
        {/* Header */}
        <div className="text-center">
          <h1 className="text-3xl font-bold text-primary-900 mb-2">
            Verify OTP
          </h1>
          <p className="text-gray-600">
            Enter the 6-digit code sent to
          </p>
          <p className="text-primary-600 font-medium">
            +91 {mobileNumber}
          </p>
        </div>

        {/* OTP Form */}
        <div className="card animate-fade-in">
          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <label htmlFor="otp" className="block text-sm font-medium text-gray-700 mb-2">
                OTP Code
              </label>
              <input
                id="otp"
                type="text"
                value={otp}
                onChange={(e) => setOtp(e.target.value.replace(/\D/g, ''))}
                placeholder="Enter 6-digit OTP"
                className="input-field text-center text-2xl tracking-widest"
                maxLength={6}
                required
                autoFocus
              />
            </div>

            {error && (
              <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
                {error}
              </div>
            )}

            <button
              type="submit"
              disabled={loading || otp.length !== 6}
              className="btn-primary w-full disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? (
                <div className="flex items-center justify-center">
                  <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white mr-2"></div>
                  Verifying...
                </div>
              ) : (
                'Verify OTP'
              )}
            </button>
          </form>

          {/* Resend OTP */}
          <div className="mt-6 text-center">
            <button
              onClick={handleResendOTP}
              disabled={resendDisabled}
              className="text-primary-600 hover:text-primary-700 disabled:text-gray-400 disabled:cursor-not-allowed"
            >
              {resendDisabled 
                ? `Resend OTP in ${countdown}s` 
                : 'Resend OTP'
              }
            </button>
          </div>

          {/* Back to Login */}
          <div className="mt-4 text-center">
            <button
              onClick={handleBackToLogin}
              className="text-gray-500 hover:text-gray-700 text-sm"
            >
              ← Back to Login
            </button>
          </div>
        </div>

        {/* Footer */}
        <div className="text-center text-sm text-gray-500">
          <p>Didn't receive the code? Check your SMS inbox</p>
        </div>
      </div>
    </div>
  );
};

export default OTPScreen; 