import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Plus,
  MessageSquare,
  FileText,
  Database,
  BarChart2,
  Shield,
  LogOut,
  Trash2
} from "lucide-react";
import ThemeToggle from "./ThemeToggle";

function Sidebar({
  activeTab,
  setActiveTab,
  conversations,
  currentConvId,
  onSelectConversation,
  onNewChat,
  onDeleteConversation,
  user,
  onLogout,
}) {
  const [convSearch, setConvSearch] = useState("");
  const navigate = useNavigate();

  const filteredConversations = conversations.filter((c) =>
    (c.title || "").toLowerCase().includes(convSearch.toLowerCase())
  );

  return (
    <aside className="sidebar">
      {/* Top Header */}
      <div className="sidebar-header">
        <div className="sidebar-logo">
          <div className="sidebar-logo-icon">
            <MessageSquare size={16} />
          </div>
          <div className="sidebar-logo-text">
            <h3>BrainForge AI</h3>
          </div>
        </div>

        <button className="new-chat-btn" onClick={onNewChat}>
          <Plus size={16} />
          <span>New Chat</span>
        </button>
      </div>

      {/* Main Navigation */}
      <div className="sidebar-nav-section">
        <div className="nav-label">Navigation</div>

        <button
          className={`nav-btn ${activeTab === "chat" ? "active" : ""}`}
          onClick={() => setActiveTab("chat")}
        >
          <MessageSquare size={16} />
          <span>Chat</span>
        </button>

        <button
          className={`nav-btn ${activeTab === "knowledge" ? "active" : ""}`}
          onClick={() => setActiveTab("knowledge")}
        >
          <FileText size={16} />
          <span>Documents</span>
        </button>

        <button
          className={`nav-btn ${activeTab === "curated" ? "active" : ""}`}
          onClick={() => setActiveTab("curated")}
        >
          <Database size={16} />
          <span>Curated Q&amp;A</span>
        </button>

        <button
          className={`nav-btn ${activeTab === "analytics" ? "active" : ""}`}
          onClick={() => setActiveTab("analytics")}
        >
          <BarChart2 size={16} />
          <span>Analytics</span>
        </button>

        {user?.role === "ADMIN" && (
          <button
            className={`nav-btn admin-nav ${activeTab === "admin" ? "active" : ""}`}
            onClick={() => navigate("/admin")}
          >
            <Shield size={16} />
            <span>Admin Panel</span>
          </button>
        )}
      </div>

      {/* Recent Conversations */}
      <div className="sidebar-conversations-section">
        <div className="nav-label">Recent Chats ({conversations.length})</div>

        {conversations.length > 3 && (
          <input
            type="text"
            className="conv-search-input"
            placeholder="Search chats..."
            value={convSearch}
            onChange={(e) => setConvSearch(e.target.value)}
          />
        )}

        <div className="conversation-list">
          {filteredConversations.length === 0 ? (
            <div className="empty-conv-text">
              {conversations.length === 0
                ? "No chat history yet."
                : "No matching chats found."}
            </div>
          ) : (
            filteredConversations.map((conv) => (
              <div
                key={conv.id}
                className={`conv-item ${
                  currentConvId === conv.id && activeTab === "chat" ? "selected" : ""
                }`}
                onClick={() => onSelectConversation(conv.id)}
              >
                <div className="conv-item-left">
                  <MessageSquare size={13} style={{ color: "var(--text-muted)", flexShrink: 0 }} />
                  <span className="conv-title" title={conv.title}>
                    {conv.title || "New Chat"}
                  </span>
                </div>

                <button
                  className="conv-delete-btn"
                  onClick={(e) => {
                    e.stopPropagation();
                    onDeleteConversation(conv.id);
                  }}
                  title="Delete chat"
                >
                  <Trash2 size={13} />
                </button>
              </div>
            ))
          )}
        </div>
      </div>

      {/* Sidebar Footer */}
      <div className="sidebar-footer">
        <div className="user-profile">
          <div className="user-avatar">
            {user?.fullName ? user.fullName.charAt(0).toUpperCase() : "U"}
          </div>
          <div className="user-info">
            <span className="user-name" title={user?.fullName || user?.email}>
              {user?.fullName || user?.email || "User"}
            </span>
            <span className="user-role-badge">
              {user?.role === "ADMIN" ? "Admin" : "Member"}
            </span>
          </div>
        </div>

        <div className="sidebar-footer-actions">
          <ThemeToggle />
          <button className="logout-icon-btn" onClick={onLogout} title="Sign Out">
            <LogOut size={15} />
          </button>
        </div>
      </div>
    </aside>
  );
}

export default Sidebar;
