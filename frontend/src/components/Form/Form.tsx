import "./Form.css";

interface UrlFormProps {
    url: string;
    ttl: string;
    customUrl: string;
    setOriginalUrl: (value: string) => void;
    setTtl: (value: string) => void;
    setCustomUrl: (value: string) => void;
    handleSubmit: (e: React.FormEvent) => void;
  }
  
  const UrlForm: React.FC<UrlFormProps> = ({
    url,
    ttl,
    customUrl,
    setOriginalUrl,
    setTtl,
    setCustomUrl,
    handleSubmit,
  }) => (
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
  );
  
  export default UrlForm;