import { useState } from "react";
import "./Form.css";

interface UrlFormProps {
  url: string;
  ttl: string;
  customUrl: string;
  setOriginalUrl: (value: string) => void;
  setTtl: (value: string) => void;
  setCustomUrl: (value: string) => void;
  handleSubmit: (e: React.FormEvent) => Promise<void>;
  isSubmitting?: boolean; // Optional prop to indicate submission state
}

const UrlForm: React.FC<UrlFormProps> = ({
  url,
  ttl,
  customUrl,
  setOriginalUrl,
  setTtl,
  setCustomUrl,
  handleSubmit,
  isSubmitting = false,
}) => {
  const [errors, setErrors] = useState<{
    url?: string;
    ttl?: string;
    customUrl?: string;
  }>({});

  // Validate inputs
  const validateForm = (): boolean => {
    const newErrors: { url?: string; ttl?: string; customUrl?: string } = {};

    // URL validation
    if (!url) {
      newErrors.url = "URL is required";
    } else if (!/^https?:\/\/[^\s/$.?#].[^\s]*$/.test(url)) {
      newErrors.url = "Please enter a valid URL (e.g., https://example.com)";
    }

    // TTL validation
    if (ttl && (isNaN(parseInt(ttl)) || parseInt(ttl) <= 0)) {
      newErrors.ttl = "TTL must be a positive number";
    }

    // Custom URL validation
    if (customUrl && !/^[a-zA-Z0-9_-]{1,20}$/.test(customUrl)) {
      newErrors.customUrl =
        "Custom URL must be 1-20 alphanumeric characters, underscores, or hyphens";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // Handle form submission
  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (validateForm()) {
      await handleSubmit(e);
    }
  };

  // Clear form fields
  const clearForm = () => {
    setOriginalUrl("");
    setTtl("");
    setCustomUrl("");
    setErrors({});
  };

  return (
    <form onSubmit={onSubmit} className="shorten-form" noValidate>
      <div className="form-group">
        <label htmlFor="url-input" className="form-label">
          URL to Shorten
        </label>
        <input
          id="url-input"
          type="url"
          value={url}
          onChange={(e) => setOriginalUrl(e.target.value)}
          placeholder="Enter URL (e.g., https://example.com)"
          className={`url-input ${errors.url ? "input-error" : ""}`}
          aria-invalid={!!errors.url}
          aria-describedby={errors.url ? "url-error" : undefined}
          required
        />
        {errors.url && (
          <span id="url-error" className="error-message">
            {errors.url}
          </span>
        )}
      </div>

      <div className="form-group">
        <label htmlFor="ttl-input" className="form-label">
          TTL (minutes, optional)
        </label>
        <input
          id="ttl-input"
          type="number"
          value={ttl}
          onChange={(e) => setTtl(e.target.value)}
          placeholder="TTL in minutes (e.g., 60)"
          className={`ttl-input ${errors.ttl ? "input-error" : ""}`}
          aria-invalid={!!errors.ttl}
          aria-describedby={errors.ttl ? "ttl-error" : undefined}
          min="1"
        />
        {errors.ttl && (
          <span id="ttl-error" className="error-message">
            {errors.ttl}
          </span>
        )}
      </div>

      <div className="form-group">
        <label htmlFor="custom-input" className="form-label">
          Custom Short URL (optional)
        </label>
        <input
          id="custom-input"
          type="text"
          value={customUrl}
          onChange={(e) => setCustomUrl(e.target.value)}
          placeholder="Custom short URL (e.g., mylink)"
          className={`custom-input ${errors.customUrl ? "input-error" : ""}`}
          aria-invalid={!!errors.customUrl}
          aria-describedby={errors.customUrl ? "custom-url-error" : undefined}
        />
        {errors.customUrl && (
          <span id="custom-url-error" className="error-message">
            {errors.customUrl}
          </span>
        )}
      </div>

      <div className="form-actions">
        <button
          type="submit"
          className="shorten-button"
          disabled={isSubmitting}
          aria-busy={isSubmitting}
        >
          {isSubmitting ? (
            <span className="loading-spinner">Shortening...</span>
          ) : (
            "Shorten"
          )}
        </button>
        <button
          type="button"
          onClick={clearForm}
          className="clear-button"
          disabled={isSubmitting}
        >
          Clear
        </button>
      </div>
    </form>
  );
};

export default UrlForm;
