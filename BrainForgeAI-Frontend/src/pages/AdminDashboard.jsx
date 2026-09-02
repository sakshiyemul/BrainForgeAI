import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Shield,
  Users,
  FileText,
  MessageSquare,
  Zap,
  Trash2,
  ArrowLeft,
  LogOut,
  RefreshCw,
  AlertCircle,
  CheckCircle2,
  Search
} from "lucide-react";
import ThemeToggle from "../components/ThemeToggle";

function AdminDashboard() {
  const navigate = useNavigate();

  const [users, setUsers] = useState([]);
  const [questions, setQuestions] = useState([]);
  const [adminAnalytics, setAdminAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [message, setMessage] = useState({ text: "", type: "" });

  const [userSearch, setUserSearch] = useState("");
  const [questionSearch, setQuestionSearch] = useState("");

  const token = localStorage.getItem("token");
  const user = JSON.parse(localStorage.getItem("user") || "null");

  useEffect(() => {
    if (!token || user?.role !== "ADMIN") {
      navigate("/");
      return;
    }
    fetchAdminData(false);
  }, []);

  const showNotification = (text, type = "success") => {
    setMessage({ text, type });
  };

  const fetchAdminData = async (isManualRefresh = false) => {
    if (isManualRefresh) {
      setRefreshing(true);
    } else {
      setLoading(true);
    }

    try {
      const [usersRes, questionsRes, analyticsRes] = await Promise.all([
        fetch("http://localhost:8080/users/all", { headers: { Authorization: `Bearer ${token}` } }),
        fetch("http://localhost:8080/questions/all", { headers: { Authorization: `Bearer ${token}` } }),
        fetch("http://localhost:8080/analytics/admin", { headers: { Authorization: `Bearer ${token}` } }),
      ]);

      if (usersRes.status === 401 || questionsRes.status === 401 || analyticsRes.status === 401) {
        showNotification("Session expired. Please login again.", "error");
        return;
      }

      if (usersRes.status === 403 || questionsRes.status === 403 || analyticsRes.status === 403) {
        showNotification("Access denied: Admin role required.", "error");
        return;
      }

      if (usersRes.ok) {
        const loadedUsers = await usersRes.json();
        setUsers(loadedUsers);
      }

      if (questionsRes.ok) {
        const loadedQuestions = await questionsRes.json();
        setQuestions(loadedQuestions);
      }

      if (analyticsRes.ok) {
        const loadedAnalytics = await analyticsRes.json();
        setAdminAnalytics(loadedAnalytics);
      }

      if (isManualRefresh) {
        showNotification("Admin data refreshed successfully.", "success");
      }
    } catch (err) {
      showNotification("Unable to connect to the server.", "error");
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  const deleteUser = async (targetUser) => {
    if (targetUser.id === user?.id || targetUser.email === user?.email) {
      showNotification("You cannot delete your own logged-in admin account.", "error");
      return;
    }

    const userName = targetUser.fullName || targetUser.email || `User #${targetUser.id}`;
    if (
      !window.confirm(
        `Are you sure you want to delete "${userName}"? All associated data will also be removed.`
      )
    ) {
      return;
    }

    try {
      const res = await fetch(`http://localhost:8080/users/${targetUser.id}`, {
        method: "DELETE",
        headers: { Authorization: `Bearer ${token}` },
      });

      if (res.ok) {
        showNotification(`User "${userName}" deleted successfully.`, "success");
        fetchAdminData(false);
      } else if (res.status === 401) {
        showNotification("Session expired. Please login again.", "error");
      } else if (res.status === 403) {
        showNotification("Access denied: Admin role required.", "error");
      } else if (res.status === 404) {
        showNotification("User not found or already deleted.", "error");
        fetchAdminData(false);
      } else {
        showNotification("Failed to delete user", "error");
      }
    } catch (err) {
      showNotification("Failed to delete user: Network error", "error");
    }
  };

  const deleteQuestion = async (targetQuestion) => {
    if (!window.confirm("Are you sure you want to delete this question?")) return;

    try {
      const res = await fetch(`http://localhost:8080/questions/${targetQuestion.id}`, {
        method: "DELETE",
        headers: { Authorization: `Bearer ${token}` },
      });

      if (res.ok) {
        showNotification("Question deleted successfully.", "success");
        fetchAdminData(false);
      } else if (res.status === 401) {
        showNotification("Session expired. Please login again.", "error");
      } else {
        let errorMsg = "Failed to delete question.";
        try {
          const errData = await res.json();
          if (errData.message) errorMsg = errData.message;
        } catch {}
        showNotification(errorMsg, "error");
      }
    } catch (err) {
      showNotification("Failed to delete question: Network error", "error");
    }
  };

  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("user");
    navigate("/");
  };

  const filteredUsers = users.filter(
    (u) =>
      (u.fullName || "").toLowerCase().includes(userSearch.toLowerCase()) ||
      (u.email || "").toLowerCase().includes(userSearch.toLowerCase()) ||
      (u.role || "").toLowerCase().includes(userSearch.toLowerCase())
  );

  const filteredQuestions = questions.filter(
    (q) =>
      (q.question || "").toLowerCase().includes(questionSearch.toLowerCase()) ||
      (q.answer || "").toLowerCase().includes(questionSearch.toLowerCase())
  );

  return (
    <div className="admin-page-container">
      {/* Top Header */}
      <header className="admin-header">
        <div className="admin-header-left">
          <button className="back-btn" onClick={() => navigate("/dashboard")}>
            <ArrowLeft size={15} />
            <span>Back to App</span>
          </button>
          <div className="admin-title-box">
            <h2>Admin Dashboard</h2>
          </div>
        </div>

        <div className="admin-header-right">
          <ThemeToggle />
          <button
            className="refresh-admin-btn"
            onClick={() => fetchAdminData(true)}
            disabled={refreshing || loading}
            title="Reload data"
          >
            <RefreshCw size={14} className={refreshing || loading ? "animate-spin" : ""} />
            <span>{refreshing ? "Refreshing..." : "Refresh"}</span>
          </button>
          <button className="admin-logout-btn" onClick={logout} title="Sign Out">
            <LogOut size={15} />
            <span>Logout</span>
          </button>
        </div>
      </header>

      {/* Admin Content Area */}
      <main className="admin-content-area">
        {message.text && (
          <div
            className={`admin-message-banner ${
              message.type === "error" ? "banner-error" : "banner-success"
            }`}
          >
            {message.type === "error" ? <AlertCircle size={16} /> : <CheckCircle2 size={16} />}
            <span>{message.text}</span>
          </div>
        )}

        {/* Stats Grid */}
        <section className="admin-stats-grid">
          <div className="admin-stat-card">
            <div className="stat-icon-wrapper bg-blue">
              <Users size={18} />
            </div>
            <div className="stat-info">
              <span className="metric-label">Registered Users</span>
              <h3 className="metric-value">{adminAnalytics?.totalUsers ?? users.length}</h3>
            </div>
          </div>

          <div className="admin-stat-card">
            <div className="stat-icon-wrapper bg-indigo">
              <MessageSquare size={18} />
            </div>
            <div className="stat-info">
              <span className="metric-label">Conversations</span>
              <h3 className="metric-value">{adminAnalytics?.totalConversations ?? 0}</h3>
            </div>
          </div>

          <div className="admin-stat-card">
            <div className="stat-icon-wrapper bg-purple">
              <FileText size={18} />
            </div>
            <div className="stat-info">
              <span className="metric-label">Documents</span>
              <h3 className="metric-value">{adminAnalytics?.totalDocuments ?? 0}</h3>
            </div>
          </div>

          <div className="admin-stat-card">
            <div className="stat-icon-wrapper bg-amber">
              <Zap size={18} />
            </div>
            <div className="stat-info">
              <span className="metric-label">Tokens Consumed</span>
              <h3 className="metric-value">
                {adminAnalytics?.totalTokensConsumed?.toLocaleString() ?? 0}
              </h3>
            </div>
          </div>
        </section>

        {/* User Management Panel */}
        <section className="admin-panel-card">
          <div
            className="panel-card-header"
            style={{
              display: "flex",
              alignItems: "center",
              justifyContent: "space-between",
              gap: "16px",
            }}
          >
            <h3>User Accounts ({users.length})</h3>
            <input
              type="text"
              className="auth-text-input"
              placeholder="Search users..."
              value={userSearch}
              onChange={(e) => setUserSearch(e.target.value)}
              style={{ maxWidth: "220px", padding: "6px 10px", fontSize: "12.5px" }}
            />
          </div>

          <div className="admin-table-container">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Full Name</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredUsers.map((u) => {
                  const isCurrentLoggedInUser = u.id === user?.id || u.email === user?.email;
                  return (
                    <tr key={u.id}>
                      <td>#{u.id}</td>
                      <td>
                        <strong>{u.fullName}</strong>
                        {isCurrentLoggedInUser && (
                          <span
                            style={{
                              marginLeft: "6px",
                              fontSize: "11px",
                              color: "var(--accent-primary)",
                              fontWeight: 600,
                            }}
                          >
                            (You)
                          </span>
                        )}
                      </td>
                      <td>{u.email}</td>
                      <td>
                        <span
                          className={`role-tag ${
                            u.role === "ADMIN" ? "role-admin" : "role-user"
                          }`}
                        >
                          {u.role}
                        </span>
                      </td>
                      <td>
                        <button
                          className="table-delete-btn"
                          onClick={() => deleteUser(u)}
                          disabled={isCurrentLoggedInUser}
                          title={
                            isCurrentLoggedInUser
                              ? "Cannot delete your own logged-in account"
                              : "Delete User"
                          }
                          style={
                            isCurrentLoggedInUser
                              ? { opacity: 0.3, cursor: "not-allowed" }
                              : {}
                          }
                        >
                          <Trash2 size={13} />
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </section>

        {/* Curated Q&A Panel */}
        <section className="admin-panel-card">
          <div
            className="panel-card-header"
            style={{
              display: "flex",
              alignItems: "center",
              justifyContent: "space-between",
              gap: "16px",
            }}
          >
            <h3>Curated Questions ({questions.length})</h3>
            <input
              type="text"
              className="auth-text-input"
              placeholder="Search questions..."
              value={questionSearch}
              onChange={(e) => setQuestionSearch(e.target.value)}
              style={{ maxWidth: "220px", padding: "6px 10px", fontSize: "12.5px" }}
            />
          </div>

          <div className="admin-table-container">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Question</th>
                  <th>Answer</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredQuestions.map((q) => (
                  <tr key={q.id}>
                    <td>#{q.id}</td>
                    <td className="table-truncate-cell" title={q.question}>
                      {q.question}
                    </td>
                    <td className="table-truncate-cell" title={q.answer}>
                      {q.answer}
                    </td>
                    <td>
                      <button
                        className="table-delete-btn"
                        onClick={() => deleteQuestion(q)}
                        title="Delete Question"
                      >
                        <Trash2 size={13} />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      </main>
    </div>
  );
}

export default AdminDashboard;