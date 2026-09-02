import React, { useState, useEffect } from "react";
import {
  Upload,
  FileText,
  Trash2,
  Search,
  CheckCircle2,
  AlertCircle,
  RefreshCw,
  X
} from "lucide-react";

function KnowledgeVaultView({ token }) {
  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [message, setMessage] = useState({ text: "", type: "" });

  // Query Modal State
  const [activeDoc, setActiveDoc] = useState(null);
  const [docQuery, setDocQuery] = useState("");
  const [docQueryLoading, setDocQueryLoading] = useState(false);
  const [docQueryResponse, setDocQueryResponse] = useState(null);

  const fetchDocuments = async () => {
    try {
      const res = await fetch("https://brainforgeai-backend.onrender.com/documents", {
        headers: { Authorization: `Bearer ${token}` },
      });
      const data = await res.json();
      if (res.ok) {
        setDocuments(data);
      }
    } catch (err) {
      console.error("Failed to fetch documents", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDocuments();
  }, []);

  const handleFileUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append("file", file);

    setUploading(true);
    setMessage({ text: "", type: "" });

    try {
      const res = await fetch("https://brainforgeai-backend.onrender.com/documents/upload", {
        method: "POST",
        headers: { Authorization: `Bearer ${token}` },
        body: formData,
      });

      const data = await res.json();

      if (!res.ok) {
        setMessage({ text: data.message || "Failed to upload document", type: "error" });
        return;
      }

      setDocuments([data, ...documents]);
      setMessage({
        text: `"${data.fileName}" uploaded successfully.`,
        type: "success",
      });
    } catch (err) {
      setMessage({ text: "Error uploading document", type: "error" });
    } finally {
      setUploading(false);
      e.target.value = "";
    }
  };

  const handleDelete = async (id, name) => {
    if (!window.confirm(`Are you sure you want to delete "${name}"?`)) return;

    try {
      const res = await fetch(`https://brainforgeai-backend.onrender.com/documents/${id}`, {
        method: "DELETE",
        headers: { Authorization: `Bearer ${token}` },
      });

      if (res.ok) {
        setDocuments(documents.filter((d) => d.id !== id));
        setMessage({ text: `Document "${name}" deleted.`, type: "success" });
      }
    } catch (err) {
      setMessage({ text: "Failed to delete document", type: "error" });
    }
  };

  const handleQueryDoc = async (e) => {
    e.preventDefault();
    if (!docQuery.trim() || !activeDoc || docQueryLoading) return;

    setDocQueryLoading(true);
    setDocQueryResponse(null);

    try {
      const res = await fetch(`https://brainforgeai-backend.onrender.com/documents/${activeDoc.id}/query`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ query: docQuery.trim() }),
      });

      const data = await res.json();
      if (res.ok) {
        setDocQueryResponse(data);
      } else {
        setDocQueryResponse({ answer: data.message || "Failed to search document" });
      }
    } catch (err) {
      setDocQueryResponse({ answer: "Unable to connect to the server." });
    } finally {
      setDocQueryLoading(false);
    }
  };

  const formatFileSize = (bytes) => {
    if (!bytes) return "0 B";
    if (bytes < 1024) return bytes + " B";
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + " KB";
    return (bytes / (1024 * 1024)).toFixed(1) + " MB";
  };

  return (
    <div className="knowledge-vault-container">
      {/* Header */}
      <div className="vault-header">
        <div>
          <h2>Documents</h2>
          <p>
            Upload reference documents (PDF, TXT, MD) to enable grounded search and question answering.
          </p>
        </div>

        <label className={`upload-btn ${uploading ? "disabled" : ""}`}>
          {uploading ? (
            <>
              <RefreshCw size={15} className="animate-spin" />
              <span>Uploading...</span>
            </>
          ) : (
            <>
              <Upload size={15} />
              <span>Upload Document</span>
            </>
          )}
          <input
            type="file"
            accept=".pdf,.txt,.md,.json,.csv"
            onChange={handleFileUpload}
            disabled={uploading}
            className="sr-only"
          />
        </label>
      </div>

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

      {/* Document Grid */}
      <div className="vault-content">
        {loading ? (
          <div style={{ display: "flex", alignItems: "center", gap: "8px", padding: "30px 0" }}>
            <RefreshCw size={16} className="animate-spin" style={{ color: "var(--accent-primary)" }} />
            <span style={{ fontSize: "13.5px", color: "var(--text-secondary)" }}>
              Loading documents...
            </span>
          </div>
        ) : documents.length === 0 ? (
          <div className="chat-welcome-box" style={{ maxWidth: "480px" }}>
            <div className="welcome-icon-glow">
              <FileText size={24} />
            </div>
            <h3>No Documents Yet</h3>
            <p>
              Upload PDF or text files using the button above to begin searching across your content.
            </p>
          </div>
        ) : (
          <div className="documents-grid">
            {documents.map((doc) => (
              <div key={doc.id} className="document-card">
                <div className="doc-card-header">
                  <div className="doc-type-icon">
                    <FileText size={18} />
                  </div>
                  <div className="doc-meta-info">
                    <h4 title={doc.fileName}>{doc.fileName}</h4>
                    <span className="doc-stats">
                      {formatFileSize(doc.fileSize)} • {doc.chunkCount} {doc.chunkCount === 1 ? "section" : "sections"}
                    </span>
                  </div>
                </div>

                <p className="doc-preview">
                  {doc.previewText || doc.extractedText?.substring(0, 120) || "Document content indexed."}
                </p>

                <div className="doc-card-actions">
                  <button
                    className="query-doc-btn"
                    onClick={() => {
                      setActiveDoc(doc);
                      setDocQuery("");
                      setDocQueryResponse(null);
                    }}
                  >
                    <Search size={13} />
                    <span>Ask Document</span>
                  </button>

                  <button
                    className="delete-doc-btn"
                    onClick={() => handleDelete(doc.id, doc.fileName)}
                    title="Delete document"
                  >
                    <Trash2 size={13} />
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Grounded Query Modal */}
      {activeDoc && (
        <div className="modal-overlay" onClick={() => setActiveDoc(null)}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <div className="modal-title-box">
                <h3>Ask about: {activeDoc.fileName}</h3>
              </div>
              <button className="modal-close-btn" onClick={() => setActiveDoc(null)}>
                <X size={16} />
              </button>
            </div>

            <form onSubmit={handleQueryDoc} className="modal-form">
              <input
                type="text"
                placeholder="Ask a question about this document..."
                value={docQuery}
                onChange={(e) => setDocQuery(e.target.value)}
                required
              />
              <button type="submit" disabled={docQueryLoading || !docQuery.trim()}>
                {docQueryLoading ? <RefreshCw size={14} className="animate-spin" /> : "Search"}
              </button>
            </form>

            {docQueryLoading && (
              <div
                style={{
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  gap: "8px",
                  padding: "16px 0",
                  color: "var(--text-secondary)",
                  fontSize: "13px",
                }}
              >
                <RefreshCw size={15} className="animate-spin" style={{ color: "var(--accent-primary)" }} />
                <span>Searching document and generating answer...</span>
              </div>
            )}

            {docQueryResponse && (
              <div className="modal-answer-box">
                <h4>Answer:</h4>
                <p className="modal-answer-text">{docQueryResponse.answer}</p>

                {docQueryResponse.relevantSnippets && docQueryResponse.relevantSnippets.length > 0 && (
                  <div className="modal-sources-box">
                    <h5>Relevant Passages:</h5>
                    {docQueryResponse.relevantSnippets.map((snip, i) => (
                      <div key={i} className="source-excerpt">
                        "{snip}"
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

export default KnowledgeVaultView;
