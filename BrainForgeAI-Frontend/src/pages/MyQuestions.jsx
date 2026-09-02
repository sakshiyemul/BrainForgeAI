import React, { useEffect, useState } from "react";
import {
  Plus,
  Edit3,
  Trash2,
  CheckCircle2,
  AlertCircle,
  RefreshCw,
  Database
} from "lucide-react";

function MyQuestions() {
  const [questions, setQuestions] = useState([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [question, setQuestion] = useState("");
  const [answer, setAnswer] = useState("");

  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState({ text: "", type: "" });

  const token = localStorage.getItem("token");

  const fetchQuestions = async () => {
    try {
      const response = await fetch("http://localhost:8080/questions/my", {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      const data = await response.json();

      if (!response.ok) {
        setMessage({ text: data.message || "Unable to load questions", type: "error" });
        return;
      }

      setQuestions(data);
    } catch (error) {
      setMessage({ text: "Unable to connect to server", type: "error" });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchQuestions();
  }, []);

  const saveQuestion = async (e) => {
    e.preventDefault();
    if (!question.trim() || !answer.trim()) return;

    setSubmitting(true);
    setMessage({ text: "", type: "" });

    try {
      const url =
        editingId !== null
          ? `http://localhost:8080/questions/${editingId}`
          : "http://localhost:8080/questions";

      const method = editingId !== null ? "PUT" : "POST";

      const response = await fetch(url, {
        method,
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          question: question.trim(),
          answer: answer.trim(),
        }),
      });

      const data = await response.json();

      if (!response.ok) {
        setMessage({ text: data.message || "Unable to save question", type: "error" });
        setSubmitting(false);
        return;
      }

      if (editingId !== null) {
        setQuestions(questions.map((q) => (q.id === editingId ? data : q)));
        setMessage({ text: "Question updated successfully.", type: "success" });
      } else {
        setQuestions([data, ...questions]);
        setMessage({ text: "Question added successfully.", type: "success" });
      }

      setQuestion("");
      setAnswer("");
      setEditingId(null);
    } catch (error) {
      setMessage({ text: "Network error while saving question", type: "error" });
    } finally {
      setSubmitting(false);
    }
  };

  const editQuestion = (q) => {
    setEditingId(q.id);
    setQuestion(q.question);
    setAnswer(q.answer);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const cancelEdit = () => {
    setEditingId(null);
    setQuestion("");
    setAnswer("");
  };

  const deleteQuestion = async (id) => {
    if (!window.confirm("Are you sure you want to delete this question?")) return;

    try {
      const response = await fetch(`http://localhost:8080/questions/${id}`, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        let data = {};
        try {
          data = await response.json();
        } catch {}
        setMessage({ text: data.message || "Unable to delete question", type: "error" });
        return;
      }

      setQuestions(questions.filter((q) => q.id !== id));
      setMessage({ text: "Question deleted.", type: "success" });
    } catch (error) {
      setMessage({ text: "Failed to delete question", type: "error" });
    }
  };

  const filteredQuestions = questions.filter(
    (q) =>
      q.question?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      q.answer?.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="questions-section">
      <div style={{ marginBottom: "16px" }}>
        <h2>Curated Q&amp;A</h2>
        <p style={{ fontSize: "13.5px", color: "var(--text-secondary)", marginTop: "2px" }}>
          Save frequently referenced questions and answers for quick lookup.
        </p>
      </div>

      {message.text && (
        <div
          className={`admin-message-banner ${
            message.type === "error" ? "banner-error" : "banner-success"
          }`}
          style={{ marginBottom: "16px" }}
        >
          {message.type === "error" ? <AlertCircle size={16} /> : <CheckCircle2 size={16} />}
          <span>{message.text}</span>
        </div>
      )}

      {/* Question Form Card */}
      <form onSubmit={saveQuestion} className="question-form">
        <h3 style={{ fontSize: "14px", fontWeight: 600, color: "var(--text-primary)" }}>
          {editingId !== null ? "Edit Question" : "Add New Question"}
        </h3>

        <input
          type="text"
          placeholder="Question title or prompt..."
          value={question}
          onChange={(e) => setQuestion(e.target.value)}
          required
        />

        <textarea
          placeholder="Answer or reference text..."
          value={answer}
          onChange={(e) => setAnswer(e.target.value)}
          rows={3}
          required
        />

        <div style={{ display: "flex", gap: "8px", marginTop: "2px" }}>
          <button type="submit" disabled={submitting}>
            {submitting ? (
              <RefreshCw size={14} className="animate-spin" />
            ) : editingId !== null ? (
              "Update Question"
            ) : (
              "Save Question"
            )}
          </button>

          {editingId !== null && (
            <button type="button" onClick={cancelEdit} className="cancel-button">
              Cancel
            </button>
          )}
        </div>
      </form>

      {/* Search Input */}
      <div style={{ marginBottom: "16px" }}>
        <input
          type="text"
          className="auth-text-input"
          placeholder="Search questions..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
        />
      </div>

      {loading ? (
        <div style={{ display: "flex", alignItems: "center", gap: "8px", padding: "30px 0" }}>
          <RefreshCw size={16} className="animate-spin" style={{ color: "var(--accent-primary)" }} />
          <span style={{ fontSize: "13px", color: "var(--text-secondary)" }}>Loading questions...</span>
        </div>
      ) : filteredQuestions.length === 0 ? (
        <div className="chat-welcome-box" style={{ maxWidth: "450px" }}>
          <div className="welcome-icon-glow">
            <Database size={20} />
          </div>
          <h3>No Questions Found</h3>
          <p>
            {questions.length === 0
              ? "You have not saved any curated questions yet. Use the form above to add your first entry."
              : "No questions matched your search query."}
          </p>
        </div>
      ) : (
        <div className="question-list">
          {filteredQuestions.map((q) => (
            <div className="question-card" key={q.id}>
              <h3>{q.question}</h3>
              <p>{q.answer}</p>
              <div className="question-actions">
                <button type="button" onClick={() => editQuestion(q)}>
                  <Edit3 size={12} style={{ marginRight: "4px" }} />
                  Edit
                </button>
                <button type="button" onClick={() => deleteQuestion(q.id)}>
                  <Trash2 size={12} style={{ marginRight: "4px" }} />
                  Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default MyQuestions;