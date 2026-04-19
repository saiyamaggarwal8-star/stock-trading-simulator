import React, { useState } from 'react';
import { Lock, User, ArrowRight, UserPlus } from 'lucide-react';
import axios from 'axios';

export default function Register({ onRegister, onBackToLogin }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [securityQuestion, setSecurityQuestion] = useState('');
  const [securityAnswer, setSecurityAnswer] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (password !== confirmPassword) {
      setError('Passwords do not match');
      return;
    }
    setLoading(true);

    try {
      const response = await axios.post('/api/auth/register', { 
        username, 
        password,
        securityQuestion,
        securityAnswer
      });
      onRegister(response.data, true);
    } catch (err) {
      setError(err.response?.data || 'Registration failed. Try another username.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <div className="login-header">
          <div className="logo-icon" style={{ marginBottom: '1rem' }}>
            <UserPlus size={40} />
          </div>
          <h1>Join FinNova</h1>
          <p>Create your private trading account</p>
        </div>

        <form onSubmit={handleSubmit} className="login-form">
          {error && <div className="error-message">{error}</div>}

          <div className="input-group">
            <label>Username</label>
            <div className="input-wrapper">
              <User size={18} className="input-icon" />
              <input
                type="text"
                placeholder="Pick a username"
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
                placeholder="Create a password"
                value={password}
                onChange={(e) => {
                  setPassword(e.target.value);
                  setError('');
                }}
                required
              />
            </div>
          </div>

          <div className="input-group">
            <label>Confirm Password</label>
            <div className="input-wrapper">
              <Lock size={18} className="input-icon" />
              <input
                type="password"
                placeholder="Confirm your password"
                value={confirmPassword}
                onChange={(e) => {
                  setConfirmPassword(e.target.value);
                  setError('');
                }}
                required
              />
            </div>
          </div>

          <div className="input-group">
            <label>Security Question</label>
            <select 
              value={securityQuestion} 
              onChange={(e) => setSecurityQuestion(e.target.value)}
              required
            >
              <option value="">Select a security question</option>
              <option value="What was the name of your first pet?">What was the name of your first pet?</option>
              <option value="In what city was your first job?">In what city was your first job?</option>
              <option value="What is your mother's maiden name?">What is your mother's maiden name?</option>
              <option value="What was the model of your first car?">What was the model of your first car?</option>
              <option value="What is your nickname?">What is your nickname?</option>
              <option value="Which city were you born in?">Which city were you born in?</option>
            </select>
          </div>

          <div className="input-group">
            <label>Secret Answer</label>
            <div className="input-wrapper">
              <Lock size={18} className="input-icon" />
              <input
                type="text"
                placeholder="Enter your secret answer"
                value={securityAnswer}
                onChange={(e) => setSecurityAnswer(e.target.value)}
                required
              />
            </div>
          </div>

          <button type="submit" className="btn-login" disabled={loading} style={{ background: 'var(--accent-green)' }}>
            {loading ? (
              <span className="loader"></span>
            ) : (
              <>
                Sign Up <ArrowRight size={18} />
              </>
            )}
          </button>
        </form>

        <div className="login-footer">
          <p>Already have an account? <a href="#" onClick={(e) => { e.preventDefault(); onBackToLogin(); }} style={{ fontWeight: 'bold', color: 'var(--accent-red)' }}>Sign In</a></p>
        </div>
      </div>
    </div>
  );
}
