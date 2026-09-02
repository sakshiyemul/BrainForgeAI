import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  User,
  Lock,
  Mail,
  Eye,
  EyeOff,
  ArrowRight,
  MessageSquare,
  FileText,
  Search,
  BarChart2,
  CheckCircle2,
  AlertCircle,
  Cpu,
  Zap
} from "lucide-react";
import ThemeToggle from "../components/ThemeToggle";

function Register() {
  const navigate = useNavigate();

  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState({ text: "", type: "" });

  const handleRegister = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage({ text: "", type: "" });

    try {
      const response = await fetch("https://brainforgeai-backend.onrender.com/users/registerUser", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          fullName: fullName.trim(),
          email: email.trim(),
          password,
          role: "USER",
        }),
      });

      const data = await response.json();

      if (!response.ok) {
        setMessage({
          text: data.message || "Registration failed. Please check your details.",
          type: "error",
        });
        setLoading(false);
        return;
      }

      setMessage({
        text: "Account registered successfully! Redirecting to sign in...",
        type: "success",
      });

      setTimeout(() => {
        navigate("/");
      }, 700);
    } catch (error) {
      setMessage({
        text: "Unable to connect to backend server.",
        type: "error",
      });
      setLoading(false);
    }
  };

  return (
    <div className="auth-split-container">
      {/* ----------------- LEFT HERO PANE (70% WIDTH, ALWAYS DARK) ----------------- */}
      <div className="auth-hero-pane">
        <header className="hero-top-nav">
          <div className="hero-brand">
            <div className="hero-brand-icon">
              <Zap size={22} color="#070d18" />
            </div>
            <div className="hero-brand-text">
              <h1 style={{ color: "#F8FAFC", opacity: 1, fontWeight: 800 }}>BrainForge AI</h1>
              <span style={{ color: "#18C8E8", opacity: 1, fontWeight: 700 }}>Knowledge Platform</span>
            </div>
          </div>

          <div className="hero-system-status">
            <div className="status-pill" style={{ color: "#18C8E8", opacity: 1 }}>
              <div className="status-dot-pulse" style={{ backgroundColor: "#18C8E8" }} />
              <span style={{ color: "#18C8E8", opacity: 1 }}>All Systems Online</span>
            </div>
            <span className="version-tag" style={{ color: "#7F91A8", opacity: 1 }}>v 2.4.0</span>
          </div>
        </header>

        <main className="auth-hero-main" style={{ opacity: 1 }}>
          <div
            className="hero-project-header"
            style={{ color: "#18C8E8", opacity: 1, fontWeight: 700, letterSpacing: "2px" }}
          >
            KNOWLEDGE PLATFORM • DOCUMENT GROUNDING • 2026
          </div>

          <h1
            className="hero-main-title"
            style={{ color: "#F8FAFC", opacity: 1, fontWeight: 900 }}
          >
            Document-Powered
            <span
              className="hero-title-gradient"
              style={{
                color: "#18C8E8",
                WebkitTextFillColor: "#18C8E8",
                opacity: 1,
                fontWeight: 900,
                display: "block"
              }}
            >
              Knowledge Engine
            </span>
          </h1>

          <h2
            className="hero-sub-title"
            style={{
              color: "#C7D2E3",
              opacity: 1,
              fontWeight: 500,
              fontSize: "19px",
              lineHeight: "1.4"
            }}
          >
            Context-Aware Assistant &amp; Document Knowledge Base
          </h2>

          <p
            className="hero-description-text"
            style={{
              color: "#B8C4D6",
              opacity: 1,
              lineHeight: "1.7",
              fontSize: "14.5px"
            }}
          >
            Join BrainForge AI to organize{" "}
            <span
              className="hero-highlight"
              style={{ color: "#18C8E8", opacity: 1, fontWeight: 600 }}
            >
              knowledge
            </span>
            , upload{" "}
            <span
              className="hero-highlight"
              style={{ color: "#18C8E8", opacity: 1, fontWeight: 600 }}
            >
              documents
            </span>
            , ask questions, and get{" "}
            <span
              className="hero-highlight"
              style={{ color: "#18C8E8", opacity: 1, fontWeight: 600 }}
            >
              contextual answers
            </span>{" "}
            grounded in your verified content.
          </p>

          <div className="hero-badges-row">
            <div className="hero-badge-pill" style={{ color: "#AFC0D3", opacity: 1 }}>
              <MessageSquare size={14} style={{ color: "#18C8E8" }} />
              <span style={{ color: "#AFC0D3", opacity: 1 }}>Interactive Chat</span>
            </div>
            <div className="hero-badge-pill" style={{ color: "#AFC0D3", opacity: 1 }}>
              <FileText size={14} style={{ color: "#38bdf8" }} />
              <span style={{ color: "#AFC0D3", opacity: 1 }}>Document Grounding</span>
            </div>
            <div className="hero-badge-pill" style={{ color: "#AFC0D3", opacity: 1 }}>
              <Search size={14} style={{ color: "#818cf8" }} />
              <span style={{ color: "#AFC0D3", opacity: 1 }}>Smart Search</span>
            </div>
            <div className="hero-badge-pill" style={{ color: "#AFC0D3", opacity: 1 }}>
              <BarChart2 size={14} style={{ color: "#10b981" }} />
              <span style={{ color: "#AFC0D3", opacity: 1 }}>Analytics</span>
            </div>
          </div>

          <div className="hero-stats-panel">
            <div className="hero-stat-card">
              <h3 style={{ color: "#F8FAFC", opacity: 1, fontWeight: 900 }}>100%</h3>
              <span style={{ color: "#7F91A8", opacity: 1, fontWeight: 700 }}>Local &amp; Secure</span>
            </div>
            <div className="hero-stat-card cyan-stat">
              <h3 style={{ color: "#18C8E8", WebkitTextFillColor: "#18C8E8", opacity: 1, fontWeight: 900 }}>
                PDF + TXT
              </h3>
              <span style={{ color: "#7F91A8", opacity: 1, fontWeight: 700 }}>Document Formats</span>
            </div>
            <div className="hero-stat-card">
              <h3 style={{ color: "#F8FAFC", opacity: 1, fontWeight: 900 }}>Multi-Turn</h3>
              <span style={{ color: "#7F91A8", opacity: 1, fontWeight: 700 }}>Session History</span>
            </div>
          </div>
        </main>

        <footer className="hero-telemetry-footer">
          <div className="telemetry-status-row" style={{ color: "#71839A", opacity: 1 }}>
            <div className="telemetry-status-left" style={{ color: "#71839A", opacity: 1 }}>
              <span className="telemetry-tag" style={{ color: "#18C8E8", opacity: 1, fontWeight: 700 }}>
                <Cpu size={13} style={{ color: "#18C8E8" }} />
                <span style={{ color: "#18C8E8", opacity: 1 }}>SYSTEM OPERATIONAL</span>
              </span>
              <span style={{ color: "#71839A", opacity: 1 }}>• WORKSPACE ENGINE READY</span>
              <span style={{ color: "#71839A", opacity: 1 }}>SESSION: SECURE</span>
            </div>
            <div style={{ color: "#71839A", opacity: 1 }}>ACTIVE NODE • STAGE PRODUCTION</div>
          </div>

          <div className="telemetry-nodes-visual">
            <svg width="120" height="48" viewBox="0 0 120 48" fill="none">
              <circle cx="60" cy="24" r="4" fill="#18C8E8" />
              <circle cx="20" cy="14" r="3" fill="#38bdf8" />
              <circle cx="25" cy="38" r="2.5" fill="#818cf8" />
              <circle cx="95" cy="17" r="3" fill="#18C8E8" />
              <circle cx="100" cy="36" r="2.5" fill="#38bdf8" />
              <line x1="60" y1="24" x2="20" y2="14" stroke="rgba(24, 200, 232, 0.4)" strokeWidth="1" />
              <line x1="60" y1="24" x2="25" y2="38" stroke="rgba(24, 200, 232, 0.3)" strokeWidth="1" />
              <line x1="60" y1="24" x2="95" y2="17" stroke="rgba(24, 200, 232, 0.4)" strokeWidth="1" />
              <line x1="60" y1="24" x2="100" y2="36" stroke="rgba(24, 200, 232, 0.3)" strokeWidth="1" />
              <line x1="20" y1="14" x2="25" y2="38" stroke="rgba(56, 189, 248, 0.2)" strokeWidth="1" />
              <line x1="95" y1="17" x2="100" y2="36" stroke="rgba(56, 189, 248, 0.2)" strokeWidth="1" />
            </svg>

            <svg width="120" height="48" viewBox="0 0 120 48" fill="none">
              <circle cx="60" cy="24" r="4" fill="#18C8E8" />
              <circle cx="35" cy="11" r="2.5" fill="#18C8E8" />
              <circle cx="40" cy="40" r="3" fill="#38bdf8" />
              <circle cx="85" cy="13" r="3" fill="#818cf8" />
              <circle cx="80" cy="38" r="2.5" fill="#18C8E8" />
              <line x1="60" y1="24" x2="35" y2="11" stroke="rgba(24, 200, 232, 0.4)" strokeWidth="1" />
              <line x1="60" y1="24" x2="40" y2="40" stroke="rgba(24, 200, 232, 0.3)" strokeWidth="1" />
              <line x1="60" y1="24" x2="85" y2="13" stroke="rgba(24, 200, 232, 0.4)" strokeWidth="1" />
              <line x1="60" y1="24" x2="80" y2="38" stroke="rgba(24, 200, 232, 0.3)" strokeWidth="1" />
            </svg>

            <svg width="120" height="48" viewBox="0 0 120 48" fill="none">
              <circle cx="60" cy="24" r="4" fill="#18C8E8" />
              <circle cx="25" cy="19" r="3" fill="#38bdf8" />
              <circle cx="35" cy="36" r="2.5" fill="#18C8E8" />
              <circle cx="90" cy="21" r="3" fill="#38bdf8" />
              <circle cx="85" cy="40" r="2.5" fill="#818cf8" />
              <line x1="60" y1="24" x2="25" y2="19" stroke="rgba(24, 200, 232, 0.4)" strokeWidth="1" />
              <line x1="60" y1="24" x2="35" y2="36" stroke="rgba(24, 200, 232, 0.3)" strokeWidth="1" />
              <line x1="60" y1="24" x2="90" y2="21" stroke="rgba(24, 200, 232, 0.4)" strokeWidth="1" />
              <line x1="60" y1="24" x2="85" y2="40" stroke="rgba(24, 200, 232, 0.3)" strokeWidth="1" />
            </svg>
          </div>
        </footer>
      </div>

      {/* ----------------- RIGHT AUTH FORM PANE (30% WIDTH, ALWAYS LIGHT) ----------------- */}
      <div className="auth-form-pane">
        <div className="auth-theme-floating">
          <ThemeToggle />
        </div>

        <div className="auth-form-card">
          <div className="auth-card-brand-badge">
            <Zap size={18} style={{ color: "#0096c7" }} />
            <span>BrainForge AI</span>
          </div>

          <div className="auth-card-header">
            <h2>
              Create your <span className="auth-role-cyan">Account</span>
            </h2>
            <p>Enter your information to set up your workspace credentials.</p>
          </div>

          {message.text && (
            <div
              className={`admin-message-banner ${
                message.type === "error" ? "banner-error" : "banner-success"
              }`}
              style={{ marginBottom: "18px" }}
            >
              {message.type === "error" ? <AlertCircle size={17} /> : <CheckCircle2 size={17} />}
              <span>{message.text}</span>
            </div>
          )}

          <form onSubmit={handleRegister}>
            <div className="auth-input-group">
              <label className="auth-input-label">FULL NAME</label>
              <div className="auth-input-wrapper">
                <User size={17} className="input-icon-left" />
                <input
                  type="text"
                  className="auth-text-input"
                  placeholder="e.g. John Doe"
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  required
                />
              </div>
            </div>

            <div className="auth-input-group">
              <label className="auth-input-label">EMAIL ADDRESS</label>
              <div className="auth-input-wrapper">
                <Mail size={17} className="input-icon-left" />
                <input
                  type="email"
                  className="auth-text-input"
                  placeholder="name@example.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>
            </div>

            <div className="auth-input-group">
              <label className="auth-input-label">PASSWORD</label>
              <div className="auth-input-wrapper">
                <Lock size={17} className="input-icon-left" />
                <input
                  type={showPassword ? "text" : "password"}
                  className="auth-text-input"
                  placeholder="At least 6 characters"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  minLength={6}
                  required
                />
                <button
                  type="button"
                  className="input-eye-toggle"
                  onClick={() => setShowPassword(!showPassword)}
                  title={showPassword ? "Hide password" : "Show password"}
                >
                  {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
            </div>

            <button
              type="submit"
              className="btn-auth-primary"
              disabled={loading}
              style={{ marginTop: "10px" }}
            >
              <span>{loading ? "Registering..." : "Create Account"}</span>
              <ArrowRight size={17} />
            </button>
          </form>

          <div className="auth-divider">
            <span>OR</span>
          </div>

          <button
            type="button"
            className="btn-auth-secondary"
            onClick={() => navigate("/")}
          >
            Already have an account? Sign In
          </button>

          <footer className="auth-security-footer">
            <div className="auth-security-badge">
              <Lock size={13} />
              <span>256-bit encrypted • Secure Vault</span>
            </div>
            <a href="#privacy" className="auth-policy-link" onClick={(e) => e.preventDefault()}>
              Privacy Policy
            </a>
          </footer>
        </div>
      </div>
    </div>
  );
}

export default Register;