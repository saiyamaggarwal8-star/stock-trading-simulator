import React, { useState, useEffect } from 'react';
import { Lock, User, ArrowRight, Gavel } from 'lucide-react';
import axios from 'axios';
import Register from './Register';

export default function Login({ onLogin }) {
  const [isRegistering, setIsRegistering] = useState(false);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [rememberMe, setRememberMe] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  
  // Forgot Password Flow
  const [recoveryStep, setRecoveryStep] = useState(0); // 0: Login, 1: Username, 2: Answer, 3: New Password
  const [recoveryUsername, setRecoveryUsername] = useState('');
  const [recoveryQuestion, setRecoveryQuestion] = useState('');
  const [recoveryAnswer, setRecoveryAnswer] = useState('');
  const [newPassword, setNewPassword] = useState('');

  useEffect(() => {
    const saved = localStorage.getItem('rememberedUser');
    if (saved) {
      setUsername(saved);
      setRememberMe(true);
    }
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const response = await axios.post('/api/auth/login', { username, password });
      if (rememberMe) {
        localStorage.setItem('rememberedUser', username);
      } else {
        localStorage.removeItem('rememberedUser');
      }
      onLogin(response.data, false);
    } catch (err) {
      setError('Invalid credentials. Check your username and password.');
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleLogin = () => {
    window.location.href = "http://localhost:8081/oauth2/authorization/google";
  };

  const startRecovery = () => {
    setRecoveryStep(1);
    setError('');
  };

  const fetchQuestion = async () => {
    setLoading(true);
    try {
      const resp = await axios.post('/api/auth/forgot-password/question', { username: recoveryUsername });
      setRecoveryQuestion(resp.data);
      setRecoveryStep(2);
    } catch (err) {
      setError('Username not found');
    } finally {
      setLoading(false);
    }
  };

  const handleReset = async () => {
    setLoading(true);
    try {
      await axios.post('/api/auth/forgot-password/reset', { 
        username: recoveryUsername, 
        securityAnswer: recoveryAnswer, 
        password: newPassword 
      });
      alert('Password reset successful! Please sign in.');
      setRecoveryStep(0);
      setRecoveryUsername('');
      setRecoveryAnswer('');
      setNewPassword('');
    } catch (err) {
      setError('Incorrect answer or reset failed');
    } finally {
      setLoading(false);
    }
  };

  if (isRegistering) {
    return <Register onRegister={onLogin} onBackToLogin={() => setIsRegistering(false)} />;
  }

  return (
    <div className="login-container">
      <div className="login-card">
        <div className="login-header">
          <div className="logo-icon">
            <img 
              src="/logo.png" 
              alt="FinNova Logo" 
              style={{ width: '120px', height: '120px', objectFit: 'contain' }} 
            />
          </div>
          <h1>
            {recoveryStep === 0 && "FinNova"}
            {recoveryStep === 1 && "Recover Account"}
            {recoveryStep === 2 && "Security Check"}
            {recoveryStep === 3 && "Reset Password"}
          </h1>
          <p>
            {recoveryStep === 0 && "Sign in to access your dashboard"}
            {recoveryStep === 1 && "Enter your username to begin"}
            {recoveryStep === 2 && "Answer your secret question"}
            {recoveryStep === 3 && "Set your new secret access key"}
          </p>
        </div>

        {recoveryStep === 0 ? (
          <form onSubmit={handleSubmit} className="login-form">
            {error && <div className="error-message">{error}</div>}

            <div className="input-group">
              <label>Username</label>
              <div className="input-wrapper">
                <User size={18} className="input-icon" />
                <input
                  type="text"
                  placeholder="Enter your username"
                  value={username}
                  onChange={(e) => {
                    setUsername(e.target.value);
                    setError('');
                  }}
                  required
                />
              </div>
            </div>

            <div className="input-group">
              <label>Password</label>
              <div className="input-wrapper">
                <Lock size={18} className="input-icon" />
                <input
                  type="password"
                  placeholder="Enter your password"
                  value={password}
                  onChange={(e) => {
                    setPassword(e.target.value);
                    setError('');
                  }}
                  required
                />
              </div>
            </div>

            <div className="form-options">
              <label className="remember-me">
                <input 
                  type="checkbox" 
                  checked={rememberMe}
                  onChange={(e) => setRememberMe(e.target.checked)}
                />
                <span>Remember me</span>
              </label>
              <a href="#" className="forgot-password" onClick={(e) => { e.preventDefault(); startRecovery(); }}>Forgot password?</a>
            </div>

            <button type="submit" className="btn-login" disabled={loading}>
              {loading ? (
                <span className="loader"></span>
              ) : (
                <>
                  Sign In <ArrowRight size={18} />
                </>
              )}
            </button>

            <button 
              type="button" 
              className="btn-login" 
              style={{ 
                background: 'rgba(255, 255, 255, 0.05)', 
                color: '#ffffff', 
                marginTop: '0.75rem', 
                border: '1px solid var(--glass-border)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '0.9rem',
                whiteSpace: 'nowrap'
              }} 
              onClick={handleGoogleLogin}
            >
              <img 
                src="https://www.gstatic.com/images/branding/product/2x/googleg_48dp.png" 
                alt="Google Logo" 
                style={{ width: '20px', height: '20px', marginRight: '12px', objectFit: 'contain' }} 
              />
              Sign In with Google
            </button>
          </form>
        ) : (
          <div className="login-form">
            {error && <div className="error-message">{error}</div>}

            {recoveryStep === 1 && (
              <div className="input-group">
                <label>Username</label>
                <div className="input-wrapper">
                  <User size={18} className="input-icon" />
                  <input
                    type="text"
                    placeholder="Your username"
                    value={recoveryUsername}
                    onChange={(e) => setRecoveryUsername(e.target.value)}
                  />
                </div>
                <button className="btn-login" onClick={fetchQuestion} disabled={loading} style={{ background: 'var(--accent-red)' }}>
                  Next <ArrowRight size={18} />
                </button>
              </div>
            )}

            {recoveryStep === 2 && (
              <div className="input-group">
                <label>{recoveryQuestion}</label>
                <div className="input-wrapper">
                  <Lock size={18} className="input-icon" />
                  <input
                    type="text"
                    placeholder="Your answer"
                    value={recoveryAnswer}
                    onChange={(e) => setRecoveryAnswer(e.target.value)}
                  />
                </div>
                <button className="btn-login" onClick={() => setRecoveryStep(3)} style={{ background: 'var(--accent-red)' }}>
                  Verify <ArrowRight size={18} />
                </button>
              </div>
            )}

            {recoveryStep === 3 && (
              <div className="input-group">
                <label>New Password</label>
                <div className="input-wrapper">
                  <Lock size={18} className="input-icon" />
                  <input
                    type="password"
                    placeholder="Enter new password"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                  />
                </div>
                <button className="btn-login" onClick={handleReset} disabled={loading} style={{ background: 'var(--accent-red)' }}>
                  Reset Password <ArrowRight size={18} />
                </button>
              </div>
            )}

            <button 
              className="btn-login" 
              style={{ background: 'transparent', color: 'var(--text-primary)', border: 'none', boxShadow: 'none' }}
              onClick={() => setRecoveryStep(0)}
            >
              ← Back to Login
            </button>
          </div>
        )}

        <div className="login-footer">
          <p>New trainee? <a href="#" onClick={(e) => { e.preventDefault(); setIsRegistering(true); }} style={{ fontWeight: 'bold', color: 'var(--accent-red)' }}>Create Account</a></p>
        </div>
      </div>
    </div>
  );
}
