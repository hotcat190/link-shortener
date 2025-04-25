import React, { useEffect, useState, useCallback } from "react";
import { useParams } from "react-router-dom";
import { BASE_BACKEND_URL } from "./constants";
import "./RedirectPage.css";

const RedirectPage: React.FC = () => {
  const { shortId } = useParams<{ shortId: string }>();
  const [originalUrl, setOriginalUrl] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [copied, setCopied] = useState(false);
  const [countdown, setCountdown] = useState(3);

  const isDarkMode = localStorage.getItem("theme") === "dark"; // or however you store the theme

  const fetchRedirect = useCallback(async () => {
    if (!shortId) {
      setError("Invalid URL: No short ID provided");
      return;
    }

    console.log("Fetching redirect for shortId:", shortId);
    try {
      const res = await fetch(`${BASE_BACKEND_URL}/${shortId}`, {
        headers: { Accept: "text/plain" },
      });

      if (res.status === 429) {
        setError("Rate limit exceeded. Please try again later.");
        return;
      }

      if (!res.ok) {
        const errorText = await res.text();
        throw new Error(`URL not found: ${res.status} - ${errorText}`);
      }

      const url = await res.text();
      setOriginalUrl(url);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : String(err);
      console.error("Redirect failed:", errorMessage);
      setError(`Failed to redirect: ${errorMessage}`);
    }
  }, [shortId]);

  useEffect(() => {
    fetchRedirect();
  }, [fetchRedirect]);

  useEffect(() => {
    if (originalUrl && !error) {
      const timer = setInterval(() => {
        setCountdown((prev) => {
          if (prev <= 1) {
            clearInterval(timer);
            window.location.href = originalUrl;
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
      return () => clearInterval(timer);
    }
  }, [originalUrl, error]);

  const handleCopy = useCallback(async () => {
    if (!originalUrl) return;
    try {
      await navigator.clipboard.writeText(originalUrl);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      console.error("Failed to copy URL:", err);
    }
  }, [originalUrl]);

  const handleImmediateRedirect = useCallback(() => {
    if (originalUrl) {
      window.location.href = originalUrl;
    }
  }, [originalUrl]);

  return (
    <div className={`redirect-wrapper ${isDarkMode ? "dark-mode" : ""}`}>
      <div className="redirect-page">
        <div className="redirect-card">
          <h2 className="redirect-heading">
            {error
              ? "Redirect Failed"
              : countdown > 0
              ? `Redirecting in ${countdown}s...`
              : "Redirecting..."}
          </h2>
          {error ? (
            <div>
              <p className="error-message">{error}</p>
              <button
                onClick={() => {
                  setError(null);
                  setCountdown(3);
                  fetchRedirect();
                }}
                className="retry-button"
              >
                Retry
              </button>
            </div>
          ) : originalUrl ? (
            <div>
              <p>
                <a
                  href={originalUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="url-link"
                >
                  {originalUrl}
                </a>
              </p>
              <div className="button-group">
                <button
                  onClick={handleCopy}
                  className="copy-button"
                  aria-label={copied ? "URL copied" : "Copy URL"}
                >
                  {copied ? (
                    <>
                      Copied!
                      <span className="copy-tooltip">Copied to clipboard!</span>
                    </>
                  ) : (
                    "Copy URL"
                  )}
                </button>
                <button
                  onClick={handleImmediateRedirect}
                  className="redirect-button"
                >
                  Redirect Now
                </button>
              </div>
            </div>
          ) : (
            <div>
              <div className="spinner"></div>
              <p className="loading-text">Fetching URL...</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default RedirectPage;
