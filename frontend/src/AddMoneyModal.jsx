import React, { useState } from 'react';
import axios from 'axios';
import { X, Wallet, IndianRupee } from 'lucide-react';

export default function AddMoneyModal({ onClose, onBalanceUpdated }) {
  const [amount, setAmount] = useState('10000');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const predefinedAmounts = [5000, 10000, 50000, 100000, 500000];

  const handleAddFunds = async (e) => {
    e.preventDefault();
    const numAmount = parseFloat(amount);
    if (!numAmount || numAmount <= 0) {
      setError("Please enter a valid amount.");
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const res = await axios.post('/api/user/wallet/add', { amount: numAmount });
      onBalanceUpdated(res.data); // Return the newly updated total balance
      onClose();
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.message || "Failed to add funds. Please try again.");
      setLoading(false);
    }
  };

  return (
    <div style={{
      position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
      background: 'rgba(5, 8, 20, 0.8)', backdropFilter: 'blur(8px)',
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      zIndex: 10000, padding: '1rem', animation: 'fadeIn 0.2s ease-out'
    }}>
      <div style={{
        background: '#0f172a', width: '100%', maxWidth: '400px',
        borderRadius: '24px', border: '1px solid rgba(16, 185, 129, 0.3)',
        boxShadow: '0 25px 50px rgba(0,0,0,0.5)', overflow: 'hidden',
        animation: 'slideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1)',
        display: 'flex', flexDirection: 'column'
      }}>
        {/* Header */}
        <div style={{
          padding: '1.5rem', borderBottom: '1px solid rgba(255,255,255,0.05)',
          display: 'flex', justifyContent: 'space-between', alignItems: 'center',
          background: 'linear-gradient(135deg, rgba(16,185,129,0.1), rgba(16,185,129,0.02))'
        }}>
          <h2 style={{ margin: 0, fontSize: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'white' }}>
            <Wallet size={24} color="#10b981" /> Add Funds
          </h2>
          <button onClick={onClose} style={{
            background: 'transparent', border: 'none', color: '#94a3b8', cursor: 'pointer',
            padding: '4px', borderRadius: '50%', display: 'flex', transition: 'all 0.2s'
          }} onMouseOver={e => e.currentTarget.style.background = 'rgba(255,255,255,0.1)'}
             onMouseOut={e => e.currentTarget.style.background = 'transparent'}
          >
            <X size={20} />
          </button>
        </div>

        {/* Content */}
        <div style={{ padding: '1.5rem' }}>
          <p style={{ color: '#94a3b8', fontSize: '0.875rem', marginBottom: '1.5rem', lineHeight: 1.5 }}>
            Top up your FinNova virtual simulation wallet. This is play money meant for practice trading.
          </p>

          <form onSubmit={handleAddFunds}>
            <div style={{ marginBottom: '1.5rem' }}>
              <label style={{ display: 'block', color: 'white', fontSize: '0.875rem', fontWeight: 600, marginBottom: '0.5rem' }}>
                Amount to Add
              </label>
              <div style={{ position: 'relative' }}>
                <div style={{ position: 'absolute', left: '1rem', top: '50%', transform: 'translateY(-50%)', color: '#94a3b8', display: 'flex' }}>
                  <IndianRupee size={18} />
                </div>
                <input
                  type="number"
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  style={{
                    width: '100%', padding: '0.875rem 1rem 0.875rem 2.5rem',
                    background: '#1e293b', border: '1px solid #334155', borderRadius: '12px',
                    color: 'white', fontSize: '1.25rem', fontWeight: 600, outline: 'none',
                    transition: 'border-color 0.2s'
                  }}
                  onFocus={e => e.target.style.borderColor = '#10b981'}
                  onBlur={e => e.target.style.borderColor = '#334155'}
                />
              </div>
            </div>

            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem', marginBottom: '2rem' }}>
               {predefinedAmounts.map(preset => (
                 <button
                   key={preset}
                   type="button"
                   onClick={() => setAmount(preset.toString())}
                   style={{
                     padding: '0.4rem 0.8rem', borderRadius: '20px', fontSize: '0.75rem', fontWeight: 600,
                     background: amount === preset.toString() ? 'rgba(16,185,129,0.2)' : 'rgba(255,255,255,0.05)',
                     color: amount === preset.toString() ? '#10b981' : '#cbd5e1',
                     border: `1px solid ${amount === preset.toString() ? 'rgba(16,185,129,0.5)' : 'rgba(255,255,255,0.1)'}`,
                     cursor: 'pointer', transition: 'all 0.2s'
                   }}
                 >
                   +{preset.toLocaleString('en-IN')}
                 </button>
               ))}
            </div>

            {error && (
              <div style={{ padding: '0.75rem', background: 'rgba(239,68,68,0.1)', color: '#ef4444', borderRadius: '8px', fontSize: '0.875rem', marginBottom: '1rem', textAlign: 'center' }}>
                {error}
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              style={{
                width: '100%', padding: '1rem', borderRadius: '12px',
                background: 'linear-gradient(135deg, #10b981, #059669)',
                color: 'white', border: 'none', fontSize: '1rem', fontWeight: 700, cursor: 'pointer',
                boxShadow: '0 8px 20px rgba(16,185,129,0.4)', transition: 'transform 0.2s, opacity 0.2s',
                opacity: loading ? 0.7 : 1, display: 'flex', justifyContent: 'center', alignItems: 'center'
              }}
              onMouseOver={e => !loading && (e.currentTarget.style.transform = 'translateY(-2px)')}
              onMouseOut={e => !loading && (e.currentTarget.style.transform = 'translateY(0)')}
            >
              {loading ? 'Processing...' : 'Add to Wallet'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
