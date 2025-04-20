import { useState, useCallback } from "react";
import { QRCodeSVG } from "qrcode.react"; // Fixed: Use QRCodeSVG for SVG rendering
import "./ResultBox.css";

interface ResultBoxProps {
  shortUrl: string;
  logAction?: (message: string) => void; // Optional prop to integrate with MainApp's action log
}

const ResultBox: React.FC<ResultBoxProps> = ({ shortUrl, logAction }) => {
  const [copied, setCopied] = useState(false);
  const [downloadError, setDownloadError] = useState("");

  // Handle copying the URL to clipboard
  const handleCopy = useCallback(async () => {
    try {
      await navigator.clipboard.writeText(shortUrl);
      setCopied(true);
      logAction?.(`Copied URL: ${shortUrl}`);
      setTimeout(() => setCopied(false), 2000);
    } catch (error) {
      console.error("Failed to copy URL:", error);
      logAction?.("Failed to copy URL");
    }
  }, [shortUrl, logAction]);

  // Handle downloading the QR code as PNG
  const handleDownloadQR = useCallback(() => {
    const svg = document.querySelector(".qr-code svg");
    if (svg) {
      const svgData = new XMLSerializer().serializeToString(svg);
      const canvas = document.createElement("canvas");
      const ctx = canvas.getContext("2d");
      const img = new Image();
      img.onload = () => {
        canvas.width = img.width * 2; // Improve resolution
        canvas.height = img.height * 2;
        ctx?.drawImage(img, 0, 0, img.width * 2, img.height * 2);
        const link = document.createElement("a");
        link.href = canvas.toDataURL("image/png");
        link.download = "short-url-qr.png";
        link.click();
        logAction?.("Downloaded QR code");
      };
      img.onerror = () => {
        setDownloadError("Failed to generate QR code image");
        logAction?.("Failed to download QR code: Image load error");
        setTimeout(() => setDownloadError(""), 3000);
      };
      img.src = `data:image/svg+xml;base64,${btoa(svgData)}`;
    } else {
      setDownloadError("QR code not found");
      logAction?.("Failed to download QR code: SVG not found");
      setTimeout(() => setDownloadError(""), 3000);
    }
  }, [logAction]);

  return (
    <div className="result-box" role="region" aria-label="Shortened URL Result">
      <div className="result-content">
        <p className="result-text">
          Shortened URL:{" "}
          <a
            href={shortUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="result-link"
          >
            {shortUrl}
          </a>
        </p>
        <div className="result-actions">
          <button
            onClick={handleCopy}
            className="copy-button"
            aria-label={
              copied ? "URL copied to clipboard" : "Copy shortened URL"
            }
          >
            {copied ? (
              <span>
                Copied!{" "}
                <span className="copy-tooltip">Copied to clipboard!</span>
              </span>
            ) : (
              "Copy"
            )}
          </button>
          <button
            onClick={handleDownloadQR}
            className="download-qr-button"
            aria-label="Download QR code"
          >
            Download QR
            <span className="download-tooltip">Download as PNG</span>
          </button>
        </div>
        {downloadError && (
          <span className="error-message">{downloadError}</span>
        )}
      </div>
      <div className="qr-code">
        <QRCodeSVG
          value={shortUrl}
          size={120}
          bgColor="#ffffff"
          fgColor="#1f2937"
          level="M"
          includeMargin
          aria-label="QR code for shortened URL"
        />
      </div>
    </div>
  );
};

export default ResultBox;
