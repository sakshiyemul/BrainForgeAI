import React from "react";
import { Sun, Moon } from "lucide-react";
import { useTheme } from "../context/ThemeContext";

function ThemeToggle({ className = "", showLabel = false }) {
  const { theme, toggleTheme } = useTheme();

  return (
    <button
      type="button"
      onClick={toggleTheme}
      className={`theme-toggle-btn ${className}`}
      title={`Switch to ${theme === "dark" ? "Light" : "Dark"} Mode`}
      aria-label="Toggle Theme"
    >
      <div className="theme-toggle-inner">
        {theme === "dark" ? (
          <Sun size={17} className="theme-icon-sun" />
        ) : (
          <Moon size={17} className="theme-icon-moon" />
        )}
        {showLabel && (
          <span className="theme-toggle-label">
            {theme === "dark" ? "Light Mode" : "Dark Mode"}
          </span>
        )}
      </div>
    </button>
  );
}

export default ThemeToggle;
