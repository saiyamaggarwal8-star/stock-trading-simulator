import React, { useState, useRef, useEffect } from 'react';
import { createPortal } from 'react-dom';
import { MessageSquare, X, Send, Sparkles, User, Minus, Paperclip } from 'lucide-react';
import axios from 'axios';

export default function ChatBot({ username }) {
  const [isOpen, setIsOpen] = useState(false);
  const [isMinimized, setIsMinimized] = useState(false);
  const [messages, setMessages] = useState([
    { id: 1, sender: 'nova', text: `Hi ${username || 'Trader'}! I'm Nova. How can I help you dominate the market today?`, time: new Date() }
  ]);
  const [inputText, setInputText] = useState('');
  const [selectedImage, setSelectedImage] = useState(null);
  const [imagePreview, setImagePreview] = useState(null);
  const [isTyping, setIsTyping] = useState(false);
  
  const messagesEndRef = useRef(null);
  const fileInputRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    if (isOpen && !isMinimized) {
      scrollToBottom();
    }
  }, [messages, isOpen, isMinimized, isTyping]);

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setSelectedImage(file);
      const reader = new FileReader();
      reader.onloadend = () => {
        setImagePreview(reader.result);
      };
      reader.readAsDataURL(file);
    }
  };

  const removeImage = () => {
    setSelectedImage(null);
    setImagePreview(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  };

  const handleSend = async (e) => {
    e?.preventDefault();
    if (!inputText.trim() && !selectedImage) return;

    const userMsg = {
      id: Date.now(),
      sender: 'user',
      text: inputText || "What do you see in this image?",
      image: imagePreview,
      time: new Date()
    };

    setMessages(prev => [...prev, userMsg]);
    
    const formData = new FormData();
    formData.append('message', userMsg.text);
    if (selectedImage) {
      formData.append('image', selectedImage);
    }

    setInputText('');
    removeImage();
    setIsTyping(true);

    try {
      const response = await axios.post('/api/chat', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      
      const aiMsg = {
        id: Date.now() + 1,
        sender: 'nova',
        text: response.data.reply,
        time: new Date()
      };
      setMessages(prev => [...prev, aiMsg]);
    } catch (error) {
      console.error('Error talking to Nova:', error);
      const errorMsg = {
        id: Date.now() + 1,
        sender: 'nova',
        text: "I'm having trouble connecting right now. Please check if your Gemini API key is configured or try again later.",
        time: new Date()
      };
      setMessages(prev => [...prev, errorMsg]);
    } finally {
      setIsTyping(false);
    }
  };

  const formatTime = (date) => {
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  const toggleBtn = (
    <button
      onClick={() => { setIsOpen(!isOpen); setIsMinimized(false); }}
      style={{
        display: 'flex', alignItems: 'center', gap: '0.4rem',
        padding: '0.45rem 1rem', borderRadius: '8px',
        background: 'linear-gradient(135deg, #8b5cf6, #d946ef)',
        border: 'none', color: 'white', cursor: 'pointer',
        fontSize: '0.78rem', fontWeight: 700, letterSpacing: '0.3px',
        boxShadow: '0 4px 15px rgba(139,92,246,0.3)'
      }}
    >
      <Sparkles size={16} /> Nova AI
    </button>
  );

  return (
    <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
      {toggleBtn}

      {isOpen && createPortal(
      <div style={{
        position: 'fixed', top: '80px', right: '2rem', zIndex: 99999,
        width: '360px', height: isMinimized ? '60px' : '550px',
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
                Chat powered by Gemini
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
                      boxShadow: '0 4px 15px rgba(0,0,0,0.1)',
                      wordBreak: 'break-word',
                      whiteSpace: 'pre-wrap'
                    }}>
                      {msg.image && (
                        <div style={{ marginBottom: msg.text ? '8px' : '0' }}>
                          <img src={msg.image} alt="User Upload" style={{ maxWidth: '100%', borderRadius: '8px', maxHeight: '150px', objectFit: 'contain' }} />
                        </div>
                      )}
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
            <div style={{ background: '#0f172a', borderTop: '1px solid rgba(255,255,255,0.05)' }}>
              
              {/* Image Preview above input */}
              {imagePreview && (
                <div style={{ padding: '0.5rem 1rem 0', position: 'relative', display: 'inline-block' }}>
                  <div style={{ position: 'relative' }}>
                    <img 
                      src={imagePreview} 
                      alt="Preview" 
                      style={{ height: '60px', borderRadius: '8px', border: '1px solid rgba(255,255,255,0.2)' }} 
                    />
                    <button 
                      onClick={removeImage}
                      style={{ 
                        position: 'absolute', top: '-6px', right: '-6px', 
                        background: '#ef4444', color: 'white', border: 'none', 
                        borderRadius: '50%', width: '18px', height: '18px', 
                        display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer' 
                      }}
                    >
                      <X size={12} />
                    </button>
                  </div>
                </div>
              )}

              <form onSubmit={handleSend} style={{
                padding: '1rem', 
                display: 'flex', gap: '0.5rem', alignItems: 'center'
              }}>
                <div style={{ display: 'flex', alignItems: 'center', background: 'rgba(255,255,255,0.03)', border: '1px solid rgba(255,255,255,0.1)', borderRadius: '24px', flex: 1, padding: '0 0.5rem' }}>
                  <button 
                    type="button" 
                    onClick={() => fileInputRef.current?.click()}
                    style={{ background: 'none', border: 'none', color: '#94a3b8', cursor: 'pointer', padding: '0.5rem', display: 'flex', alignItems: 'center' }}
                    title="Attach Image"
                  >
                    <Paperclip size={18} />
                  </button>
                  <input 
                    type="file" 
                    accept="image/*" 
                    ref={fileInputRef} 
                    style={{ display: 'none' }} 
                    onChange={handleImageChange}
                  />
                  <input
                    type="text"
                    value={inputText}
                    onChange={(e) => setInputText(e.target.value)}
                    placeholder="Ask Nova anything..."
                    style={{
                      flex: 1, padding: '0.75rem 0.5rem',
                      background: 'transparent', border: 'none',
                      color: 'white', fontSize: '0.85rem',
                      outline: 'none'
                    }}
                  />
                </div>
                
                <button
                  type="submit"
                  disabled={(!inputText.trim() && !selectedImage) || isTyping}
                  style={{
                    width: '42px', height: '42px', borderRadius: '50%', flexShrink: 0,
                    background: ((inputText.trim() || selectedImage) && !isTyping) ? '#3b82f6' : 'rgba(255,255,255,0.1)',
                    border: 'none', color: 'white', 
                    cursor: ((inputText.trim() || selectedImage) && !isTyping) ? 'pointer' : 'not-allowed',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    transition: 'background 0.2s'
                  }}
                >
                  <Send size={16} style={{ marginLeft: '2px' }} />
                </button>
              </form>
            </div>
          </>
        )}
      </div>
      , document.body)}
      <style>{`
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
    </div>
  );
}
