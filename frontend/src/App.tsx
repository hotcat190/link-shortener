import { useState } from "react";
import { Routes, Route } from "react-router-dom";
import RedirectPage from "./RedirectPage.tsx";
import "./App.css";


interface UrlData {
  shortenedUrl: string;
  url: string;
  clickCount: string;
  expirationTime: string;
}

const MainApp: React.FC = () => {
  const [url, setOriginalUrl] = useState("");
  const [shortUrl, setShortUrl] = useState("");
  const [ttl, setTtl] = useState("");
  const [customUrl, setCustomUrl] = useState("");
  const [allUrls, setAllUrls] = useState<UrlData[]>([]);
  const [message, setMessage] = useState("");

  const baseApiUrl = "http://localhost:8080";

  const handleShorten = async () => {
   
    try {
      const response = await fetch(`${baseApiUrl}/create`, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({
          url: url,
          ...(ttl && { ttlMinute: ttl }),
          ...(customUrl && { customShortenedUrl: customUrl }),
        }),
      });

      const responseText = await response.text();

      if (response.status === 429) {
        setMessage("Rate limit exceeded. Please try again later.");
        return;
      }

      if (response.ok) {
        setShortUrl(`${baseApiUrl}/short/${responseText}`);
        setMessage("URL shortened successfully!");
      } else {
        setMessage(
          `Error shortening URL: ${response.status} - ${responseText}`
        );
      }
    } catch (error) {
      console.error("Fetch error:", error);
      setMessage(
        `Error connecting to server: ${
          error instanceof Error ? error.message : String(error)
        }`
      );
    }
  };

  const handleGetAll = async () => {
    try {
      const response = await fetch(`${baseApiUrl}/all`);
      const responseText = await response.text();

      if (response.status === 429) {
        setMessage("Rate limit exceeded. Please try again later.");
        return;
      }

      if (response.ok) {
        const urls: UrlData[] = JSON.parse(responseText);
        setAllUrls(urls);
      } else {
        setMessage(`Error fetching URLs: ${response.status} - ${responseText}`);
      }
    } catch (error) {
      setMessage(
        `Error fetching URLs: ${
          error instanceof Error ? error.message : String(error)
        }`
      );
    }
  };

  const handleDelete = async (shortenedUrl: string) => {
    try {
      const response = await fetch(
        `${baseApiUrl}/delete?shortenedUrl=${shortenedUrl}`,
        {
          method: "DELETE",
        }
      );

      if (response.status === 429) {
        setMessage("Rate limit exceeded. Please try again later.");
        return;
      }

      if (response.ok) {
        setMessage("URL deleted successfully");
        handleGetAll();
      } else {
        const responseText = await response.text();
        setMessage(`Error deleting URL: ${response.status} - ${responseText}`);
      }
    } catch (error) {
      setMessage(
        `Error deleting URL: ${
          error instanceof Error ? error.message : String(error)
        }`
      );
    }
  };
  const handleSubmit = async(e: React.FormEvent) => {
    e.preventDefault();
    await handleShorten();  
    await handleGetAll();   
  };
  return (
   
    <div className="app-container">
      <h1>URL Shortener</h1>
      <form onSubmit={handleSubmit} className="shorten-form">
        <input
          type="url"
          value={url}
          onChange={(e) => setOriginalUrl(e.target.value)}
          placeholder="Enter URL to shorten"
          required
          className="url-input"
        />
        <input
          type="number"
          value={ttl}
          onChange={(e) => setTtl(e.target.value)}
          placeholder="TTL (minutes)"
          className="ttl-input"
        />
        <input
          type="text"
          value={customUrl}
          onChange={(e) => setCustomUrl(e.target.value)}
          placeholder="Custom short URL"
          className="custom-input"
        />
        <button type="submit" className="shorten-button">
          Shorten
        </button>
      </form>

      {shortUrl && (
        <div className="result-box">
          <p>
            Shortened URL:{" "}
            <a href={shortUrl} target="_blank" rel="noopener noreferrer">
              {shortUrl}
            </a>
          </p>
        </div>
      )}

      {message && (
        <div
          className={`message-box ${
            message.includes("Error") ? "error" : "success"
          }`}
        >
          <p>{message}</p>
        </div>
      )}

      <div className="url-list-container">
        <button onClick={handleGetAll} className="show-all-button">
          Show All URLs
        </button>
        {allUrls.length > 0 && (
          <ul className="url-list">
            {allUrls.slice().reverse().map((urlData) => (
              <li key={urlData.shortenedUrl} className="url-item">
                <span className="original-url">{urlData.url}</span>
                <span> → </span>
                <span className="short-url"><a href={`/redirect/${urlData.shortenedUrl}`} target="_blank">
                  {urlData.shortenedUrl}
                </a></span>
                
                <span className="click-count">
                  Click count: {urlData.clickCount}
                </span>

                <span className="expiration">
                  {urlData.expirationTime
                    ? `Expires at: ${urlData.expirationTime}`
                    : "No expiration time"}
                </span>
                <button
                  onClick={() => handleDelete(urlData.shortenedUrl)}
                  className="delete-button"
                >
                  Delete
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}

const App: React.FC = () => {
  return (
    <Routes>
      <Route path="/" element={<MainApp />} />
      <Route path="/redirect/:shortId" element={<RedirectPage />} />
    </Routes>
  );
};

export default App;