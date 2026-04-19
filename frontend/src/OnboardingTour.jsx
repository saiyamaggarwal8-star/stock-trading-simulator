import React, { useState, useEffect, useRef } from 'react';
import { ChevronRight, ChevronLeft, X, Sparkles } from 'lucide-react';

// Tour steps: each step targets an element by ID and shows a message
const TOUR_STEPS = [
  {
    targetId: null, // No target — welcome screen
    position: 'center',
    title: "Welcome to FinNova! 🎉",
    message: "Hey there, future trader! I'm Nova, your AI trading guide. I'll walk you through the dashboard so you're ready to make your first trade. It only takes 2 minutes!",
    emoji: "🤖",
  },
  {
    targetId: 'tour-header',
    position: 'bottom',
    title: "The Control Bar",
    message: "This is your command center. Here you can see your live account balance, click 📚 Learn to study stocks anytime, and log out. Your balance starts at ₹1,00,000 — use it wisely!",
    emoji: "🎮",
  },
  {
    targetId: 'tour-market-list',
    position: 'right',
    title: "NSE Market Stocks",
    message: "This panel shows all available NSE stocks with live prices updating every few seconds. Green arrows mean the price is going UP 📈, red means DOWN 📉. Click any stock to open its chart!",
    emoji: "📊",
  },
  {
    targetId: 'tour-portfolio',
    position: 'right',
    title: "Your Portfolio",
    message: "After you buy stocks, they appear here. You can see how many shares you own and the total value. Right now it's empty — let's change that soon!",
    emoji: "💼",
  },
  {
    targetId: 'tour-stock-header',
    position: 'bottom',
    title: "Selected Stock Info",
    message: "This shows the currently selected stock — its name, current price, and how much it has changed today. The blinking green dot means it's updating live!",
    emoji: "📍",
  },
  {
    targetId: 'tour-chart',
    position: 'center',
    title: "The Price Chart",
    message: "This is a candlestick chart — the most important tool for traders! Each candle shows the Open, High, Low, and Close price. 🟢 Green candles = price went UP. 🔴 Red candles = price went DOWN. Hover any candle for details!",
    emoji: "🕯️",
  },
  {
    targetId: 'tour-stats',
    position: 'top',
    title: "Today's Market Stats",
    message: "These are today's key stats for the selected stock — the opening price, the highest and lowest points of the day, and trading volume. High volume means lots of interest in the stock!",
    emoji: "📈",
  },
  {
    targetId: 'tour-indicators',
    position: 'top',
    title: "Technical Indicators",
    message: "These are professional trading tools! Click SMA or EMA to see trend lines on the chart. Click RSI to see if a stock is overbought or oversold. Click 'Scan Patterns' to detect chart patterns automatically!",
    emoji: "🔬",
  },
  {
    targetId: 'tour-order-panel',
    position: 'left',
    title: "Place Your Orders Here",
    message: "This is the trading panel — your most important tool! Enter the number of shares you want, then click BUY (green) or SELL (red). Remember: you can only sell stocks you already own!",
    emoji: "🛍️",
  },
  {
    targetId: 'tour-cheatsheet',
    position: 'left',
    title: "Pattern Cheat Sheet & Trades",
    message: "Toggle between your Recent Trades history and the Pattern Cheat Sheet. The cheat sheet explains what each chart pattern means so you know when to buy or sell!",
    emoji: "📋",
  },
  {
    targetId: null,
    position: 'center',
    title: "You're Ready to Trade! 🚀",
    message: "That's the full tour! Here's your 3-step beginner plan:\n1️⃣ Pick a stock from the left panel\n2️⃣ Check the RSI indicator (buy if below 30!)\n3️⃣ Enter quantity and hit BUY\n\nGood luck, trader! Remember — practice makes perfect. And I'm always here in the 📚 Learn section!",
    emoji: "🏆",
  },
];

function getElementRect(id) {
  if (!id) return null;
  const el = document.getElementById(id);
  if (!el) return null;
  return el.getBoundingClientRect();
}

export default function OnboardingTour({ username, onClose, onOpenLearning }) {
  const [step, setStep] = useState(0);
  const [rect, setRect] = useState(null);
  const [visible, setVisible] = useState(false);
  const [typing, setTyping] = useState(true);
  const [displayedText, setDisplayedText] = useState('');
  const typingRef = useRef(null);

  const current = TOUR_STEPS[step];

  // Measure target element position
  useEffect(() => {
    setRect(null);
    setTyping(true);
    setDisplayedText('');

    const measure = () => {
      const r = getElementRect(current.targetId);
      setRect(r);
      setVisible(true);

      // Scroll element into view if needed
      if (current.targetId) {
        const el = document.getElementById(current.targetId);
        if (el) el.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
      }
    };

    const t = setTimeout(measure, 80);
    return () => clearTimeout(t);
  }, [step]);

  // Typewriter effect
  useEffect(() => {
    if (!typing) return;
    const fullText = current.message;
    let i = 0;
    setDisplayedText('');

    typingRef.current = setInterval(() => {
      i++;
      setDisplayedText(fullText.slice(0, i));
      if (i >= fullText.length) {
        clearInterval(typingRef.current);
        setTyping(false);
      }
    }, 18);

    return () => clearInterval(typingRef.current);
  }, [step, typing]);

  const goNext = () => {
    if (step < TOUR_STEPS.length - 1) setStep(s => s + 1);
    else onClose();
  };
  const goPrev = () => { if (step > 0) setStep(s => s - 1); };
  const skipAll = () => typing ? (clearInterval(typingRef.current), setDisplayedText(current.message), setTyping(false)) : onClose();

  // Compute tooltip position — always clamped within viewport
  const getTooltipStyle = () => {
    const W = window.innerWidth;
    const H = window.innerHeight;
    const TW = 380;
    const TH = 280; // estimated tooltip height
    const padding = 16;

    if (!rect || current.position === 'center') {
      return {
        position: 'fixed',
        left: '50%', top: '50%',
        transform: 'translate(-50%, -50%)',
        width: TW,
        zIndex: 10002,
      };
    }

    const mid = { x: rect.left + rect.width / 2, y: rect.top + rect.height / 2 };
    let top, left;

    if (current.position === 'bottom') {
      top = rect.bottom + padding;
      left = mid.x - TW / 2;
      // Flip to top if it would go off bottom
      if (top + TH > H - padding) top = rect.top - TH - padding;
    } else if (current.position === 'top') {
      top = rect.top - TH - padding;
      left = mid.x - TW / 2;
      // Flip to bottom if it would go off top
      if (top < padding) top = rect.bottom + padding;
    } else if (current.position === 'right') {
      top = mid.y - TH / 2;
      left = rect.right + padding;
      // Flip to bottom if not enough space on right
      if (left + TW > W - padding) { left = mid.x - TW / 2; top = rect.bottom + padding; }
    } else if (current.position === 'left') {
      top = mid.y - TH / 2;
      left = rect.left - TW - padding;
      // Flip to bottom if not enough space on left
      if (left < padding) { left = mid.x - TW / 2; top = rect.bottom + padding; }
    }

    // Final clamp to keep entirely within viewport
    left = Math.min(Math.max(left, padding), W - TW - padding);
    top  = Math.min(Math.max(top,  padding), H - TH - padding);

    return { position: 'fixed', top, left, width: TW, zIndex: 10002 };
  };

  const progress = ((step + 1) / TOUR_STEPS.length) * 100;

  return (
    <>
      {/* Dark overlay with spotlight hole */}
      <div style={{ position: 'fixed', inset: 0, zIndex: 9998, pointerEvents: 'none' }}>
        {rect ? (
          <svg width="100%" height="100%" style={{ position: 'absolute', inset: 0 }}>
            <defs>
              <mask id="spotlight-mask">
                <rect width="100%" height="100%" fill="white" />
                <rect
                  x={rect.left - 8} y={rect.top - 8}
                  width={rect.width + 16} height={rect.height + 16}
                  rx={12} fill="black"
                />
              </mask>
            </defs>
            <rect width="100%" height="100%" fill="rgba(0,0,8,0.78)" mask="url(#spotlight-mask)" />
          </svg>
        ) : current.targetId === null ? (
          <div style={{ position: 'absolute', inset: 0, background: 'rgba(0,0,8,0.82)' }} />
        ) : null}

        {/* Glowing border around highlighted element */}
        {rect && (
          <div
            style={{
              position: 'fixed',
              left: rect.left - 8, top: rect.top - 8,
              width: rect.width + 16, height: rect.height + 16,
              borderRadius: 12,
              border: '2px solid rgba(99, 179, 237, 0.8)',
              boxShadow: '0 0 0 4px rgba(99, 179, 237, 0.2), 0 0 30px rgba(99, 179, 237, 0.4)',
              pointerEvents: 'none',
              animation: 'tour-pulse 2s ease-in-out infinite',
            }}
          />
        )}
      </div>

      {/* Click blocker for the overlay */}
      <div
        style={{ position: 'fixed', inset: 0, zIndex: 9999, cursor: 'default' }}
        onClick={e => e.stopPropagation()}
      />

      {/* Tooltip card */}
      <div style={{ ...getTooltipStyle(), opacity: visible ? 1 : 0, transition: 'opacity 0.25s ease' }}>
        <div style={{
          background: 'linear-gradient(145deg, #0f172a, #1e293b)',
          border: '1px solid rgba(99, 179, 237, 0.35)',
          borderRadius: 20,
          padding: '1.5rem',
          boxShadow: '0 25px 60px rgba(0,0,0,0.7), 0 0 40px rgba(59,130,246,0.15)',
          fontFamily: "'Inter', -apple-system, sans-serif",
        }}>
          {/* Header row */}
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
              {/* AI mascot avatar */}
              <div style={{
                width: 44, height: 44, borderRadius: '50%', flexShrink: 0,
                background: 'linear-gradient(135deg, #3b82f6, #8b5cf6)',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                fontSize: '1.3rem',
                boxShadow: '0 0 16px rgba(139,92,246,0.5)',
                animation: 'tour-float 3s ease-in-out infinite',
              }}>
                {current.emoji}
              </div>
              <div>
                <div style={{ fontSize: '0.65rem', color: '#64748b', fontWeight: 700, letterSpacing: '1px' }}>
                  NOVA · AI GUIDE
                </div>
                <div style={{ fontSize: '0.95rem', fontWeight: 800, color: '#e2e8f0' }}>
                  {current.title}
                </div>
              </div>
            </div>
            <button
              onClick={onClose}
              style={{
                background: 'rgba(255,255,255,0.06)', border: '1px solid rgba(255,255,255,0.1)',
                borderRadius: 8, cursor: 'pointer', color: '#64748b',
                width: 30, height: 30, display: 'flex', alignItems: 'center', justifyContent: 'center',
                flexShrink: 0,
              }}
            >
              <X size={14} />
            </button>
          </div>

          {/* Progress bar */}
          <div style={{ height: 3, background: 'rgba(255,255,255,0.06)', borderRadius: 2, marginBottom: '1rem' }}>
            <div style={{
              height: '100%', borderRadius: 2,
              width: `${progress}%`,
              background: 'linear-gradient(90deg, #3b82f6, #8b5cf6)',
              transition: 'width 0.4s ease',
            }} />
          </div>

          {/* Message */}
          <div style={{
            fontSize: '0.85rem', color: '#94a3b8', lineHeight: 1.75,
            minHeight: 80, whiteSpace: 'pre-line',
            background: 'rgba(255,255,255,0.02)', borderRadius: 10,
            padding: '0.85rem 1rem', marginBottom: '1.25rem',
            border: '1px solid rgba(255,255,255,0.05)',
          }}>
            {displayedText}
            {typing && <span style={{ display: 'inline-block', width: 2, height: '1em', background: '#8b5cf6', marginLeft: 2, animation: 'tour-blink 0.7s steps(1) infinite', verticalAlign: 'text-bottom' }} />}
          </div>

          {/* Navigation */}
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <button
              onClick={goPrev}
              disabled={step === 0}
              style={{
                display: 'flex', alignItems: 'center', gap: 4,
                padding: '0.5rem 1rem', borderRadius: 8,
                background: step === 0 ? 'rgba(255,255,255,0.03)' : 'rgba(255,255,255,0.07)',
                border: '1px solid rgba(255,255,255,0.08)',
                color: step === 0 ? '#334155' : '#94a3b8',
                cursor: step === 0 ? 'not-allowed' : 'pointer',
                fontSize: '0.8rem', fontWeight: 600,
              }}
            >
              <ChevronLeft size={14} /> Back
            </button>

            <span style={{ fontSize: '0.7rem', color: '#475569' }}>
              {step + 1} / {TOUR_STEPS.length}
            </span>

            <div style={{ display: 'flex', gap: 8 }}>
              {typing ? (
                <button
                  onClick={() => { clearInterval(typingRef.current); setDisplayedText(current.message); setTyping(false); }}
                  style={{
                    padding: '0.5rem 0.9rem', borderRadius: 8,
                    background: 'rgba(255,255,255,0.06)', border: '1px solid rgba(255,255,255,0.1)',
                    color: '#64748b', cursor: 'pointer', fontSize: '0.75rem', fontWeight: 600,
                  }}
                >
                  Fast Forward ⏭
                </button>
              ) : (
                step < TOUR_STEPS.length - 1 && (
                  <button
                    onClick={() => { onClose(); onOpenLearning(); }}
                    style={{
                      padding: '0.5rem 0.9rem', borderRadius: 8,
                      background: 'rgba(255,255,255,0.06)', border: '1px solid rgba(255,255,255,0.1)',
                      color: '#94a3b8', cursor: 'pointer', fontSize: '0.75rem', fontWeight: 600,
                    }}
                  >
                    Skip to Learn ⏭
                  </button>
                )
              )}
              <button
                onClick={() => {
                  if (step === TOUR_STEPS.length - 1) {
                    onClose();
                    onOpenLearning();
                  } else {
                    goNext();
                  }
                }}
                style={{
                  display: 'flex', alignItems: 'center', gap: 4,
                  padding: '0.5rem 1.2rem', borderRadius: 8,
                  background: step === TOUR_STEPS.length - 1
                    ? 'linear-gradient(135deg, #10b981, #3b82f6)'
                    : 'linear-gradient(135deg, #3b82f6, #8b5cf6)',
                  border: 'none', color: 'white',
                  cursor: 'pointer', fontSize: '0.8rem', fontWeight: 700,
                  boxShadow: '0 4px 15px rgba(59,130,246,0.35)',
                }}
              >
                {step === TOUR_STEPS.length - 1 ? '📚 Go to Learning Center' : <>Next <ChevronRight size={14} /></>}
              </button>
            </div>
          </div>
        </div>
      </div>

      <style>{`
        @keyframes tour-pulse {
          0%, 100% { box-shadow: 0 0 0 4px rgba(99,179,237,0.2), 0 0 30px rgba(99,179,237,0.4); }
          50% { box-shadow: 0 0 0 8px rgba(99,179,237,0.1), 0 0 50px rgba(99,179,237,0.6); }
        }
        @keyframes tour-float {
          0%, 100% { transform: translateY(0); }
          50% { transform: translateY(-4px); }
        }
        @keyframes tour-blink {
          0%, 100% { opacity: 1; }
          50% { opacity: 0; }
        }
      `}</style>
    </>
  );
}
