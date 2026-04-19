import React, { useState, useEffect, useCallback } from 'react';
import axios from 'axios';
import {
  TrendingUp,
  TrendingDown,
  Wallet,
  BarChart3,
  Activity,
  LayoutDashboard,
  Search,
  Bell,
  LogOut,
  User,
  X,
  BookOpen,
  Plus
} from 'lucide-react';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  AreaChart,
  Area,
  ReferenceLine,
  ComposedChart,
  Bar
} from 'recharts';
import Login from './Login';
import Landing from './Landing';
import ParticleBackground from './ParticleBackground';
import LearningCenter from './LearningCenter';
import OnboardingTour from './OnboardingTour';
import ChatBot from './ChatBot';
import AddMoneyModal from './AddMoneyModal';

function App() {
  const [user, setUser] = useState(null);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [showLogin, setShowLogin] = useState(false);
  const [stocks, setStocks] = useState([]);
  const [selectedStock, setSelectedStock] = useState(null);
  const [chartData, setChartData] = useState([]);
  const [order, setOrder] = useState({ quantity: '', price: 0 });
  const [balance, setBalance] = useState(100000.00);
  const [portfolio, setPortfolio] = useState([]);
  const [trades, setTrades] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showSideMenu, setShowSideMenu] = useState(false);
  const [showLearning, setShowLearning] = useState(false);
  const [showTour, setShowTour] = useState(false);
  const [showAddMoney, setShowAddMoney] = useState(false);
  const [currentView, setCurrentView] = useState('dashboard'); // 'dashboard', 'profile', 'history', 'portfolio', 'notifications'

  // Detect Google Login Success via URL param
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const googleUsername = params.get('googleLoginSuccess');
    if (googleUsername) {
      // Simulate/perform the login shift
      setUser({ username: googleUsername });
      setIsLoggedIn(true);
      // Clean up the URL
      window.history.replaceState({}, document.title, window.location.pathname);
    }
  }, []);

  // Configure axios interceptor for multi-tenancy
  useEffect(() => {
    if (user) {
      axios.defaults.headers.common['X-Tenant-ID'] = user.username;
    } else {
      delete axios.defaults.headers.common['X-Tenant-ID'];
    }
  }, [user]);

  // Stats (Open/High/Low/Volume)
  const [stats, setStats] = useState({ open: 0, high: 0, low: 0, volume: 0 });

  // Indicator state
  const [activeIndicator, setActiveIndicator] = useState(null); // null | 'SMA' | 'EMA' | 'RSI'
  const [indicatorData, setIndicatorData] = useState({});       // { SMA: number, EMA: number, RSI: number }
  const [patterns, setPatterns] = useState([]);                 // Detected patterns
  const [showPatterns, setShowPatterns] = useState(false);      // Toggle chart overlay
  const [showCheatSheet, setShowCheatSheet] = useState(false);  // Toggle cheat sheet side panel

  useEffect(() => {
    if (isLoggedIn) {
      fetchStocks();
      fetchBalance();
      fetchPortfolio();
      fetchTrades();
      const interval = setInterval(() => {
        fetchStocks();
        fetchPortfolio();
      }, 3000);
      return () => clearInterval(interval);
    }
  }, [isLoggedIn]);

  const fetchPortfolio = async () => {
    try {
      const response = await axios.get('/api/user/portfolio');
      setPortfolio(response.data);
    } catch (error) {
      console.error("Error fetching portfolio", error);
    }
  };

  const fetchTrades = async () => {
    try {
      const response = await axios.get('/api/user/trades');
      setTrades(response.data);
    } catch (error) {
      console.error("Error fetching trades", error);
    }
  };

  const fetchBalance = async () => {
    try {
      const response = await axios.get('/api/user/balance');
      setBalance(response.data);
    } catch (error) {
      console.error("Error fetching balance", error);
    }
  };

  // ... (stats, indicators, patterns remain same but will use the header)
  
  // Fetch live stats (Open/High/Low/Volume)
  const fetchStats = useCallback(async (symbol) => {
    try {
      const response = await axios.get(`/api/market/stats/${symbol}`);
      setStats(response.data);
    } catch (error) {
      console.error("Error fetching stats", error);
    }
  }, []);

  // Fetch indicators for the current symbol
  const fetchIndicators = useCallback(async (symbol) => {
    try {
      const response = await axios.get(`/api/market/indicators/${symbol}?period=14`);
      setIndicatorData(response.data);
    } catch (error) {
      console.error("Error fetching indicators", error);
    }
  }, []);

  const fetchPatterns = useCallback(async (symbol) => {
    try {
      const response = await axios.get(`/api/market/patterns/${symbol}`);
      setPatterns(response.data);
    } catch (error) {
      console.error("Error fetching patterns", error);
    }
  }, []);

  // Initial stock selection
  useEffect(() => {
    if (stocks.length > 0 && !selectedStock) {
      setSelectedStock(stocks[0]);
    }
  }, [stocks.length]);

  // Sync selected stock price updates
  useEffect(() => {
    if (selectedStock && stocks.length > 0) {
      const updated = stocks.find(s => s.symbol === selectedStock.symbol);
      if (updated) setSelectedStock(updated);
    }
  }, [stocks, selectedStock?.symbol]);

  // When selected stock changes, fetch history, stats, and indicators
  useEffect(() => {
    if (selectedStock && isLoggedIn) {
      setChartData([]);          // immediately clear stale chart data
      fetchHistory(selectedStock.symbol);
      fetchStats(selectedStock.symbol);
      fetchIndicators(selectedStock.symbol);
      fetchPatterns(selectedStock.symbol);
      setActiveIndicator(null);
      setIndicatorData({});
      setPatterns([]);
    }
  }, [selectedStock?.symbol, isLoggedIn]);

  // Refresh indicators & stats periodically
  useEffect(() => {
    if (!selectedStock || !isLoggedIn) return;
    const interval = setInterval(() => {
      fetchStats(selectedStock.symbol);
      if (activeIndicator) fetchIndicators(selectedStock.symbol);
    }, 10000);
    return () => clearInterval(interval);
  }, [selectedStock?.symbol, activeIndicator, isLoggedIn]);

  const fetchHistory = async (symbol) => {
    try {
      const response = await axios.get(`/api/market/candles/${symbol}`);
      const raw = response.data.filter(d => d.close > 0 && d.high >= (d.low || 0));

      // Compute median close to filter out cross-stock outliers
      if (raw.length > 0) {
        const closes = [...raw.map(d => d.close)].sort((a, b) => a - b);
        const median = closes[Math.floor(closes.length / 2)];
        const validData = raw
          .filter(d => d.close > median * 0.25 && d.close < median * 4)
          .map(d => ({ ...d, body: [d.open, d.close] }));
        setChartData(validData);
      } else {
        setChartData([]);
      }
    } catch (error) {
      console.error("Error fetching history", error);
    }
  };

  useEffect(() => {
    if (!selectedStock || !isLoggedIn) return;
    const interval = setInterval(() => {
      fetchHistory(selectedStock.symbol);
    }, 10000);
    return () => clearInterval(interval);
  }, [selectedStock?.symbol, isLoggedIn]);

  const fetchStocks = async () => {
    try {
      const response = await axios.get('/api/market/stocks');
      console.log("Stocks fetched:", response.data?.length);
      if (Array.isArray(response.data)) {
        setStocks(response.data);
      } else {
        console.error("Stocks API did not return an array:", response.data);
      }
    } catch (error) {
      console.error("Error fetching stocks", error);
    }
  };

  const placeOrder = async (type) => {
    if (!selectedStock) {
      alert("Please select a stock first.");
      return;
    }

    const qty = parseInt(order.quantity);
    if (isNaN(qty) || qty <= 0) {
      alert("Please enter a valid quantity greater than zero.");
      return;
    }

    setLoading(true);
    try {
      await axios.post('/api/trade/order', {
        stock: { id: selectedStock.id },
        orderType: type,
        quantity: qty
      });

      alert(`Order Successful: ${type} ${qty} shares of ${selectedStock.symbol}`);
      setOrder({ ...order, quantity: '' }); 
      fetchBalance();
      fetchPortfolio();
      fetchTrades();
    } catch (error) {
      console.error("Order failed", error);
      const message = error.response?.data?.message || "Order failed. Please check your balance and try again.";
      alert(`Error: ${message}`);
    } finally {
      setLoading(false);
    }
  };

  const Candle = (props) => {
    const { x, y, width, height, payload } = props;
    if (!payload || x == null || y == null) return null;

    const { open, close, high, low } = payload;
    const isBullish = close >= open;
    const color = isBullish ? 'var(--accent-green)' : 'var(--accent-red)';

    // Safer coordinate calculation. 
    // If Recharts passes the scale, use it. Otherwise calculate ratio.
    let highY, lowY;
    const yScale = props.yAxis?.scale;
    
    if (typeof yScale === 'function') {
      highY = yScale(high);
      lowY = yScale(low);
    } else {
      const priceDiff = Math.abs(open - close);
      const ratio = priceDiff > 0 ? height / priceDiff : 2; // default ratio if flat
      highY = isBullish ? y - (high - close) * ratio : y - (high - open) * ratio;
      lowY = isBullish ? (y + height) + (open - low) * ratio : (y + height) + (close - low) * ratio;
    }

    return (
      <g>
        <line
          x1={x + width / 2}
          y1={highY}
          x2={x + width / 2}
          y2={lowY}
          stroke={color}
          strokeWidth={1}
        />
        <rect
          x={x}
          y={y - (height === 0 ? 1 : 0)} // Small sliver for dojis
          width={width}
          height={Math.max(height, 2)}
          fill={color}
          stroke={color}
        />
      </g>
    );
  };

  // ... (rest of the file)

  const handleLogout = () => {
    setIsLoggedIn(false);
    setShowLogin(true);
  };

  const handleIndicatorToggle = async (indicator) => {
    if (activeIndicator === indicator) {
      setActiveIndicator(null);
      return;
    }
    setActiveIndicator(indicator);
    if (selectedStock) {
      await fetchIndicators(selectedStock.symbol);
    }
  };

  // Format volume to Indian style (Cr, L, K)
  const formatVolume = (v) => {
    if (!v) return '₹0';
    if (v >= 10000000) return `${(v / 10000000).toFixed(1)} Cr`;
    if (v >= 100000) return `${(v / 100000).toFixed(1)} L`;
    if (v >= 1000) return `${(v / 1000).toFixed(1)} K`;
    return String(v);
  };

  const formatINR = (v) => {
    if (!v) return '₹0';
    return `₹${Number(v).toLocaleString('en-IN', { maximumFractionDigits: 2 })}`;
  };

  // Build RSI chart data from existing chart points + latest RSI value
  const rsiValue = indicatorData?.RSI;
  const rsiChartData = rsiValue != null
    ? chartData.slice(-1).map(d => ({ ...d, rsi: rsiValue }))
    : [];

  return (
    <>
      <ParticleBackground />
      {!isLoggedIn ? (
        showLogin ? (
          <Login onLogin={(loggedUser, isNew) => {
            setUser(loggedUser);
            setIsLoggedIn(true);
            if (isNew) setTimeout(() => setShowTour(true), 800);
          }} />
        ) : (
          <Landing onNavigateLogin={() => setShowLogin(true)} />
        )
      ) : (
        <>
          <div className="dashboard">


          <header id="tour-header">
            <div className="logo" style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
              <img 
                src="/logo.png" 
                alt="FinNova Logo" 
                style={{ height: '100px', objectFit: 'contain', imageRendering: 'pixelated' }} 
              />
              FinNova
            </div>
            <div className="header-actions">
              <div className="balance-card" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <div>
                  <div className="balance-label">Balance</div>
                  <div className="balance-value">{formatINR(balance)}</div>
                </div>
                <button 
                  onClick={() => setShowAddMoney(true)}
                  style={{ 
                    background: 'rgba(16,185,129,0.2)', border: '1px solid rgba(16,185,129,0.5)', 
                    color: '#10b981', display: 'flex', alignItems: 'center', justifyContent: 'center',
                    width: '32px', height: '32px', borderRadius: '50%', cursor: 'pointer', transition: 'all 0.2s'
                  }}
                  onMouseOver={e => e.currentTarget.style.background = 'rgba(16,185,129,0.4)'}
                  onMouseOut={e => e.currentTarget.style.background = 'rgba(16,185,129,0.2)'}
                  title="Add Practice Funds"
                >
                  <Plus size={18} strokeWidth={3} />
                </button>
              </div>
              <button
                onClick={() => setShowLearning(true)}
                style={{
                  display: 'flex', alignItems: 'center', gap: '0.4rem',
                  padding: '0.45rem 1rem', borderRadius: '8px',
                  background: 'linear-gradient(135deg, #3b82f6, #8b5cf6)',
                  border: 'none', color: 'white', cursor: 'pointer',
                  fontSize: '0.78rem', fontWeight: 700, letterSpacing: '0.3px',
                  boxShadow: '0 4px 15px rgba(59,130,246,0.3)'
                }}
              >
                📚 Learn
              </button>
              <button
                onClick={() => setShowTour(true)}
                style={{
                  display: 'flex', alignItems: 'center', gap: '0.4rem',
                  padding: '0.45rem 1rem', borderRadius: '8px',
                  background: 'linear-gradient(135deg, #10b981, #06b6d4)',
                  border: 'none', color: 'white', cursor: 'pointer',
                  fontSize: '0.78rem', fontWeight: 700, letterSpacing: '0.3px',
                  boxShadow: '0 4px 15px rgba(16,185,129,0.3)'
                }}
              >
                🗺️ Tour
              </button>
              <Bell size={20} color="var(--text-secondary)" style={{ cursor: 'pointer' }} />
              <div className="user-profile" onClick={() => setShowSideMenu(true)} style={{ cursor: 'pointer' }}>
                <div className="avatar-circle"></div>
              </div>
            </div>
          </header>

          <aside className="sidebar">
            <div>
              <h2 className="section-title">NSE Market</h2>
              <div className="market-list" id="tour-market-list">
                {stocks.map(stock => (
                  <div
                    key={stock.id}
                    className={`stock-item ${selectedStock?.id === stock.id ? 'active' : ''}`}
                    onClick={() => setSelectedStock(stock)}
                  >
                    <div>
                      <div className="symbol-name">{stock.symbol}</div>
                      <div className="company-name">{stock.companyName}</div>
                    </div>
                    <div style={{ textAlign: 'right' }}>
                      <div className="price">{formatINR(stock.currentPrice)}</div>
                      <div style={{
                        fontSize: '0.75rem',
                        color: stock.percentChange >= 0 ? 'var(--accent-green)' : 'var(--accent-pink)',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '2px',
                        justifyContent: 'flex-end'
                      }}>
                        {stock.percentChange >= 0 ? <TrendingUp size={12} /> : <TrendingDown size={12} />}
                        {stock.percentChange >= 0 ? '+' : ''}{stock.percentChange?.toFixed(2)}%
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="portfolio-section" id="tour-portfolio">
              <h2 className="section-title">My Portfolio</h2>
              <div className="market-list">
                {portfolio.length === 0 ? (
                  <div className="no-data-msg">No stocks owned</div>
                ) : (
                  portfolio.map(item => (
                    <div
                      key={item.id}
                      className="stock-item"
                      onClick={() => {
                        const s = stocks.find(st => st.symbol === item.stock.symbol);
                        if (s) setSelectedStock(s);
                      }}
                    >
                      <div>
                        <div className="symbol-name">{item.stock.symbol}</div>
                        <div className="company-name">{item.quantity} shares</div>
                      </div>
                      <div style={{ textAlign: 'right' }}>
                        <div className="price">
                          {formatINR(item.quantity * item.averagePrice)}
                        </div>
                        <div className="stats-label">Value</div>
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>

            <div className="card" style={{ marginTop: currentView === 'dashboard' ? 'auto' : 0 }}>
              <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
                <Wallet size={20} color="var(--accent-blue)" />
                <div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>Buying Power</div>
                  <div style={{ fontWeight: 700 }}>{formatINR(balance)}</div>
                </div>
              </div>
            </div>
          </aside>

          {currentView === 'dashboard' && (
            <>
              <main className="main-content">
                <div className="card stock-header-card" id="tour-stock-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <h1 className="symbol-title" style={{ fontSize: '1.5rem', fontWeight: 700 }}>{selectedStock?.symbol}</h1>
                <p className="company-subtitle" style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>{selectedStock?.companyName} · NSE</p>
              </div>
              <div style={{ textAlign: 'right' }}>
                <div className="stock-price-hero" style={{ fontSize: '1.5rem', fontWeight: 700 }}>
                  {formatINR(selectedStock?.currentPrice)}
                </div>
                <div className="stock-change-hero" style={{
                  color: selectedStock?.percentChange >= 0 ? 'var(--accent-green)' : 'var(--accent-red)',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px',
                  justifyContent: 'flex-end',
                  fontSize: '0.875rem'
                }}>
                  <span className="live-pulse"></span>
                  {selectedStock?.percentChange >= 0 ? '+' : ''}{selectedStock?.percentChange?.toFixed(2)}% · Live
                </div>
              </div>
            </div>

            {/* Main Price Chart */}
            <div className="card chart-container" id="tour-chart" style={{ position: 'relative' }}>
              {chartData.length === 0 && (
                <div style={{ position: 'absolute', inset: 0, display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--text-secondary)', zIndex: 10 }}>
                  Waiting for market data... (Stocks loaded: {stocks.length})
                </div>
              )}
              {chartData.length > 0 && (
                <div style={{ position: 'absolute', top: 10, left: 10, fontSize: '0.6rem', color: 'var(--text-secondary)', opacity: 0.5, zIndex: 10 }}>
                  DEBUG: {chartData.length} pts | Range: ₹{Math.min(...chartData.map(d => d.low))}-₹{Math.max(...chartData.map(d => d.high))}
                </div>
              )}
              <ResponsiveContainer width="100%" height="100%">
                <ComposedChart data={chartData} barCategoryGap="15%">
                  <CartesianGrid strokeDasharray="3 3" stroke="var(--glass-border)" vertical={false} />
                  <XAxis
                    dataKey="time"
                    stroke="var(--text-secondary)"
                    tick={{ fontSize: 9 }}
                    minTickGap={30}
                  />
                  <YAxis
                    domain={['dataMin', 'dataMax']}
                    padding={{ top: 30, bottom: 30 }}
                    allowDataOverflow={false}
                    orientation="right"
                    stroke="var(--text-secondary)"
                    tick={{ fontSize: 10 }}
                    tickFormatter={(v) => `₹${Number(v).toLocaleString('en-IN')}`}
                    width={80}
                  />
                  <Tooltip
                    contentStyle={{
                      background: '#0f121b',
                      border: '1px solid var(--glass-border)',
                      borderRadius: '8px',
                    }}
                    content={({ active, payload }) => {
                      if (active && payload && payload.length) {
                        const data = payload[0].payload;
                        const isBullish = data.close >= data.open;
                        return (
                          <div className="chart-tooltip-glass">
                            <div style={{ marginBottom: '8px', color: 'var(--text-secondary)', fontWeight: 700, fontSize: '0.9rem', borderBottom: '1px solid var(--glass-border)', paddingBottom: '4px' }}>
                              Time: {data.time}
                            </div>
                            <div style={{ color: isBullish ? 'var(--accent-green)' : 'var(--accent-red)' }}>
                              <div style={{ display: 'flex', justifyContent: 'space-between', gap: '30px', marginBottom: '4px' }}>
                                <span style={{ color: 'var(--text-secondary)' }}>Open:</span> <span style={{ fontWeight: 600 }}>{formatINR(data.open)}</span>
                              </div>
                              <div style={{ display: 'flex', justifyContent: 'space-between', gap: '30px', marginBottom: '4px' }}>
                                <span style={{ color: 'var(--text-secondary)' }}>High:</span> <span style={{ fontWeight: 600 }}>{formatINR(data.high)}</span>
                              </div>
                              <div style={{ display: 'flex', justifyContent: 'space-between', gap: '30px', marginBottom: '4px' }}>
                                <span style={{ color: 'var(--text-secondary)' }}>Low:</span> <span style={{ fontWeight: 600 }}>{formatINR(data.low)}</span>
                              </div>
                              <div style={{ display: 'flex', justifyContent: 'space-between', gap: '30px' }}>
                                <span style={{ color: 'var(--text-secondary)' }}>Close:</span> <span style={{ fontWeight: 600 }}>{formatINR(data.close)}</span>
                              </div>
                            </div>
                          </div>
                        );
                      }
                      return null;
                    }}
                  />
                  {/* Hidden range influencers to ensure high/low are in the domain */}
                  <Line dataKey="high" stroke="none" dot={false} activeDot={false} legendType="none" />
                  <Line dataKey="low" stroke="none" dot={false} activeDot={false} legendType="none" />

                  {/* Candlestick renderer */}
                  <Bar
                    dataKey="body"
                    shape={(props) => <Candle {...props} />}
                    animationDuration={300}
                  />

                  {/* SMA overlay */}
                  {activeIndicator === 'SMA' && indicatorData?.SMA != null && (
                    <ReferenceLine
                      y={indicatorData.SMA}
                      stroke="#f59e0b"
                      strokeDasharray="6 3"
                      strokeWidth={2}
                      label={{ value: `SMA ₹${Number(indicatorData.SMA).toFixed(1)}`, fill: '#f59e0b', fontSize: 10, position: 'insideTopLeft' }}
                    />
                  )}
                  {/* EMA overlay */}
                  {activeIndicator === 'EMA' && indicatorData?.EMA != null && (
                    <ReferenceLine
                      y={indicatorData.EMA}
                      stroke="#a78bfa"
                      strokeDasharray="6 3"
                      strokeWidth={2}
                      label={{ value: `EMA ₹${Number(indicatorData.EMA).toFixed(1)}`, fill: '#a78bfa', fontSize: 10, position: 'insideTopLeft' }}
                    />
                  )}

                  {/* Pattern Overlays */}
                  {showPatterns && patterns.map((p, pIdx) => (
                    <React.Fragment key={`pat-${pIdx}`}>
                      {p.lines.map((line, lIdx) => (
                        <Line
                          key={`line-${pIdx}-${lIdx}`}
                          type="monotone"
                          dataKey={(d) => {
                            const found = line.points.find(pt => pt.time === d.time);
                            return found ? found.price : null;
                          }}
                          stroke={p.type === 'BULLISH' ? '#10b981' : p.type === 'BEARISH' ? '#ef4444' : '#3b82f6'}
                          strokeWidth={3}
                          dot={{ r: 4, fill: '#fff', strokeWidth: 2 }}
                          strokeDasharray={line.label === "Resistance" ? "5 5" : "0"}
                          connectNulls
                          animationDuration={500}
                          style={{
                            filter: 'drop-shadow(0 0 5px rgba(59, 130, 246, 0.5))',
                            strokeLinecap: 'round'
                          }}
                        />
                      ))}
                      {p.name === "Triple Bottom" && p.lines[0]?.points[0] && (
                        <ReferenceLine
                          y={p.lines[0].points[0].price}
                          stroke="#10b981"
                          strokeDasharray="3 3"
                          label={{ value: "Neckline", fill: "#10b981", fontSize: 10, position: 'insideTopLeft' }}
                        />
                      )}
                    </React.Fragment>
                  ))}
                </ComposedChart>
              </ResponsiveContainer>
            </div>

            {/* RSI Panel (only shown when RSI is active) */}
            {activeIndicator === 'RSI' && (
              <div className="card rsi-panel">
                <div className="rsi-header">
                  <span className="stats-label">RSI (14)</span>
                  {indicatorData?.RSI != null ? (
                    <span className={`rsi-value ${indicatorData.RSI > 70 ? 'overbought' : indicatorData.RSI < 30 ? 'oversold' : 'neutral'}`}>
                      {indicatorData.RSI.toFixed(1)}
                      {indicatorData.RSI > 70 ? ' · Overbought' : indicatorData.RSI < 30 ? ' · Oversold' : ' · Neutral'}
                    </span>
                  ) : (
                    <span className="rsi-loading">Collecting data…</span>
                  )}
                </div>
                {/* RSI visual gauge bar */}
                <div className="rsi-gauge-container">
                  <div className="rsi-gauge-bar" style={{
                    width: `${indicatorData?.RSI ?? 50}%`,
                    background: indicatorData?.RSI > 70
                      ? 'linear-gradient(90deg, #f59e0b, #ef4444)'
                      : indicatorData?.RSI < 30
                        ? 'linear-gradient(90deg, #10b981, #06b6d4)'
                        : 'linear-gradient(90deg, #3b82f6, #a78bfa)',
                  }} />
                  {/* Reference lines at 30 and 70 */}
                  <div className="rsi-ref-line" style={{ left: '30%' }} />
                  <div className="rsi-ref-line" style={{ left: '70%' }} />
                </div>
                <div className="rsi-labels">
                  <span>0 — Oversold</span>
                  <span>Neutral</span>
                  <span>Overbought — 100</span>
                </div>
              </div>
            )}

            <div className="main-stats-grid">
              {/* Live Statistics */}
              <div className="card" id="tour-stats">
                <h3 className="section-title">Today's Stats · NSE</h3>
                <div className="stats-grid">
                  <div className="stats-item">
                    <div className="stats-label">Open</div>
                    <div className="stats-value">{stats.open ? formatINR(stats.open) : '—'}</div>
                  </div>
                  <div className="stats-item">
                    <div className="stats-label">High</div>
                    <div className="stats-value">{stats.high ? formatINR(stats.high) : '—'}</div>
                  </div>
                  <div className="stats-item">
                    <div className="stats-label">Low</div>
                    <div className="stats-value">{stats.low ? formatINR(stats.low) : '—'}</div>
                  </div>
                  <div className="stats-item">
                    <div className="stats-label">Volume</div>
                    <div className="stats-value">{stats.volume ? formatVolume(stats.volume) : '—'}</div>
                  </div>
                </div>
              </div>

            {/* Technical Indicators — now functional */}
            <div className="card technical-indicators" id="tour-indicators">
              <h3 className="section-title">Technical Indicators</h3>
                <div className="indicators-container">
                  {['SMA', 'EMA', 'RSI'].map((ind) => (
                    <button
                      key={ind}
                      onClick={() => handleIndicatorToggle(ind)}
                      className={`btn-indicator ${activeIndicator === ind ? `active ${ind.toLowerCase()}` : ''}`}
                    >
                      {ind}
                    </button>
                  ))}
                  <button
                    onClick={async () => {
                      if (!showPatterns && selectedStock) {
                        setLoading(true);
                        await fetchPatterns(selectedStock.symbol);
                        setLoading(false);
                      }
                      setShowPatterns(!showPatterns);
                    }}
                    disabled={loading}
                    className={`btn-indicator ${showPatterns ? 'active pattern' : ''}`}
                  >
                    {loading ? 'Scanning...' : 'Scan Patterns'}
                    {patterns.length > 0 && showPatterns && (
                      <span className="pattern-count-badge">
                        {patterns.length}
                      </span>
                    )}
                  </button>
                </div>
                {/* Show indicator/pattern values */}
                <div className="indicator-values-display">
                  {indicatorData?.error ? (
                    <span className="indicator-error-message">{indicatorData.error}</span>
                  ) : (
                    <>
                      {indicatorData?.SMA != null && activeIndicator === 'SMA' && (
                        <div><span style={{ color: '#f59e0b', fontWeight: 600 }}>SMA(14):</span> {formatINR(indicatorData.SMA)}</div>
                      )}
                      {indicatorData?.EMA != null && activeIndicator === 'EMA' && (
                        <div><span style={{ color: '#a78bfa', fontWeight: 600 }}>EMA(14):</span> {formatINR(indicatorData.EMA)}</div>
                      )}
                      {indicatorData?.RSI != null && activeIndicator === 'RSI' && (
                        <div>
                          <span style={{ color: '#10b981', fontWeight: 600 }}>RSI(14):</span>{' '}
                          <span style={{
                            color: indicatorData.RSI > 70 ? '#ef4444' : indicatorData.RSI < 30 ? '#10b981' : '#f59e0b'
                          }}>
                            {indicatorData.RSI.toFixed(1)}
                          </span>
                        </div>
                      )}
                      {showPatterns && patterns.length > 0 && (
                        <div style={{ marginTop: '0.5rem', background: 'rgba(59, 130, 246, 0.1)', padding: '8px', borderRadius: '8px', borderLeft: '3px solid var(--accent-cyan)' }}>
                          {patterns.map((p, i) => (
                            <div key={i} style={{ marginBottom: '4px' }}>
                              <span style={{ color: 'var(--accent-cyan)', fontWeight: 700 }}>{p.name}:</span> {p.description}
                            </div>
                          ))}
                        </div>
                      )}
                      {!activeIndicator && !showPatterns && (
                        <span style={{ fontSize: '0.7rem' }}>Click a button above to activate</span>
                      )}
                    </>
                  )}
                </div>
              </div>
            </div>
          </main>

          <aside className="right-panel">
            <div className="card" id="tour-order-panel">
              <h2 className="section-title">Place Order</h2>
              <div className="order-form">
                <div className="form-group">
                  <label>Quantity</label>
                  <input
                    type="number"
                    placeholder="0"
                    min="1"
                    value={order.quantity}
                    onChange={(e) => setOrder({ ...order, quantity: e.target.value })}
                  />
                </div>
                <div className="form-group">
                  <label>Limit Price (₹)</label>
                  <input
                    type="number"
                    placeholder={selectedStock?.currentPrice.toFixed(2)}
                    onChange={(e) => setOrder({ ...order, price: parseFloat(e.target.value) })}
                  />
                </div>
                <div className="btn-group">
                  <button className="btn-buy" onClick={() => placeOrder('BUY')}>BUY</button>
                  <button className="btn-sell" onClick={() => placeOrder('SELL')}>SELL</button>
                </div>
                <p style={{ fontSize: '0.7rem', color: 'var(--text-secondary)', textAlign: 'center', marginTop: '0.5rem' }}>
                  STT + Exchange fee: 0.05%
                </p>
              </div>
            </div>

            <div className="card cheat-sheet-section" id="tour-cheatsheet" style={{ flex: 1, maxHeight: '400px', overflowY: 'auto' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                <h2 className="section-title" style={{ margin: 0 }}>Pattern Cheat Sheet</h2>
                <button
                  onClick={() => setShowCheatSheet(!showCheatSheet)}
                  style={{ background: 'transparent', border: 'none', color: 'var(--accent-cyan)', fontSize: '0.7rem', cursor: 'pointer', fontWeight: 700 }}
                >
                  {showCheatSheet ? 'HIDE' : 'LEARN'}
                </button>
              </div>

              {showCheatSheet ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                  <CheatSheetItem
                    title="TRIPLE BOTTOM"
                    desc="3 equal lows. Bullish reversal pattern indicating price has found strong support."
                    color="#10b981"
                  />
                  <CheatSheetItem
                    title="FALLING WEDGE"
                    desc="Descending lines that narrow. Bullish reversal indicating sellers are losing steam."
                    color="#10b981"
                  />
                  <CheatSheetItem
                    title="SYMMETRICAL TRIANGLE"
                    desc="Consolidating highs and lows. Breakout can happen in either direction."
                    color="#3b82f6"
                  />
                  <CheatSheetItem
                    title="DESCENDING TRIANGLE"
                    desc="Lower highs with flat support. Typically bearish continuation pattern."
                    color="#ef4444"
                  />
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                  <h2 className="section-title">Recent Trades</h2>
                  {trades.length === 0 ? (
                    <div style={{ color: 'var(--text-secondary)', fontSize: '0.8rem', textAlign: 'center' }}>No trades yet</div>
                  ) : (
                    trades.map(trade => (
                      <TradeRow
                        key={trade.id}
                        type={trade.buyOrder ? 'BUY' : 'SELL'}
                        symbol={trade.stock.symbol}
                        qty={trade.quantity}
                        price={formatINR(trade.price)}
                        time={new Date(trade.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      />
                    ))
                  )}
                </div>
              )}
            </div>
          </aside>
          </>
          )}

          {currentView === 'history' && (
            <main className="main-content" style={{ gridColumn: '2 / -1' }}>
              <div className="card" style={{ flex: 1 }}>
                <h2 className="section-title" style={{ marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <BarChart3 size={24} color="var(--accent-cyan)" /> Trade History
              </h2>
              {trades.length === 0 ? (
                <div className="no-data-msg" style={{ padding: '3rem 0', fontSize: '1rem' }}>No trades executed yet.</div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                  {trades.map(t => (
                    <div key={t.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '1rem', background: 'var(--bg-accent)', borderRadius: '12px' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                        <div style={{
                          width: 12, height: 12, borderRadius: '50%',
                          background: t.buyOrder ? 'var(--accent-green)' : 'var(--accent-red)'
                        }} />
                        <div>
                          <div style={{ fontWeight: 700, fontSize: '1.1rem' }}>{t.buyOrder ? 'BUY' : 'SELL'} · {t.stock.symbol}</div>
                          <div style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>{new Date(t.createdAt).toLocaleString()}</div>
                        </div>
                      </div>
                      <div style={{ textAlign: 'right' }}>
                        <div style={{ fontWeight: 700, fontSize: '1.2rem' }}>{formatINR(t.price)}</div>
                        <div style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>{t.quantity} Shares</div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </main>
          )}

          {currentView === 'portfolio' && (
            <main className="main-content" style={{ gridColumn: '2 / -1' }}>
              <div className="card" style={{ flex: 1 }}>
                <h2 className="section-title" style={{ marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <Wallet size={24} color="var(--accent-blue)" /> Full Portfolio
              </h2>
              {portfolio.length === 0 ? (
                <div className="no-data-msg" style={{ padding: '3rem 0', fontSize: '1rem' }}>Your portfolio is currently empty.</div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                  {portfolio.map(p => (
                    <div key={p.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '1.5rem', background: 'var(--bg-accent)', borderRadius: '12px' }}>
                      <div>
                        <div style={{ fontWeight: 700, fontSize: '1.3rem', color: 'white' }}>{p.stock.symbol}</div>
                        <div style={{ color: 'var(--accent-cyan)', fontSize: '0.9rem', marginTop: '0.2rem' }}>{p.quantity} Shares Owned</div>
                      </div>
                      <div style={{ textAlign: 'right' }}>
                        <div style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginBottom: '0.2rem' }}>Avg Price</div>
                        <div style={{ fontWeight: 700, fontSize: '1.1rem' }}>{formatINR(p.averagePrice)}</div>
                      </div>
                      <div style={{ textAlign: 'right' }}>
                        <div style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginBottom: '0.2rem' }}>Total Invested</div>
                        <div style={{ fontWeight: 700, fontSize: '1.3rem', color: 'white' }}>{formatINR(p.quantity * p.averagePrice)}</div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </main>
          )}

          {currentView === 'profile' && (
            <main className="main-content" style={{ gridColumn: '2 / -1' }}>
              <div className="card" style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
              <div style={{ width: 100, height: 100, borderRadius: '50%', background: 'linear-gradient(135deg, var(--accent-blue), var(--accent-purple))', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '1.5rem' }}>
                <User size={50} color="white" />
              </div>
              <h2 style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>{user?.username}</h2>
              <div style={{ color: 'var(--text-secondary)', fontSize: '1.1rem', marginBottom: '3rem' }}>Simulation Trader</div>
              
              <div style={{ display: 'flex', gap: '3rem', width: '100%', maxWidth: '600px', justifyContent: 'center', padding: '2rem', background: 'var(--bg-accent)', borderRadius: '20px' }}>
                <div style={{ textAlign: 'center' }}>
                  <div style={{ color: 'var(--text-secondary)', marginBottom: '0.5rem' }}>Total Trades</div>
                  <div style={{ fontSize: '1.8rem', fontWeight: 800 }}>{trades.length}</div>
                </div>
                <div style={{ width: 1, background: 'rgba(255,255,255,0.1)' }}></div>
                <div style={{ textAlign: 'center' }}>
                  <div style={{ color: 'var(--text-secondary)', marginBottom: '0.5rem' }}>Portfolio Assets</div>
                  <div style={{ fontSize: '1.8rem', fontWeight: 800 }}>{portfolio.length}</div>
                </div>
                <div style={{ width: 1, background: 'rgba(255,255,255,0.1)' }}></div>
                <div style={{ textAlign: 'center' }}>
                  <div style={{ color: 'var(--text-secondary)', marginBottom: '0.5rem' }}>Liquid Cash</div>
                  <div style={{ fontSize: '1.8rem', fontWeight: 800, color: 'var(--accent-green)' }}>{formatINR(balance)}</div>
                </div>
              </div>
            </div>
          </main>
          )}

          {currentView === 'notifications' && (
             <main className="main-content" style={{ gridColumn: '2 / -1' }}>
               <div className="card" style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
               <Bell size={64} color="var(--text-secondary)" style={{ opacity: 0.5, marginBottom: '1.5rem' }} />
               <h2 style={{ color: 'var(--text-secondary)' }}>No new notifications</h2>
             </div>
           </main>
          )}
        </div>

        {/* Sidebar Menu Overlay */}
        <div className={`sidemenu-overlay ${showSideMenu ? 'active' : ''}`} onClick={() => setShowSideMenu(false)}>
          <div className={`sidemenu-content ${showSideMenu ? 'active' : ''}`} onClick={(e) => e.stopPropagation()}>
            <div className="sidemenu-header">
              <div className="user-info">
                <div className="avatar-large"></div>
                <div className="user-details">
                  <h3>{user?.username || 'Trader'}</h3>
                  <p>Pro Account</p>
                </div>
              </div>
              <button className="close-btn" onClick={() => setShowSideMenu(false)}>
                <X size={20} />
              </button>
            </div>
            <nav className="sidemenu-nav">
              <button className={`menu-item ${currentView === 'dashboard' ? 'active' : ''}`} onClick={() => { setCurrentView('dashboard'); setShowSideMenu(false); }}>
                <LayoutDashboard size={18} />
                <span>Dashboard</span>
              </button>
              <button className={`menu-item ${currentView === 'profile' ? 'active' : ''}`} onClick={() => { setCurrentView('profile'); setShowSideMenu(false); }}>
                <User size={18} />
                <span>My Profile</span>
              </button>
              <button className={`menu-item ${currentView === 'history' ? 'active' : ''}`} onClick={() => { setCurrentView('history'); setShowSideMenu(false); }}>
                <BarChart3 size={18} />
                <span>Trade History</span>
              </button>
              <button className={`menu-item ${currentView === 'portfolio' ? 'active' : ''}`} onClick={() => { setCurrentView('portfolio'); setShowSideMenu(false); }}>
                <Wallet size={18} />
                <span>Portfolio</span>
              </button>
              <button className={`menu-item ${currentView === 'notifications' ? 'active' : ''}`} onClick={() => { setCurrentView('notifications'); setShowSideMenu(false); }}>
                <Bell size={18} />
                <span>Notifications</span>
              </button>
              <div className="menu-divider"></div>
              <button className="menu-item" onClick={() => { setShowSideMenu(false); setShowLearning(true); }}>
                <BookOpen size={18} />
                <span>Learning Center</span>
              </button>
              <button className="menu-item" onClick={() => { setShowSideMenu(false); setShowTour(true); }}>
                <span style={{ fontSize: '1rem' }}>🗺️</span>
                <span>Take Tour Again</span>
              </button>
              <button className="menu-item logout" onClick={handleLogout}>
                <LogOut size={18} />
                <span>Log Out</span>
              </button>
            </nav>
          </div>
          <ChatBot username={user?.username} />
        </div>
      {showLearning && <LearningCenter onClose={() => setShowLearning(false)} />}
      {showTour && <OnboardingTour username={user?.username} onClose={() => setShowTour(false)} onOpenLearning={() => setShowLearning(true)} />}
      {showAddMoney && <AddMoneyModal onClose={() => setShowAddMoney(false)} onBalanceUpdated={(newBal) => setBalance(newBal)} />}
      </>
    )}
    </>
  );
}

function LabelText({ label, value }) {
  return (
    <div style={{ marginBottom: '0.75rem' }}>
      <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>{label}</div>
      <div style={{ fontWeight: 600 }}>{value}</div>
    </div>
  );
}

function TradeRow({ type, symbol, qty, price, time }) {
  return (
    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.875rem' }}>
      <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
        <div style={{
          width: 8, height: 8, borderRadius: '50%',
          background: type === 'BUY' ? 'var(--accent-green)' : 'var(--accent-red)'
        }}></div>
        <div>
          <span style={{ fontWeight: 600 }}>{type} · {symbol}</span>
          <div style={{ fontSize: '0.7rem', color: 'var(--text-secondary)' }}>{qty} shares</div>
        </div>
      </div>
      <div style={{ textAlign: 'right' }}>
        <div style={{ fontWeight: 600 }}>{price}</div>
        <div style={{ fontSize: '0.7rem', color: 'var(--text-secondary)' }}>{time}</div>
      </div>
    </div>
  );
}

function CheatSheetItem({ title, desc, color }) {
  return (
    <div style={{ padding: '12px', background: 'var(--bg-accent)', borderRadius: '12px', borderLeft: `4px solid ${color}`, transition: 'transform 0.2s ease' }}>
      <div style={{ fontSize: '0.75rem', fontWeight: 800, color: color, marginBottom: '4px', letterSpacing: '0.5px' }}>{title}</div>
      <div style={{ fontSize: '0.72rem', color: 'var(--text-secondary)', lineHeight: 1.4 }}>{desc}</div>
    </div>
  );
}

export default App;
