import React from "react";
import { BrowserRouter, Routes, Route, useNavigate } from "react-router-dom";

import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import AdminDashboard from "./pages/AdminDashboard";
import ProtectedRoute from "./pages/ProtectedRoute";
import { ThemeProvider } from "./context/ThemeContext";

import "./App.css";

function NotFound() {
  const navigate = useNavigate();

  return (
    <div className="error-page-container">
      <div className="error-card-glass">
        <div className="error-code-glow">404</div>
        <h2>Page Not Found</h2>
        <p>
          The page you are looking for does not exist or has been moved.
        </p>
        <button
          type="button"
          className="btn-auth-primary"
          onClick={() => navigate("/")}
          style={{ maxWidth: "180px" }}
        >
          Return to Home
        </button>
      </div>
    </div>
  );
}

function App() {
  return (
    <ThemeProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute role="USER">
                <Dashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin"
            element={
              <ProtectedRoute role="ADMIN">
                <AdminDashboard />
              </ProtectedRoute>
            }
          />
          <Route path="*" element={<NotFound />} />
        </Routes>
      </BrowserRouter>
    </ThemeProvider>
  );
}

export default App;