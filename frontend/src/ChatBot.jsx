import React, { useState, useRef, useEffect } from 'react';
import { MessageSquare, X, Send, Sparkles, User, Minus } from 'lucide-react';

const KNOWLEDGE_BASE = [
  {
    keywords: ['buy', 'how to buy', 'purchase', 'buying'],
    response: "To buy a stock, select it from the 'NSE Market' list on the left. Then look at the 'Place Order' panel on the right, enter the number of shares you want, and click the green BUY button! Make sure you have enough Balance."
  },
  {
    keywords: ['sell', 'how to sell', 'selling', 'short'],
    response: "To sell, you must first own the stock! Select a stock you own from your 'My Portfolio' list, enter the quantity in the complete 'Place Order' panel on the right, and click the red SELL button."
  },
  {
    keywords: ['rsi', 'relative strength', 'overbought', 'oversold'],
    response: "RSI (Relative Strength Index) helps you spot if a stock is overbought (>70) or oversold (<30). If it's below 30, it might be a good time to buy! Click the 'RSI' button under the chart to turn it on."
  },
  {
    keywords: ['sma', 'moving average', 'ema', 'trend'],
    response: "SMA (Simple Moving Average) and EMA (Exponential) show the stock's trend. If the price is above the line, it's an uptrend (good to buy). If it's below, it's a downtrend. Try clicking the SMA button below the chart!"
  },
  {
    keywords: ['pattern', 'cheat sheet', 'bullish', 'bearish', 'wedge', 'triangle'],
    response: "Chart patterns predict future price movements! Use the 'Scan Patterns' button below the chart to find them automatically. You can read what they mean in the Pattern Cheat Sheet on the right."
  },
  {
    keywords: ['what is a stock', 'stock market', 'explain stock'],
    response: "A stock represents a tiny fraction of ownership in a company. When the company grows, your stock value goes up! You can learn all the basics by clicking the 📚 Learn button at the top to open the Learning Center."
  },
  {
    keywords: ['balance', 'money', 'funds', 'cash'],
    response: "You start with ₹1,00,000 in virtual cash. You can see your live balance at the top of the screen. Try to grow it by making smart trades!"
  },
  {
    keywords: ['hello', 'hi', 'hey', 'greetings', 'nova'],
    response: "Hello there! 👋 I'm Nova, your AI trading assistant. Ask me anything about how to trade, use the app, or understand technical indicators!"
  },
  {
    keywords: ['thanks', 'thank you', 'thx'],
    response: "You're very welcome! Happy trading! 🚀 Feel free to ask if you need anything else."
  }
];

const DEFAULT_RESPONSES = [
  "That's a great question! For detailed strategies, I recommend checking out our 📚 Learning Center at the top of the screen.",
  "I'm still learning about that specific topic! Why don't you try asking me about how to buy/sell, or how to use indicators like RSI and SMA?",
  "Interesting! As a trading simulation AI, I'd suggest focusing on the technical indicators below the chart to make your next move.",
  "I don't have live news feeds right now, but you can always rely on the chart patterns! Try clicking 'Scan Patterns' to see what the chart is doing."
];

export default function ChatBot({ username }) {
  const [isOpen, setIsOpen] = useState(false);
  const [isMinimized, setIsMinimized] = useState(false);
  const [messages, setMessages] = useState([
    { id: 1, sender: 'nova', text: `Hi ${username || 'Trader'}! I'm Nova. How can I help you dominate the market today?`, time: new Date() }
  ]);
  const [inputText, setInputText] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    if (isOpen && !isMinimized) {
      scrollToBottom();
    }
  }, [messages, isOpen, isMinimized, isTyping]);

  const getAiResponse = (text) => {
    const lowerText = text.toLowerCase();
    
    // Check knowledge base
    for (const item of KNOWLEDGE_BASE) {
      if (item.keywords.some(kw => lowerText.includes(kw))) {
        return item.response;
      }
    }
    
    // Fallback response
    return DEFAULT_RESPONSES[Math.floor(Math.random() * DEFAULT_RESPONSES.length)];
  };

  const handleSend = (e) => {
    e?.preventDefault();
    if (!inputText.trim()) return;

    const userMsg = {
      id: Date.now(),
      sender: 'user',
      text: inputText,
      time: new Date()
    };

    setMessages(prev => [...prev, userMsg]);
    setInputText('');
    setIsTyping(true);

    // Simulate AI thinking delay
    setTimeout(() => {
      const responseText = getAiResponse(userMsg.text);
      const aiMsg = {
        id: Date.now() + 1,
        sender: 'nova',
        text: responseText,
        time: new Date()
      };
      setMessages(prev => [...prev, aiMsg]);
      setIsTyping(false);
    }, 1000 + Math.random() * 1000); // 1-2 second delay
  };

  const formatTime = (date) => {
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  if (!isOpen) {
    return (
      <button 
        onClick={() => { setIsOpen(true); setIsMinimized(false); }}
        style={{
          position: 'fixed', bottom: '2rem', right: '2rem', zIndex: 9999,
          width: '60px', height: '60px', borderRadius: '50%',
          background: 'linear-gradient(135deg, #3b82f6, #8b5cf6)',
          border: 'none', color: 'white', cursor: 'pointer',
          boxShadow: '0 8px 30px rgba(59,130,246,0.4)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          animation: 'chat-bounce 2s infinite',
          transition: 'transform 0.2s'
        }}
        onMouseOver={e => e.currentTarget.style.transform = 'scale(1.1)'}
        onMouseOut={e => e.currentTarget.style.transform = 'scale(1)'}
      >
        <MessageSquare size={28} />
      </button>
    );
  }

  return (
    <>
      <div style={{
        position: 'fixed', bottom: isMinimized ? '2rem' : '2rem', right: '2rem', zIndex: 9999,
        width: '360px', height: isMinimized ? '60px' : '520px',
        background: '#0f172a',
        border: '1px solid rgba(59,130,246,0.3)',
        borderRadius: '20px',
        boxShadow: '0 15px 50px rgba(0,0,0,0.5)',
        display: 'flex', flexDirection: 'column',
        fontFamily: "'Inter', -apple-system, sans-serif",
        overflow: 'hidden',
        transition: 'all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1)'
      }}>
        {/* Header */}
        <div style={{
          padding: '1rem', background: 'linear-gradient(135deg, #1e293b, #0f172a)',
          borderBottom: '1px solid rgba(255,255,255,0.05)',
          display: 'flex', justifyContent: 'space-between', alignItems: 'center',
          cursor: 'pointer'
        }} onClick={() => setIsMinimized(!isMinimized)}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <div style={{
              width: 34, height: 34, borderRadius: '50%',
              background: 'linear-gradient(135deg, #3b82f6, #8b5cf6)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              boxShadow: '0 0 10px rgba(139,92,246,0.4)'
            }}>
              <Sparkles size={16} color="white" />
            </div>
            <div>
              <div style={{ fontWeight: 700, fontSize: '0.95rem', color: '#f1f5f9' }}>Nova AI</div>
              <div style={{ fontSize: '0.7rem', color: '#10b981', display: 'flex', alignItems: 'center', gap: '4px' }}>
                <div style={{ width: 6, height: 6, background: '#10b981', borderRadius: '50%', boxShadow: '0 0 5px #10b981' }} />
                Online
              </div>
            </div>
          </div>
          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <button
              onClick={(e) => { e.stopPropagation(); setIsMinimized(!isMinimized); }}
              style={{ background: 'transparent', border: 'none', color: '#94a3b8', cursor: 'pointer', display: 'flex' }}
            >
              <Minus size={18} />
            </button>
            <button
              onClick={(e) => { e.stopPropagation(); setIsOpen(false); }}
              style={{ background: 'transparent', border: 'none', color: '#94a3b8', cursor: 'pointer', display: 'flex' }}
            >
              <X size={18} />
            </button>
          </div>
        </div>

        {/* Chat Area */}
        {!isMinimized && (
          <>
            <div style={{
              flex: 1, overflowY: 'auto', padding: '1rem',
              display: 'flex', flexDirection: 'column', gap: '1rem',
              background: '#050814'
            }}>
              <div style={{ textAlign: 'center', fontSize: '0.75rem', color: '#475569', marginBottom: '0.5rem' }}>
                Chat started tonight
              </div>

              {messages.map(msg => (
                <div key={msg.id} style={{
                  display: 'flex', gap: '0.5rem',
                  alignSelf: msg.sender === 'user' ? 'flex-end' : 'flex-start',
                  flexDirection: msg.sender === 'user' ? 'row-reverse' : 'row',
                  maxWidth: '85%'
                }}>
                  {/* Avatar */}
                  <div style={{
                    width: 28, height: 28, borderRadius: '50%', flexShrink: 0,
                    background: msg.sender === 'user' ? '#1e293b' : 'linear-gradient(135deg, #3b82f6, #8b5cf6)',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    marginTop: 'auto'
                  }}>
                    {msg.sender === 'user' ? <User size={14} color="#94a3b8" /> : <span style={{ fontSize: '14px' }}>🤖</span>}
                  </div>

                  {/* Bubble */}
                  <div style={{ display: 'flex', flexDirection: 'column', alignItems: msg.sender === 'user' ? 'flex-end' : 'flex-start' }}>
                    <div style={{
                      padding: '0.75rem 1rem',
                      background: msg.sender === 'user' ? '#3b82f6' : '#1e293b',
                      color: '#f8fafc', fontSize: '0.85rem', lineHeight: 1.5,
                      borderRadius: msg.sender === 'user' ? '18px 18px 4px 18px' : '18px 18px 18px 4px',
                      border: msg.sender === 'user' ? 'none' : '1px solid rgba(255,255,255,0.05)',
                      boxShadow: '0 4px 15px rgba(0,0,0,0.1)'
                    }}>
                      {msg.text}
                    </div>
                    <span style={{ fontSize: '0.65rem', color: '#475569', marginTop: '4px', padding: '0 4px' }}>
                      {formatTime(msg.time)}
                    </span>
                  </div>
                </div>
              ))}

              {isTyping && (
                <div style={{ display: 'flex', gap: '0.5rem', alignSelf: 'flex-start' }}>
                  <div style={{
                    width: 28, height: 28, borderRadius: '50%', flexShrink: 0,
                    background: 'linear-gradient(135deg, #3b82f6, #8b5cf6)',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    marginTop: 'auto'
                  }}>
                    <span style={{ fontSize: '14px' }}>🤖</span>
                  </div>
                  <div style={{
                    padding: '0.75rem 1rem', background: '#1e293b',
                    borderRadius: '18px 18px 18px 4px',
                    display: 'flex', gap: '4px', alignItems: 'center'
                  }}>
                    <span className="dot-typing" style={{ animationDelay: '0ms' }}></span>
                    <span className="dot-typing" style={{ animationDelay: '200ms' }}></span>
                    <span className="dot-typing" style={{ animationDelay: '400ms' }}></span>
                  </div>
                </div>
              )}
              <div ref={messagesEndRef} />
            </div>

            {/* Input Area */}
            <form onSubmit={handleSend} style={{
              padding: '1rem', background: '#0f172a',
              borderTop: '1px solid rgba(255,255,255,0.05)',
              display: 'flex', gap: '0.5rem'
            }}>
              <input
                type="text"
                value={inputText}
                onChange={(e) => setInputText(e.target.value)}
                placeholder="Ask Nova anything..."
                style={{
                  flex: 1, padding: '0.75rem 1rem',
                  background: 'rgba(255,255,255,0.03)',
                  border: '1px solid rgba(255,255,255,0.1)',
                  borderRadius: '24px', color: 'white', fontSize: '0.85rem',
                  outline: 'none'
                }}
              />
              <button
                type="submit"
                disabled={!inputText.trim() || isTyping}
                style={{
                  width: '42px', height: '42px', borderRadius: '50%',
                  background: inputText.trim() && !isTyping ? '#3b82f6' : 'rgba(255,255,255,0.1)',
                  border: 'none', color: 'white', cursor: inputText.trim() && !isTyping ? 'pointer' : 'not-allowed',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  transition: 'background 0.2s'
                }}
              >
                <Send size={16} style={{ marginLeft: '2px' }} />
              </button>
            </form>
          </>
        )}
      </div>

      <style>{`
        @keyframes chat-bounce {
          0%, 100% { transform: translateY(0); }
          50% { transform: translateY(-8px); }
        }
        .dot-typing {
          width: 6px; height: 6px;
          background: #94a3b8;
          border-radius: 50%;
          animation: dot-bounce 1.4s infinite ease-in-out both;
        }
        @keyframes dot-bounce {
          0%, 80%, 100% { transform: scale(0); opacity: 0.5; }
          40% { transform: scale(1); opacity: 1; }
        }
      `}</style>
    </>
  );
}
