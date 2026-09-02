import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import ChatView from "../components/ChatView";
import KnowledgeVaultView from "../components/KnowledgeVaultView";
import AnalyticsView from "../components/AnalyticsView";
import MyQuestions from "./MyQuestions";

function Dashboard() {
  const navigate = useNavigate();
  const token = localStorage.getItem("token");
  const user = JSON.parse(localStorage.getItem("user") || "null");

  const [activeTab, setActiveTab] = useState("chat");
  const [conversations, setConversations] = useState([]);
  const [currentConvId, setCurrentConvId] = useState(null);
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(false);
  const [streamingContent, setStreamingContent] = useState("");
  const [useKnowledgeBase, setUseKnowledgeBase] = useState(false);

  useEffect(() => {
    if (!token || !user) {
      navigate("/");
      return;
    }
    fetchConversations();
  }, []);

  const fetchConversations = async () => {
    try {
      const res = await fetch("https://brainforgeai-backend.onrender.com/chat/conversations", {
        headers: { Authorization: `Bearer ${token}` }
      });
      const data = await res.json();
      if (res.ok) {
        setConversations(data);
        if (data.length > 0 && !currentConvId) {
          selectConversation(data[0].id);
        }
      }
    } catch (err) {
      console.error("Failed to load conversations", err);
    }
  };

  const selectConversation = async (convId) => {
    setCurrentConvId(convId);
    setActiveTab("chat");
    setStreamingContent("");
    try {
      const res = await fetch(`https://brainforgeai-backend.onrender.com/chat/conversations/${convId}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      const data = await res.json();
      if (res.ok) {
        setMessages(data);
      }
    } catch (err) {
      console.error("Failed to fetch messages", err);
    }
  };

  const handleNewChat = async () => {
    try {
      const res = await fetch("https://brainforgeai-backend.onrender.com/chat/conversations", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify({ title: "New Conversation" })
      });
      const data = await res.json();
      if (res.ok) {
        setConversations([data, ...conversations]);
        setCurrentConvId(data.id);
        setMessages([]);
        setActiveTab("chat");
        setStreamingContent("");
      }
    } catch (err) {
      console.error("Failed to create conversation", err);
    }
  };

  const handleDeleteConversation = async (convId) => {
    try {
      const res = await fetch(`https://brainforgeai-backend.onrender.com/chat/conversations/${convId}`, {
        method: "DELETE",
        headers: { Authorization: `Bearer ${token}` }
      });
      if (res.ok) {
        const remaining = conversations.filter((c) => c.id !== convId);
        setConversations(remaining);
        if (currentConvId === convId) {
          if (remaining.length > 0) {
            selectConversation(remaining[0].id);
          } else {
            setCurrentConvId(null);
            setMessages([]);
          }
        }
      }
    } catch (err) {
      console.error("Failed to delete conversation", err);
    }
  };

  const handleSendMessage = async (prompt) => {
    let targetConvId = currentConvId;

    // Create a conversation on the fly if none exists
    if (!targetConvId) {
      try {
        const res = await fetch("https://brainforgeai-backend.onrender.com/chat/conversations", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`
          },
          body: JSON.stringify({ title: prompt.substring(0, 30) })
        });
        const newConv = await res.json();
        if (res.ok) {
          targetConvId = newConv.id;
          setCurrentConvId(newConv.id);
          setConversations([newConv, ...conversations]);
        }
      } catch (err) {
        console.error("Failed to initialize conversation", err);
        return;
      }
    }

    // Optimistically add user message
    const userMsg = { id: Date.now(), role: "user", content: prompt, createdAt: new Date() };
    setMessages((prev) => [...prev, userMsg]);
    setLoading(true);
    setStreamingContent("");

    // Use SSE Streaming
    try {
      const url = `https://brainforgeai-backend.onrender.com/chat/conversations/${targetConvId}/stream?prompt=${encodeURIComponent(prompt)}&useKnowledgeBase=${useKnowledgeBase}`;
      const response = await fetch(url, {
        headers: { Authorization: `Bearer ${token}` }
      });

      if (!response.ok) {
        throw new Error("Streaming request failed");
      }

      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let accumulated = "";

      while (true) {
        const { value, done } = await reader.read();
        if (done) break;

        const text = decoder.decode(value, { stream: true });
        const lines = text.split("\n");

        for (const line of lines) {
          if (line.startsWith("data:")) {
            const data = line.replace("data:", "");
            if (data.trim() === "[DONE]") {
              // Finish stream
              break;
            } else {
              accumulated += data;
              setStreamingContent(accumulated);
            }
          }
        }
      }

      // Add final assistant message to history
      const assistantMsg = {
        id: Date.now() + 1,
        role: "assistant",
        content: accumulated,
        createdAt: new Date()
      };
      setMessages((prev) => [...prev, assistantMsg]);
      setStreamingContent("");
      fetchConversations(); // refresh title & order
    } catch (err) {
      console.error("Streaming error, falling back to sync API", err);
      // Fallback to synchronous endpoint
      try {
        const res = await fetch(`https://brainforgeai-backend.onrender.com/chat/conversations/${targetConvId}/messages`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`
          },
          body: JSON.stringify({ content: prompt, useKnowledgeBase })
        });
        const data = await res.json();
        if (res.ok) {
          setMessages((prev) => [...prev, data]);
          fetchConversations();
        }
      } catch (fallbackErr) {
        console.error("Sync message send failed", fallbackErr);
      }
    } finally {
      setLoading(false);
      setStreamingContent("");
    }
  };

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("user");
    navigate("/");
  };

  const currentConversation = conversations.find((c) => c.id === currentConvId);

  return (
    <div className="app-workspace">
      <Sidebar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        conversations={conversations}
        currentConvId={currentConvId}
        onSelectConversation={selectConversation}
        onNewChat={handleNewChat}
        onDeleteConversation={handleDeleteConversation}
        user={user}
        onLogout={handleLogout}
      />

      <main className="main-content-panel">
        {activeTab === "chat" && (
          <ChatView
            conversation={currentConversation}
            messages={messages}
            onSendMessage={handleSendMessage}
            loading={loading}
            streamingContent={streamingContent}
            useKnowledgeBase={useKnowledgeBase}
            setUseKnowledgeBase={setUseKnowledgeBase}
          />
        )}

        {activeTab === "knowledge" && (
          <KnowledgeVaultView token={token} />
        )}

        {activeTab === "curated" && (
          <MyQuestions />
        )}

        {activeTab === "analytics" && (
          <AnalyticsView token={token} />
        )}
      </main>
    </div>
  );
}

export default Dashboard;