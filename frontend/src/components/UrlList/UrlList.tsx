import React, { useState } from "react";
import "./UrlList.css";

interface UrlData {
  shortenedUrl: string;
  url: string;
  clickCount: string;
  expirationTime: string;
}

interface UrlListProps {
  allUrls: UrlData[];
  handleGetAll: () => void;
  handleDelete: (shortenedUrl: string) => void;
}

const CopyButton: React.FC<{ text: string }> = ({ text }) => {
  const [copied, setCopied] = useState(false);

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(`${window.location.origin}/redirect/${text}`);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      console.error("Failed to copy text:", err);
    }
  };

  return (
    <button onClick={handleCopy} className="copy-button">
      {copied ? "Copied" : "Copy"}
    </button>
  );
};

const UrlList: React.FC<UrlListProps> = ({ allUrls, handleGetAll, handleDelete }) => {
  const [searchTerm, setSearchTerm] = useState("");
  const [showUrls, setShowUrls] = useState(false); // toggle visibility

  const handleToggle = () => {
    if (!showUrls) handleGetAll(); // only fetch when opening
    setShowUrls((prev) => !prev);
  };

  const filteredUrls = allUrls.filter(
    (urlData) =>
      urlData.url.toLowerCase().includes(searchTerm.toLowerCase()) ||
      urlData.shortenedUrl.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="url-list-container">
      <button onClick={handleToggle} className="show-all-button">
        {showUrls ? "Hide URLs" : "Show URLs"}
      </button>

      {showUrls && (
        <>
          <input
            type="text"
            placeholder="Search URLs..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="search-bar"
          />
          {filteredUrls.length > 0 ? (
            <ul className="url-list">
              {filteredUrls
                .slice()
                .reverse()
                .map((urlData) => (
                  <li key={urlData.shortenedUrl} className="url-item">
                    <span className="original-url">{urlData.url}</span>
                    <span> → </span>
                    <span className="short-url">
                      <a
                        href={`/redirect/${urlData.shortenedUrl}`}
                        target="_blank"
                        rel="noopener noreferrer"
                      >
                        {urlData.shortenedUrl}
                      </a>
                      <CopyButton text={urlData.shortenedUrl} />
                    </span>
                    <span className="click-count">
                      Click count: {urlData.clickCount}
                      <div className="note">update every 1 minute</div>
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
          ) : (
            <p>No matching URLs found.</p>
          )}
        </>
      )}
    </div>
  );
};

export default UrlList;
