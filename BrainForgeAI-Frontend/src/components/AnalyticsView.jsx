import React, { useState, useEffect } from "react";
import {
  BarChart2,
  MessageSquare,
  FileText,
  Zap,
  Database,
  RefreshCw,
  Clock
} from "lucide-react";

function AnalyticsView({ token }) {
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  const fetchAnalytics = async (isManual = false) => {
    if (isManual) setRefreshing(true);
    try {
      const res = await fetch("http://localhost:8080/analytics/user", {
        headers: { Authorization: `Bearer ${token}` },
      });
      const data = await res.json();
      if (res.ok) {
        setAnalytics(data);
      }
    } catch (err) {
      console.error("Failed to load user analytics", err);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => {
    fetchAnalytics(false);
  }, []);

  if (loading) {
    return (
      <div
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          gap: "8px",
          padding: "50px 0",
          color: "var(--text-secondary)",
          fontSize: "13.5px",
        }}
      >
        <RefreshCw size={16} className="animate-spin" style={{ color: "var(--accent-primary)" }} />
        <span>Loading analytics...</span>
      </div>
    );
  }

  const formatAction = (action) => {
    switch (action?.toUpperCase()) {
      case "CHAT":
        return "Chat Message";
      case "STREAM_CHAT":
        return "Streaming Chat";
      case "DOC_UPLOAD":
        return "Document Upload";
      case "RAG_DOC_QUERY":
        return "Document Search";
      default:
        return action || "Activity";
    }
  };

  return (
    <div className="analytics-container">
      {/* Header */}
      <header className="analytics-header">
        <div>
          <h2>Analytics</h2>
          <p>Overview of your conversations, documents, and token usage.</p>
        </div>
        <button
          className="refresh-admin-btn"
          onClick={() => fetchAnalytics(true)}
          disabled={refreshing}
          title="Reload analytics"
        >
          <RefreshCw size={14} className={refreshing ? "animate-spin" : ""} />
          <span>{refreshing ? "Refreshing..." : "Refresh"}</span>
        </button>
      </header>

      {/* Metrics Summary Grid */}
      <section className="metrics-grid">
        <div className="metric-card">
          <div className="metric-icon-box bg-blue">
            <MessageSquare size={18} />
          </div>
          <div className="metric-info">
            <span className="metric-label">Conversations</span>
            <h3 className="metric-value">{analytics?.totalConversations || 0}</h3>
          </div>
        </div>

        <div className="metric-card">
          <div className="metric-icon-box bg-emerald">
            <MessageSquare size={18} />
          </div>
          <div className="metric-info">
            <span className="metric-label">Messages</span>
            <h3 className="metric-value">{analytics?.totalMessages || 0}</h3>
          </div>
        </div>

        <div className="metric-card">
          <div className="metric-icon-box bg-purple">
            <FileText size={18} />
          </div>
          <div className="metric-info">
            <span className="metric-label">Documents</span>
            <h3 className="metric-value">{analytics?.totalDocuments || 0}</h3>
          </div>
        </div>

        <div className="metric-card">
          <div className="metric-icon-box bg-amber">
            <Zap size={18} />
          </div>
          <div className="metric-info">
            <span className="metric-label">Tokens Used</span>
            <h3 className="metric-value">
              {analytics?.totalTokensUsed?.toLocaleString() || 0}
            </h3>
          </div>
        </div>

        <div className="metric-card">
          <div className="metric-icon-box bg-cyan">
            <Database size={18} />
          </div>
          <div className="metric-info">
            <span className="metric-label">Curated Q&amp;As</span>
            <h3 className="metric-value">{analytics?.totalSavedQuestions || 0}</h3>
          </div>
        </div>
      </section>

      {/* Activity Timeline */}
      <section className="activity-section">
        <h3>Recent Activity</h3>

        {analytics?.recentActivity && analytics.recentActivity.length > 0 ? (
          <div className="activity-list">
            {analytics.recentActivity.map((item, idx) => (
              <div key={idx} className="activity-item">
                <div className="activity-main">
                  <span className={`activity-badge badge-${item.action?.toLowerCase()}`}>
                    {formatAction(item.action)}
                  </span>
                  <span className="activity-details">{item.details}</span>
                </div>
                <div className="activity-meta">
                  <span className="token-tag">~{item.tokens} tokens</span>
                  <span>
                    {new Date(item.timestamp).toLocaleTimeString([], {
                      hour: "2-digit",
                      minute: "2-digit",
                    })}
                  </span>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="empty-conv-text" style={{ padding: "24px 0" }}>
            No recent activity recorded yet.
          </div>
        )}
      </section>
    </div>
  );
}

export default AnalyticsView;
