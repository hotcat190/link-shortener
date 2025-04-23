import { useState, useEffect, useCallback } from "react";
import { Routes, Route } from "react-router-dom";
import RedirectPage from "./RedirectPage.tsx";
import UrlForm from "./components/Form/Form.tsx";
import UrlList from "./components/UrlList/UrlList.tsx";
import MessageBox from "./components/MessageBox/MessageBox.tsx";
import ResultBox from "./components/ResultBox/ResultBox.tsx";
import "./App.css";
import CreationRequest from "./types/creationRequest.ts";
import { BASE_URL } from "./constants.ts";

interface UrlData {
  shortenedUrl: string;
  url: string;
  clickCount: string;
  expirationTime: string;
}

interface ActionLog {
  id: string;
  message: string;
  timestamp: string;
}

const MainApp: React.FC = () => {
  const [url, setOriginalUrl] = useState("");
  const [shortUrl, setShortUrl] = useState("");
  const [ttl, setTtl] = useState("");
  const [customUrl, setCustomUrl] = useState("");
  const [allUrls, setAllUrls] = useState<UrlData[]>([]);
  const [message, setMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [actionLog, setActionLog] = useState<ActionLog[]>([]);
  const [isDarkMode, setIsDarkMode] = useState(
    localStorage.getItem("theme") === "dark"
  );
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize] = useState(10);
  const [estimatedTotalPages, setEstimatedTotalPages] = useState(1);

  // Toggle dark mode
  useEffect(() => {
    document.body.classList.toggle("dark-mode", isDarkMode);
    localStorage.setItem("theme", isDarkMode ? "dark" : "light");
  }, [isDarkMode]);

  // Auto-dismiss message after 3 seconds
  useEffect(() => {
    if (message) {
      const timer = setTimeout(() => setMessage(""), 3000);
      return () => clearTimeout(timer);
    }
  }, [message]);

  // Log actions
  const logAction = useCallback((actionMessage: string) => {
    setActionLog((prev) => [
      {
        id: crypto.randomUUID(),
        message: actionMessage,
        timestamp: new Date().toLocaleTimeString(),
      },
      ...prev.slice(0, 4), // Keep last 5 actions
    ]);
  }, []);

  const handleShorten = useCallback(async () => {
    setIsSubmitting(true);
    setIsLoading(true);
    try {
      const requestBody: CreationRequest = {
        url,
        ttlMinute: ttl ? parseInt(ttl) : null,
        customShortenedUrl: customUrl,
      };

      const response = await fetch(BASE_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(requestBody),
      });

      const responseText = await response.text();

      if (response.status === 429) {
        setMessage("Rate limit exceeded. Please try again later.");
        //logAction("Failed to shorten URL: Rate limit exceeded");
        return;
      }

      if (response.ok) {
        setShortUrl(`${responseText}`);
        setMessage("URL shortened successfully!");
        logAction(`Shortened URL: ${url}`);
        setOriginalUrl("");
        setTtl("");
        setCustomUrl("");
        setCurrentPage(0); // Reset to first page
      } else {
        try {
          const errorData = JSON.parse(responseText);
          setMessage(`Error: ${errorData.error || responseText}`);
        } catch {
          setMessage(
            `Error shortening URL: ${response.status} - ${responseText}`
          );
        }
        //logAction(`Failed to shorten URL: ${response.status}`);
      }
    } catch (error) {
      const errorMessage =
        error instanceof Error ? error.message : String(error);
      setMessage(`Error connecting to server: ${errorMessage}`);
      //logAction(`Failed to shorten URL: ${errorMessage}`);
    } finally {
      setIsSubmitting(false);
      setIsLoading(false);
    }
  }, [url, ttl, customUrl, logAction]);

  const handleGetAll = useCallback(async () => {
    setIsLoading(true);
    try {
      const queryParams = new URLSearchParams({
        page: currentPage.toString(),
        size: pageSize.toString(),
      });
      const response = await fetch(`${BASE_URL}/all?${queryParams.toString()}`);
      const responseText = await response.text();

      if (response.status === 429) {
        setMessage("Rate limit exceeded. Please try again later.");
        //logAction("Failed to fetch URLs: Rate limit exceeded");
        return;
      }

      if (response.ok) {
        const urls: UrlData[] = JSON.parse(responseText);
        setAllUrls(urls);
        // Estimate totalPages: if fewer than pageSize, assume last page
        const isLastPage = urls.length < pageSize;
        setEstimatedTotalPages((prev) =>
          isLastPage ? currentPage + 1 : Math.max(prev, currentPage + 2)
        );
        //logAction(`Fetched URLs (page ${currentPage + 1})`);
      } else {
        setMessage(`Error fetching URLs: ${response.status} - ${responseText}`);
        logAction(`Failed to fetch URLs: ${response.status}`);
      }
    } catch (error) {
      const errorMessage =
        error instanceof Error ? error.message : String(error);
      setMessage(`Error fetching URLs: ${errorMessage}`);
      //logAction(`Failed to fetch URLs: ${errorMessage}`);
    } finally {
      setIsLoading(false);
    }
  }, [currentPage, pageSize, logAction]);

  const handleDelete = useCallback(
    async (shortenedUrl: string) => {
      setIsLoading(true);
      try {
        const response = await fetch(`${BASE_URL}/${shortenedUrl}`, {
          method: "DELETE",
        });

        if (response.status === 429) {
          setMessage("Rate limit exceeded. Please try again later.");
          logAction("Failed to delete URL: Rate limit exceeded");
          return;
        }

        if (response.ok) {
          setMessage("URL deleted successfully");
          logAction(`Deleted URL: ${shortenedUrl}`);
          setCurrentPage(0); // Reset to first page
        } else {
          const responseText = await response.text();
          setMessage(
            `Error deleting URL: ${response.status} - ${responseText}`
          );
          logAction(`Failed to delete URL: ${response.status}`);
        }
      } catch (error) {
        const errorMessage =
          error instanceof Error ? error.message : String(error);
        setMessage(`Error deleting URL: ${errorMessage}`);
        logAction(`Failed to delete URL: ${errorMessage}`);
      } finally {
        setIsLoading(false);
      }
    },
    [logAction]
  );

  const handleSubmit = useCallback(
    async (e: React.FormEvent) => {
      e.preventDefault();
      await handleShorten();
    },
    [handleShorten]
  );

  const handleShare = useCallback(async () => {
    if (shortUrl && navigator.share) {
      try {
        await navigator.share({
          title: "Shortened URL",
          url: shortUrl,
        });
        logAction("Shared shortened URL");
      } catch (error) {
        console.error("Share error:", error);
        logAction("Failed to share URL");
      }
    }
  }, [shortUrl, logAction]);

  // Fetch URLs when page changes
  useEffect(() => {
    handleGetAll();
  }, [currentPage, handleGetAll]);

  return (
    <div className={`app ${isDarkMode ? "dark" : ""}`}>
      <header className="app-header">
        <div className="container">
          <h1>🔗 URL Shortener</h1>
          <button
            onClick={() => setIsDarkMode(!isDarkMode)}
            className="theme-toggle"
            aria-label={
              isDarkMode ? "Switch to light mode" : "Switch to dark mode"
            }
          >
            {isDarkMode ? "☀️" : "🌙"}
          </button>
        </div>
      </header>

      <main className="main-content">
        {isLoading && (
          <div className="loading-overlay">
            <div className="loading-spinner"></div>
          </div>
        )}
        <div className="app-container">
          <h2>Shorten Your URLs</h2>
          <UrlForm
            url={url}
            ttl={ttl}
            customUrl={customUrl}
            setOriginalUrl={setOriginalUrl}
            setTtl={setTtl}
            setCustomUrl={setCustomUrl}
            handleSubmit={handleSubmit}
            isSubmitting={isSubmitting}
          />
          {shortUrl && (
            <div className="result-container">
              <ResultBox shortUrl={shortUrl} logAction={logAction} />
              {
                <button
                  onClick={handleShare}
                  className="share-button"
                  aria-label="Share shortened URL"
                >
                  Share
                </button>
              }
            </div>
          )}
          {message && <MessageBox message={message} />}
          <UrlList
            allUrls={allUrls}
            handleGetAll={handleGetAll}
            handleDelete={handleDelete}
            currentPage={currentPage}
            setCurrentPage={setCurrentPage}
            estimatedTotalPages={estimatedTotalPages}
          />

          {actionLog.length > 0 && (
            <section className="action-log">
              <h3>Recent Actions</h3>
              <ul>
                {actionLog.map((log) => (
                  <li key={log.id}>
                    {log.message} <span>({log.timestamp})</span>
                  </li>
                ))}
              </ul>
            </section>
          )}
        </div>
      </main>

      <footer className="app-footer">
        <div className="container">
          <p>© 2025 URL Shortener - Tool to shorten a long link</p>
        </div>
      </footer>
    </div>
  );
};

const App: React.FC = () => {
  return (
    <Routes>
      <Route path="/" element={<MainApp />} />
      <Route path="/redirect/:shortId" element={<RedirectPage />} />
    </Routes>
  );
};

export default App;
