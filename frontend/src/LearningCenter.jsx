import React, { useState } from 'react';
import { X, ChevronRight, ChevronLeft, BookOpen, TrendingUp, TrendingDown, BarChart3, Activity, Zap, Target, AlertTriangle, CheckCircle } from 'lucide-react';

const chapters = [
  {
    id: 'intro',
    icon: <BookOpen size={22} />,
    title: 'What is a Stock?',
    color: '#3b82f6',
    sections: [
      {
        heading: '📈 Stocks Explained Simply',
        content: `A stock is a tiny piece of ownership in a company. When you buy 1 share of Reliance Industries, you literally own a small fraction of that company.

Companies sell stocks to raise money for growth. In return, you (the shareholder) may earn:
• **Dividends** — a share of the company's profit paid to you periodically
• **Capital gains** — profit when the stock price rises and you sell higher than you bought`
      },
      {
        heading: '🏛️ NSE & BSE',
        content: `India has two major stock exchanges:
• **NSE (National Stock Exchange)** — where FinNova simulates trades. Home of the NIFTY 50 index.
• **BSE (Bombay Stock Exchange)** — Asia's oldest exchange. Home of the SENSEX index.

Stock prices change every second during market hours (9:15 AM – 3:30 PM IST) based on supply and demand.`
      },
      {
        heading: '🔑 Key Terms',
        content: `• **Share / Stock** — A unit of ownership in a company
• **Portfolio** — All the stocks you currently own
• **Bull Market** — Prices rising overall (good times 🐂)
• **Bear Market** — Prices falling overall (tough times 🐻)
• **Equity** — Another word for stocks
• **Market Cap** — Total value of all shares of a company
• **P/E Ratio** — Price÷Earnings. How expensive a stock is relative to profits`
      }
    ]
  },
  {
    id: 'buy-sell',
    icon: <Target size={22} />,
    title: 'When to Buy & Sell',
    color: '#10b981',
    sections: [
      {
        heading: '🟢 When to BUY',
        content: `**Buy signals to look for:**
• **Price dips / corrections** — Buy when quality stocks fall 10–20% temporarily (not permanently)
• **RSI below 30** — Stock is oversold, likely to bounce back up
• **Price above SMA/EMA** — Upward trend confirmed
• **Earnings beat** — Company reported better profits than expected
• **Bullish patterns** — Triple Bottom, Falling Wedge (see Pattern Cheat Sheet)
• **Strong sector performance** — When the whole industry is growing

**Golden rule: Buy when others are fearful (panic selling), but only in fundamentally strong companies.**`
      },
      {
        heading: '🔴 When to SELL',
        content: `**Sell signals to look for:**
• **RSI above 70** — Stock is overbought, likely to fall
• **Hit your target price** — Always set a profit target before buying
• **Stop-loss triggered** — To limit losses, sell if price falls 5–10% below buy price
• **Fundamental change** — Bad earnings, management scandal, or the business model broke
• **Bearish patterns** — Descending Triangle (see Cheat Sheet)
• **Need the money** — Never invest money you'll need within 6 months

**Never sell in panic. Decide your exit BEFORE you enter a trade.**`
      },
      {
        heading: '⚠️ Common Mistakes',
        content: `❌ **FOMO buying** — Buying just because a stock went up a lot already
❌ **Holding losers forever** — Hoping a bad stock will "come back"
❌ **No stop-loss** — Letting small losses become huge ones
❌ **Overtrading** — Buying and selling too frequently, losing to fees
❌ **Putting all eggs in one basket** — Diversify across 5–10 different stocks/sectors
❌ **Following tips blindly** — Always research before buying`
      }
    ]
  },
  {
    id: 'indicators',
    icon: <Activity size={22} />,
    title: 'Technical Indicators',
    color: '#a78bfa',
    sections: [
      {
        heading: '📊 SMA — Simple Moving Average',
        content: `SMA is the average closing price of a stock over the last N days (14 days in FinNova).

**How to use it:**
• **Price > SMA** → Stock is in an UPTREND → Potential BUY signal 🟢
• **Price < SMA** → Stock is in a DOWNTREND → Consider SELLING or staying out 🔴
• **Price crosses above SMA** → Bullish crossover — strong buy signal
• **Price crosses below SMA** → Bearish crossover — sell signal

SMA smooths out noise so you can see the bigger trend.`
      },
      {
        heading: '📊 EMA — Exponential Moving Average',
        content: `EMA is like SMA but gives MORE weight to recent prices, making it more responsive.

**How to use it:**
• Works the same way as SMA (above = uptrend, below = downtrend)
• **EMA reacts faster** to price changes than SMA
• Traders often watch for **EMA crossing above SMA** as a strong buy signal ("Golden Cross")
• **EMA crossing below SMA** = strong sell signal ("Death Cross")

Use in FinNova: Click the EMA button to see the EMA line on the chart.`
      },
      {
        heading: '📊 RSI — Relative Strength Index',
        content: `RSI measures whether a stock is overbought or oversold, on a 0–100 scale.

**Reading RSI:**
• **RSI > 70** = OVERBOUGHT 🔴 — Stock may be due for a pullback (consider selling)
• **RSI < 30** = OVERSOLD 🟢 — Stock may be due for a bounce (consider buying)
• **RSI 30–70** = NEUTRAL — Trend is intact, neither extreme

**Important:** RSI alone isn't enough. A stock can stay overbought/oversold for weeks during strong trends. Always combine with other signals.

Use in FinNova: Click the RSI button — you'll see the RSI gauge bar below the chart.`
      }
    ]
  },
  {
    id: 'patterns',
    icon: <BarChart3 size={22} />,
    title: 'Chart Patterns',
    color: '#f59e0b',
    sections: [
      {
        heading: '🐂 Bullish Patterns (Buy Signals)',
        content: `**Triple Bottom**
Price hits the same low 3 times but fails to go lower. Strong support found.
→ Signal: BUY when price breaks above the neckline (highs between the 3 bottoms)

**Falling Wedge**
Price makes lower highs AND lower lows, but the lines converge (narrow). Sellers are losing power.
→ Signal: BUY when price breaks above the upper trendline with volume

**Cup & Handle**
Price dips gradually in a U-shape (cup), then small pullback (handle), then breakout.
→ Signal: BUY at the handle breakout`
      },
      {
        heading: '🐻 Bearish Patterns (Sell Signals)',
        content: `**Descending Triangle**
Price makes lower highs (downward resistance) with flat support. Bears are in control.
→ Signal: SELL/SHORT when price breaks below the flat support line

**Head & Shoulders**
Three peaks — left shoulder, higher head, right shoulder. Classic reversal.
→ Signal: SELL when price breaks below the "neckline" connecting the two valleys

**Double Top**
Price hits the same high twice and fails. Strong resistance found.
→ Signal: SELL when price breaks below the valley between the two tops`
      },
      {
        heading: '⚖️ Neutral Patterns',
        content: `**Symmetrical Triangle**
Price makes lower highs AND higher lows — converging toward a point. Breakout imminent.
→ Signal: Wait for the breakout direction. Trade in whichever direction it breaks.

**How to scan patterns in FinNova:**
1. Select any stock from the left panel
2. Click the **"Scan Patterns"** button in the Technical Indicators section
3. See detected patterns highlighted on the chart
4. Check the "Pattern Cheat Sheet" in the right panel for explanations`
      }
    ]
  },
  {
    id: 'app-guide',
    icon: <Zap size={22} />,
    title: 'How to Use FinNova',
    color: '#06b6d4',
    sections: [
      {
        heading: '🗺️ App Layout',
        content: `**Left Sidebar:**
• **NSE Market** — List of all stocks. Click any to view its chart and data.
• **My Portfolio** — Shows stocks you currently own and their value.
• **Buying Power** — Your available cash balance.

**Main Center:**
• **Price Chart** — Candlestick chart updating every 10 seconds.
• **Today's Stats** — Open, High, Low, Volume for the selected stock.
• **Technical Indicators** — SMA, EMA, RSI overlays + Pattern Scanner.

**Right Panel:**
• **Place Order** — Enter quantity and buy/sell.
• **Pattern Cheat Sheet / Recent Trades** — Toggle between them.`
      },
      {
        heading: '🛍️ How to Place Your First Trade',
        content: `**Step 1:** Click a stock from the left sidebar (e.g., RELIANCE)
**Step 2:** Study the chart — is it going up or down?
**Step 3:** Check the indicators — click SMA for trend confirmation
**Step 4:** In the right panel, enter the **Quantity** (number of shares)
**Step 5:** Click **BUY** (green) to buy, or **SELL** (red) to sell

⚠️ You need enough balance to buy. Start with smaller quantities.
⚠️ You can only SELL stocks you already own in your portfolio.

After trading, your portfolio updates immediately on the left sidebar.`
      },
      {
        heading: '🎯 Strategy for Beginners',
        content: `Here's a simple beginner strategy to get started in FinNova:

**1. Pick 3 stocks** from different sectors (e.g., RELIANCE, TCS, HDFC)
**2. Watch them for a few minutes** — observe how prices move
**3. Use RSI** — look for any stock with RSI below 35 (oversold)
**4. Confirm with SMA** — check if price is near or above SMA
**5. Buy a small quantity** (50–100 shares) as a test trade
**6. Set a mental profit target** of ~3–5% and a stop at -2%
**7. Watch your portfolio** grow (or learn from mistakes!)

FinNova uses real NSE price data for simulation — it's as realistic as it gets without real money. Practice here before investing real money!`
      }
    ]
  },
  {
    id: 'risk',
    icon: <AlertTriangle size={22} />,
    title: 'Risk Management',
    color: '#ef4444',
    sections: [
      {
        heading: '🛡️ The Golden Rules of Risk',
        content: `**Rule 1 — Never risk more than 2% of capital on a single trade**
If you have ₹1,00,000, don't put more than ₹2,000 at risk per trade.

**Rule 2 — Always use a Stop-Loss**
Decide BEFORE buying: "I'll sell if this falls by X%." Stick to it.

**Rule 3 — Diversify**
Don't put all money in one stock or sector. Spread across 5–10 positions.

**Rule 4 — Position Sizing**
Buying more when you're confident is okay. But even your "best" trade can go wrong.

**Rule 5 — Cut losses, let profits run**
Small losses are normal. Big profits come from holding winners longer.`
      },
      {
        heading: '🧮 The Risk:Reward Ratio',
        content: `Before every trade, calculate Risk:Reward.

**Example:**
• You buy RELIANCE at ₹2,500
• Stop-loss at ₹2,450 (risk = ₹50 per share)
• Target at ₹2,650 (reward = ₹150 per share)
• Risk:Reward = 1:3 ✅ (You risk ₹1 to earn ₹3 — excellent!)

**Minimum acceptable ratio: 1:2**
Never take a trade where you risk ₹100 to potentially make ₹50. The math doesn't work out long-term.

In FinNova, practice calculating this BEFORE every trade!`
      },
      {
        heading: '🧘 The Psychology of Trading',
        content: `The biggest enemy of a trader is emotion.

**Fear** → Makes you sell winning trades too early
**Greed** → Makes you hold losing trades hoping they "come back"
**Overconfidence** → After a few wins, you take too-large positions

**Tips to stay disciplined:**
✅ Always have a plan BEFORE trading
✅ Never trade when emotional (angry, excited, or panicking)
✅ Keep a trading journal — write why you entered and exited each trade
✅ Accept that losses are part of the game — even professionals lose 40% of trades
✅ Focus on the process, not individual profits/losses`
      }
    ]
  }
];

export default function LearningCenter({ onClose }) {
  const [activeChapter, setActiveChapter] = useState(0);
  const [activeSection, setActiveSection] = useState(0);
  const chapter = chapters[activeChapter];
  const section = chapter.sections[activeSection];

  const goNext = () => {
    if (activeSection < chapter.sections.length - 1) {
      setActiveSection(s => s + 1);
    } else if (activeChapter < chapters.length - 1) {
      setActiveChapter(c => c + 1);
      setActiveSection(0);
    }
  };

  const goPrev = () => {
    if (activeSection > 0) {
      setActiveSection(s => s - 1);
    } else if (activeChapter > 0) {
      setActiveChapter(c => c - 1);
      setActiveSection(chapters[activeChapter - 1].sections.length - 1);
    }
  };

  const isFirst = activeChapter === 0 && activeSection === 0;
  const isLast = activeChapter === chapters.length - 1 && activeSection === chapter.sections.length - 1;

  const totalSections = chapters.reduce((sum, c) => sum + c.sections.length, 0);
  const completedSections = chapters.slice(0, activeChapter).reduce((sum, c) => sum + c.sections.length, 0) + activeSection;

  const renderContent = (text) => {
    return text.split('\n').map((line, i) => {
      if (!line.trim()) return <br key={i} />;
      // Bold
      const parts = line.split(/(\*\*[^*]+\*\*)/g);
      return (
        <p key={i} style={{ margin: '0.3rem 0', lineHeight: 1.7 }}>
          {parts.map((part, j) =>
            part.startsWith('**') && part.endsWith('**')
              ? <strong key={j} style={{ color: '#e2e8f0' }}>{part.slice(2, -2)}</strong>
              : part
          )}
        </p>
      );
    });
  };

  return (
    <div style={{
      position: 'fixed', inset: 0, zIndex: 9999,
      background: 'rgba(5, 8, 20, 0.97)',
      backdropFilter: 'blur(20px)',
      display: 'flex', flexDirection: 'column',
      fontFamily: "'Inter', -apple-system, sans-serif"
    }}>
      {/* Header */}
      <div style={{
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        padding: '1.25rem 2rem',
        borderBottom: '1px solid rgba(255,255,255,0.08)',
        background: 'rgba(255,255,255,0.02)'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <div style={{
            width: 36, height: 36, borderRadius: '10px',
            background: 'linear-gradient(135deg, #3b82f6, #8b5cf6)',
            display: 'flex', alignItems: 'center', justifyContent: 'center'
          }}>
            <BookOpen size={18} color="white" />
          </div>
          <div>
            <div style={{ fontWeight: 700, fontSize: '1.1rem', color: '#f1f5f9' }}>FinNova Learning Center</div>
            <div style={{ fontSize: '0.72rem', color: '#64748b' }}>Master stock trading step by step</div>
          </div>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <div style={{ fontSize: '0.75rem', color: '#64748b' }}>
            {completedSections + 1} / {totalSections} sections
          </div>
          <button onClick={onClose} style={{
            background: 'rgba(255,255,255,0.06)', border: '1px solid rgba(255,255,255,0.1)',
            borderRadius: '8px', cursor: 'pointer', color: '#94a3b8',
            width: 36, height: 36, display: 'flex', alignItems: 'center', justifyContent: 'center'
          }}>
            <X size={18} />
          </button>
        </div>
      </div>

      {/* Progress bar */}
      <div style={{ height: 3, background: 'rgba(255,255,255,0.06)' }}>
        <div style={{
          height: '100%',
          width: `${((completedSections + 1) / totalSections) * 100}%`,
          background: `linear-gradient(90deg, ${chapter.color}, #8b5cf6)`,
          transition: 'width 0.4s ease'
        }} />
      </div>

      <div style={{ display: 'flex', flex: 1, overflow: 'hidden' }}>
        {/* Chapter Navigation */}
        <div style={{
          width: 260, flexShrink: 0,
          borderRight: '1px solid rgba(255,255,255,0.06)',
          overflowY: 'auto', padding: '1.5rem 1rem',
          background: 'rgba(255,255,255,0.01)'
        }}>
          <div style={{ fontSize: '0.65rem', fontWeight: 700, color: '#475569', letterSpacing: '1.5px', marginBottom: '0.75rem', paddingLeft: '0.5rem' }}>
            CHAPTERS
          </div>
          {chapters.map((ch, ci) => (
            <button
              key={ch.id}
              onClick={() => { setActiveChapter(ci); setActiveSection(0); }}
              style={{
                width: '100%', textAlign: 'left', padding: '0.75rem',
                borderRadius: '10px', marginBottom: '0.25rem',
                background: activeChapter === ci ? `${ch.color}18` : 'transparent',
                border: activeChapter === ci ? `1px solid ${ch.color}40` : '1px solid transparent',
                cursor: 'pointer', transition: 'all 0.2s ease',
                display: 'flex', alignItems: 'center', gap: '0.75rem'
              }}
            >
              <div style={{
                width: 32, height: 32, borderRadius: '8px', flexShrink: 0,
                background: activeChapter === ci ? `${ch.color}30` : 'rgba(255,255,255,0.05)',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                color: activeChapter === ci ? ch.color : '#475569',
                transition: 'all 0.2s'
              }}>
                {ch.icon}
              </div>
              <div>
                <div style={{
                  fontSize: '0.8rem', fontWeight: activeChapter === ci ? 700 : 500,
                  color: activeChapter === ci ? '#f1f5f9' : '#64748b',
                  transition: 'color 0.2s'
                }}>{ch.title}</div>
                <div style={{ fontSize: '0.65rem', color: '#475569' }}>{ch.sections.length} sections</div>
              </div>
            </button>
          ))}
        </div>

        {/* Section Sub-Nav */}
        <div style={{
          width: 200, flexShrink: 0,
          borderRight: '1px solid rgba(255,255,255,0.06)',
          overflowY: 'auto', padding: '1.5rem 0.75rem',
          background: 'rgba(255,255,255,0.01)'
        }}>
          <div style={{ fontSize: '0.65rem', fontWeight: 700, color: '#475569', letterSpacing: '1.5px', marginBottom: '0.75rem', paddingLeft: '0.25rem' }}>
            SECTIONS
          </div>
          {chapter.sections.map((sec, si) => (
            <button
              key={si}
              onClick={() => setActiveSection(si)}
              style={{
                width: '100%', textAlign: 'left', padding: '0.65rem 0.75rem',
                borderRadius: '8px', marginBottom: '0.2rem',
                background: activeSection === si ? `${chapter.color}20` : 'transparent',
                border: 'none', cursor: 'pointer', transition: 'all 0.2s ease',
                display: 'flex', alignItems: 'center', gap: '0.5rem'
              }}
            >
              <div style={{
                width: 6, height: 6, borderRadius: '50%', flexShrink: 0,
                background: activeSection === si ? chapter.color : '#334155',
                transition: 'background 0.2s'
              }} />
              <span style={{
                fontSize: '0.75rem', lineHeight: 1.3,
                color: activeSection === si ? '#e2e8f0' : '#475569',
                fontWeight: activeSection === si ? 600 : 400
              }}>{sec.heading.replace(/^[^\s]+\s/, '')}</span>
            </button>
          ))}
        </div>

        {/* Content Area */}
        <div style={{ flex: 1, overflowY: 'auto', padding: '2.5rem 3rem' }}>
          <div style={{ maxWidth: 760, margin: '0 auto' }}>
            {/* Chapter badge */}
            <div style={{
              display: 'inline-flex', alignItems: 'center', gap: '0.5rem',
              background: `${chapter.color}18`, border: `1px solid ${chapter.color}40`,
              borderRadius: '20px', padding: '0.3rem 0.9rem',
              marginBottom: '1rem'
            }}>
              <span style={{ color: chapter.color }}>{chapter.icon}</span>
              <span style={{ fontSize: '0.72rem', fontWeight: 700, color: chapter.color, letterSpacing: '0.5px' }}>
                {chapter.title.toUpperCase()}
              </span>
            </div>

            {/* Section heading */}
            <h2 style={{
              fontSize: '1.6rem', fontWeight: 800, color: '#f1f5f9',
              marginBottom: '1.5rem', lineHeight: 1.3
            }}>
              {section.heading}
            </h2>

            {/* Content */}
            <div style={{
              fontSize: '0.9rem', color: '#94a3b8', lineHeight: 1.8,
              background: 'rgba(255,255,255,0.02)',
              border: '1px solid rgba(255,255,255,0.06)',
              borderRadius: '16px', padding: '2rem'
            }}>
              {renderContent(section.content)}
            </div>

            {/* Navigation buttons */}
            <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '2rem', alignItems: 'center' }}>
              <button
                onClick={goPrev}
                disabled={isFirst}
                style={{
                  display: 'flex', alignItems: 'center', gap: '0.5rem',
                  padding: '0.7rem 1.5rem', borderRadius: '10px',
                  background: isFirst ? 'rgba(255,255,255,0.03)' : 'rgba(255,255,255,0.06)',
                  border: '1px solid rgba(255,255,255,0.08)',
                  color: isFirst ? '#334155' : '#94a3b8',
                  cursor: isFirst ? 'not-allowed' : 'pointer',
                  fontSize: '0.85rem', fontWeight: 600, transition: 'all 0.2s'
                }}
              >
                <ChevronLeft size={16} /> Previous
              </button>

              <div style={{ display: 'flex', gap: '0.4rem' }}>
                {chapter.sections.map((_, si) => (
                  <div key={si} style={{
                    width: si === activeSection ? 20 : 6, height: 6,
                    borderRadius: '3px',
                    background: si === activeSection ? chapter.color : '#1e293b',
                    transition: 'all 0.3s ease', cursor: 'pointer'
                  }} onClick={() => setActiveSection(si)} />
                ))}
              </div>

              {isLast ? (
                <button
                  onClick={onClose}
                  style={{
                    display: 'flex', alignItems: 'center', gap: '0.5rem',
                    padding: '0.7rem 1.5rem', borderRadius: '10px',
                    background: 'linear-gradient(135deg, #10b981, #3b82f6)',
                    border: 'none', color: 'white',
                    cursor: 'pointer', fontSize: '0.85rem', fontWeight: 700
                  }}
                >
                  <CheckCircle size={16} /> Start Trading!
                </button>
              ) : (
                <button
                  onClick={goNext}
                  style={{
                    display: 'flex', alignItems: 'center', gap: '0.5rem',
                    padding: '0.7rem 1.5rem', borderRadius: '10px',
                    background: `linear-gradient(135deg, ${chapter.color}, #8b5cf6)`,
                    border: 'none', color: 'white',
                    cursor: 'pointer', fontSize: '0.85rem', fontWeight: 700,
                    boxShadow: `0 4px 15px ${chapter.color}40`
                  }}
                >
                  Next <ChevronRight size={16} />
                </button>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
