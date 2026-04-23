import React from 'react';
import { ShieldCheck, TrendingUp, BarChart3, Users, ArrowRight } from 'lucide-react';

export default function Landing({ onNavigateLogin }) {
  return (
    <div className="landing-container">
      <div className="hero-background">
        <div className="animated-grid"></div>
        <div className="shape-1"></div>
        <div className="shape-2"></div>
        <div className="shape-3"></div>
      </div>

      <nav className="landing-nav">
        <div className="logo-group" style={{ gap: '1rem', alignItems: 'center' }}>
          <img 
            src="/logo.png" 
            alt="FinNova Logo" 
            style={{ height: '120px', objectFit: 'contain', imageRendering: 'pixelated' }} 
          />
          <div className="logo-text" style={{ fontSize: '2.5rem' }}>FinNova</div>
        </div>
        <div className="nav-links">
          <a href="#features">Features</a>
          <a href="#founders">About Us</a>
          <button onClick={onNavigateLogin} className="btn-nav-login">
            Login <ArrowRight size={16} />
          </button>
        </div>
      </nav>

      <main className="landing-main">
        <section className="hero-section">
          <h1 className="hero-title">
            Master the Market<br />
            <span className="gradient-text">Without the Risk</span>
          </h1>
          <p className="hero-subtitle">
            Experience real-time simulated trading with paper money. Practice your strategies, analyze market trends, and become a better trader before risking real capital.
          </p>
          <div className="hero-cta">
            <button onClick={onNavigateLogin} className="btn-primary-large">
              Start Trading Now
            </button>
          </div>
        </section>

        <section id="features" className="features-section">
          <div className="section-header">
            <h2>Why Choose Our Platform</h2>
            <p>Everything you need to practice trading in one place</p>
          </div>
          <div className="features-grid">
            <div className="feature-card">
              <div className="feature-icon"><TrendingUp size={28} color="#10b981" /></div>
              <h3>Real-time Simulation</h3>
              <p>Experience dynamic market conditions driven by our advanced geometric Brownian motion algorithm directly mimicking real volatility.</p>
            </div>
            <div className="feature-card">
              <div className="feature-icon"><BarChart3 size={28} color="#3b82f6" /></div>
              <h3>Advanced Analytics</h3>
              <p>Visualize your portfolio performance with intuitive charts, candlestick patterns, and technical indicators.</p>
            </div>
            <div className="feature-card">
              <div className="feature-icon"><Users size={28} color="#8b5cf6" /></div>
              <h3>Risk-Free Environment</h3>
              <p>Start with ₹83,00,000 in virtual paper currency and test unlimited strategies without ever losing real money.</p>
            </div>
          </div>
        </section>

        <section id="founders" className="founders-section">
          <div className="section-header">
            <h2>Meet the Founders</h2>
            <p>The team behind FinNova</p>
          </div>
          <div className="founders-grid">
            <div className="founder-card">
              <div className="founder-avatar"></div>
              <h3>Naitik Jain</h3>
              <p className="founder-role">UI and Product Architect Developer</p>
              <p className="founder-bio">Expert in crafting intuitive user interfaces and architecting seamless product experiences for modern trading platforms.</p>
            </div>

            <div className="founder-card">
              <div className="founder-avatar" style={{ background: 'var(--accent-red)' }}></div>
              <h3>Mahi Tiwari</h3>
              <p className="founder-role">Backend Developer</p>
              <p className="founder-bio">Specialized in building high-performance backend systems and robust trading engines for complex financial simulations.</p>
            </div>

            <div className="founder-card">
              <div className="founder-avatar" style={{ background: 'var(--accent-cyan)' }}></div>
              <h3>Saiyam Aggarwal</h3>
              <p className="founder-role">Quantitative Analyst</p>
              <p className="founder-bio">Specializing in algorithmic market simulation, data analytics, and risk management models.</p>
            </div>
          </div>
        </section>
      </main>

      <footer className="landing-footer">
        <div className="logo-group" style={{ opacity: 0.8, gap: '0.75rem', alignItems: 'center' }}>
          <img 
            src="/logo.png" 
            alt="FinNova Logo" 
            style={{ height: '48px', objectFit: 'contain', imageRendering: 'pixelated' }} 
          />
          <div className="logo-text" style={{ fontSize: '1.5rem' }}>FinNova</div>
        </div>
        <p>© 2026 FinNova. All rights reserved.</p>
      </footer>
    </div>
  );
}
