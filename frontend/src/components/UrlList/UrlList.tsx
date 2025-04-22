import { useState, useEffect, useMemo } from "react";
import "./UrlList.css";

interface UrlData {
  shortenedUrl: string;
  url: string;
  clickCount: string;
  expirationTime: string;
}

interface UrlListProps {
  allUrls: UrlData[];
  handleGetAll: () => Promise<void>;
  handleDelete: (shortenedUrl: string) => Promise<void>;
  currentPage: number;
  setCurrentPage: React.Dispatch<React.SetStateAction<number>>;
  estimatedTotalPages: number;
}

enum SortKey {
  OriginalUrl = "url",
  ShortUrl = "shortenedUrl",
  ClickCount = "clickCount",
  ExpirationTime = "expirationTime",
}

const CopyButton: React.FC<{ text: string }> = ({ text }) => {
  const [copied, setCopied] = useState(false);

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(
        `${window.location.origin}/redirect/${text}`
      );
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      console.error("Failed to copy text:", err);
    }
  };

  return (
    <div className="copy-button-container">
      <button
        onClick={handleCopy}
        className="copy-button"
        aria-label={copied ? "Copied to clipboard" : "Copy shortened URL"}
      >
        {copied ? "Copied!" : "Copy"}
      </button>
      {copied && <span className="copy-tooltip">Copied to clipboard!</span>}
    </div>
  );
};

const UrlList: React.FC<UrlListProps> = ({
  allUrls,
  handleGetAll,
  handleDelete,
  currentPage,
  setCurrentPage,
  estimatedTotalPages,
}) => {
  const [searchTerm, setSearchTerm] = useState("");
  const [showUrls, setShowUrls] = useState(false);
  const [loading, setLoading] = useState(false);
  const [sortKey, setSortKey] = useState<SortKey>(SortKey.OriginalUrl);
  const [sortAsc, setSortAsc] = useState(true);

  // Fetch URLs on mount if not already fetched
  useEffect(() => {
    if (allUrls.length === 0 && showUrls) {
      setLoading(true);
      handleGetAll().finally(() => setLoading(false));
    }
  }, [allUrls, showUrls, handleGetAll]);

  const toggleVisibility = () => {
    if (!showUrls && allUrls.length === 0) {
      setLoading(true);
      handleGetAll().finally(() => setLoading(false));
    }
    setShowUrls((prev) => !prev);
    if (!showUrls) {
      setTimeout(() => window.scrollTo({ top: 0, behavior: "smooth" }), 50);
    }
  };

  const handleSort = (key: SortKey) => {
    if (key === sortKey) {
      setSortAsc(!sortAsc);
    } else {
      setSortKey(key);
      setSortAsc(true);
    }
  };

  const filteredAndSortedUrls = useMemo(() => {
    const filtered = allUrls.filter(
      (urlData) =>
        urlData.url.toLowerCase().includes(searchTerm.toLowerCase()) ||
        urlData.shortenedUrl.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return filtered.slice().sort((a, b) => {
      const aValue = a[sortKey];
      const bValue = b[sortKey];
      if (sortKey === SortKey.ClickCount) {
        const aNum = parseInt(aValue) || 0;
        const bNum = parseInt(bValue) || 0;
        return sortAsc ? aNum - bNum : bNum - aNum;
      }
      if (sortKey === SortKey.ExpirationTime) {
        const aDate = aValue ? new Date(aValue).getTime() : Infinity;
        const bDate = bValue ? new Date(bValue).getTime() : Infinity;
        return sortAsc ? aDate - bDate : bDate - aDate;
      }
      return sortAsc
        ? aValue.localeCompare(bValue)
        : bValue.localeCompare(aValue);
    });
  }, [allUrls, searchTerm, sortKey, sortAsc]);

  return (
    <div className="url-list-wrapper">
      <button
        onClick={toggleVisibility}
        className="show-all-button"
        aria-expanded={showUrls}
        aria-controls="url-list"
      >
        {showUrls ? "Hide URLs" : "Show URLs"}
      </button>

      {showUrls && (
        <div className="url-list-container">
          <div className="search-bar-container">
            <input
              type="text"
              placeholder="Search URLs..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="search-bar"
              aria-label="Search URLs"
            />
          </div>

          <div className="sort-buttons">
            <button
              onClick={() => handleSort(SortKey.OriginalUrl)}
              className={`sort-button ${
                sortKey === SortKey.OriginalUrl ? "active" : ""
              }`}
            >
              Original URL{" "}
              {sortKey === SortKey.OriginalUrl && (sortAsc ? "↑" : "↓")}
            </button>
            <button
              onClick={() => handleSort(SortKey.ShortUrl)}
              className={`sort-button ${
                sortKey === SortKey.ShortUrl ? "active" : ""
              }`}
            >
              Short URL {sortKey === SortKey.ShortUrl && (sortAsc ? "↑" : "↓")}
            </button>
            <button
              onClick={() => handleSort(SortKey.ClickCount)}
              className={`sort-button ${
                sortKey === SortKey.ClickCount ? "active" : ""
              }`}
            >
              Click Count{" "}
              {sortKey === SortKey.ClickCount && (sortAsc ? "↑" : "↓")}
            </button>
            <button
              onClick={() => handleSort(SortKey.ExpirationTime)}
              className={`sort-button ${
                sortKey === SortKey.ExpirationTime ? "active" : ""
              }`}
            >
              Expiration{" "}
              {sortKey === SortKey.ExpirationTime && (sortAsc ? "↑" : "↓")}
            </button>
          </div>

          {loading ? (
            <div className="loading-spinner">
              <div className="spinner"></div>
              <p>Loading URLs...</p>
            </div>
          ) : filteredAndSortedUrls.length > 0 ? (
            <ul className="url-list" id="url-list">
              {filteredAndSortedUrls.map((urlData) => (
                <li key={urlData.shortenedUrl} className="url-item">
                  <span className="original-url">{urlData.url}</span>
                  <span className="arrow">→</span>
                  <span className="short-url">
                    <a
                      href={`/redirect/${urlData.shortenedUrl}`}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="short-url-link"
                    >
                      {urlData.shortenedUrl}
                    </a>
                  </span>
                  <CopyButton text={urlData.shortenedUrl} />
                  <span className="click-count">
                    Clicks: {urlData.clickCount}
                    <div className="note">Updates every 1 min</div>
                  </span>
                  <span className="expiration">
                    {urlData.expirationTime
                      ? `Expires: ${urlData.expirationTime}`
                      : "No expiration"}
                  </span>
                  <button
                    onClick={() => handleDelete(urlData.shortenedUrl)}
                    className="delete-button"
                    aria-label={`Delete ${urlData.shortenedUrl}`}
                  >
                    Delete
                  </button>
                </li>
              ))}
            </ul>
          ) : (
            <div className="empty-state">
              <p>No matching URLs found.</p>
              <p>Try adjusting your search or shortening a new URL!</p>
            </div>
          )}
          <div className="pagination-controls">
            <button
              onClick={() => setCurrentPage((prev) => prev - 1)}
              disabled={currentPage === 0}
              className="pagination-button"
              aria-label="Previous page"
            >
              Previous
            </button>
            <span>Page {currentPage + 1}</span>
            <button
              onClick={() => setCurrentPage((prev) => prev + 1)}
              disabled={currentPage >= estimatedTotalPages - 1}
              className="pagination-button"
              aria-label="Next page"
            >
              {currentPage >= estimatedTotalPages - 1
                ? "No more pages 😅"
                : "Next"}
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default UrlList;
