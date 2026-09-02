import React, { useState, useEffect, useRef } from "react";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import {
  Send,
  Bot,
  User,
  FileText,
  Copy,
  Check,
  RefreshCw,
  MessageSquare
} from "lucide-react";

function ChatView({
  conversation,
  messages,
  onSendMessage,
  loading,
  streamingContent,
  useKnowledgeBase,
  setUseKnowledgeBase,
}) {
  const [input, setInput] = useState("");
  const [copiedId, setCopiedId] = useState(null);
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, streamingContent]);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!input.trim() || loading) return;
    onSendMessage(input.trim());
    setInput("");
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSubmit(e);
    }
  };

  const copyToClipboard = (text, id) => {
    navigator.clipboard.writeText(text);
    setCopiedId(id);
    setTimeout(() => setCopiedId(null), 2000);
  };

  const suggestedPrompts = [
    {
      title: "Document Search",
      text: "Summarize the key takeaways from my uploaded documents.",
    },
    {
      title: "Backend Security",
      text: "Explain how JWT tokens and refresh tokens work together.",
    },
    {
      title: "Code Example",
      text: "Write an example REST controller in Spring Boot.",
    },
    {
      title: "Database Performance",
      text: "What are best practices for database indexing and query optimization?",
    },
  ];

  return (
    <div className="chat-container">
      {/* Header */}
      <header className="chat-header">
        <div className="chat-title-box">
          <h2>{conversation?.title || "New Chat"}</h2>
        </div>

        <div className="chat-header-actions">
          <button
            type="button"
            className={`rag-toggle-btn ${useKnowledgeBase ? "active" : ""}`}
            onClick={() => setUseKnowledgeBase(!useKnowledgeBase)}
            title="Search answers inside uploaded documents"
          >
            <FileText size={15} />
            <span>Search Documents</span>
          </button>
        </div>
      </header>

      {/* Messages Scroll Area */}
      <div className="chat-messages-area">
        {messages.length === 0 && !streamingContent ? (
          <div className="chat-welcome-box">
            <div className="welcome-icon-glow">
              <MessageSquare size={24} />
            </div>
            <h3>How can I help you today?</h3>
            <p>
              Ask any question, generate code, or enable{" "}
              <strong>Search Documents</strong> to search your uploaded files.
            </p>

            <div className="suggested-prompts-grid">
              {suggestedPrompts.map((item, idx) => (
                <button
                  key={idx}
                  className="suggested-prompt-chip"
                  onClick={() => onSendMessage(item.text)}
                >
                  <div>
                    <span
                      style={{
                        display: "block",
                        fontSize: "11px",
                        fontWeight: 600,
                        color: "var(--accent-primary)",
                        marginBottom: "2px",
                      }}
                    >
                      {item.title}
                    </span>
                    <span>{item.text}</span>
                  </div>
                </button>
              ))}
            </div>
          </div>
        ) : (
          messages.map((msg, index) => (
            <div
              key={msg.id || index}
              className={`message-row ${msg.role === "user" ? "user-row" : "assistant-row"}`}
            >
              <div className="message-avatar">
                {msg.role === "user" ? <User size={15} /> : <Bot size={15} />}
              </div>
              <div className="message-bubble">
                <div className="message-header">
                  <span className="sender-name">
                    {msg.role === "user" ? "You" : "BrainForge"}
                  </span>
                  {msg.role === "assistant" && (
                    <button
                      className="copy-btn"
                      onClick={() => copyToClipboard(msg.content, msg.id || index)}
                      title="Copy response"
                    >
                      {copiedId === (msg.id || index) ? (
                        <Check size={13} className="text-green" />
                      ) : (
                        <Copy size={13} />
                      )}
                    </button>
                  )}
                </div>
                <div className="markdown-content">
                  <ReactMarkdown remarkPlugins={[remarkGfm]}>
                    {msg.content}
                  </ReactMarkdown>
                </div>
              </div>
            </div>
          ))
        )}

        {/* Streaming Content */}
        {streamingContent && (
          <div className="message-row assistant-row streaming-row">
            <div className="message-avatar">
              <Bot size={15} />
            </div>
            <div className="message-bubble">
              <div className="message-header">
                <span className="sender-name">BrainForge</span>
                <span className="streaming-badge">Generating...</span>
              </div>
              <div className="markdown-content">
                <ReactMarkdown remarkPlugins={[remarkGfm]}>
                  {streamingContent}
                </ReactMarkdown>
              </div>
            </div>
          </div>
        )}

        {/* Loading Indicator */}
        {loading && !streamingContent && (
          <div className="message-row assistant-row loading-row">
            <div className="message-avatar">
              <Bot size={15} />
            </div>
            <div className="message-bubble" style={{ display: "flex", alignItems: "center", gap: "8px" }}>
              <RefreshCw size={14} className="animate-spin" style={{ color: "var(--accent-primary)" }} />
              <span style={{ fontSize: "13px", color: "var(--text-secondary)" }}>
                Thinking...
              </span>
            </div>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>

      {/* Floating Chat Input Bar */}
      <footer className="chat-input-wrapper">
        {useKnowledgeBase && (
          <div className="active-rag-banner">
            <FileText size={13} />
            <span>Document search is active for your questions</span>
          </div>
        )}
        <form onSubmit={handleSubmit} className="chat-form">
          <textarea
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="Send a message... (Enter to send, Shift + Enter for new line)"
            rows={1}
            disabled={loading}
          />
          <button
            type="submit"
            className="send-btn"
            disabled={!input.trim() || loading}
            title="Send Message"
          >
            {loading ? <RefreshCw size={14} className="animate-spin" /> : <Send size={14} />}
          </button>
        </form>
      </footer>
    </div>
  );
}

export default ChatView;
