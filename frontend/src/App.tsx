import { useState } from "react";
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

const MainApp: React.FC = () => {
  const [url, setOriginalUrl] = useState("");
  const [shortUrl, setShortUrl] = useState("");
  const [ttl, setTtl] = useState("");
  const [customUrl, setCustomUrl] = useState("");
  const [allUrls, setAllUrls] = useState<UrlData[]>([]);
  const [message, setMessage] = useState("");


  const handleShorten = async () => {
    try {
      const requestBody: CreationRequest = {
        url: url,
        ttlMinute: ttl ? parseInt(ttl) : null,
        customShortenedUrl: customUrl,
      };

      console.log("Request Body:", requestBody);

      const response = await fetch(BASE_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(requestBody),
      });

      const responseText = await response.text();

      if (response.status === 429) {
        setMessage("Rate limit exceeded. Please try again later.");
        return;
      }

      if (response.ok) {
        setShortUrl(`${BASE_URL}/${responseText}`);
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
      const response = await fetch(`${BASE_URL}/all`);
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
        `${BASE_URL}/delete?shortenedUrl=${shortenedUrl}`,
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
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    await handleShorten();
    await handleGetAll();
  };
  return (
    <div className="app-container">
      <h1>URL Shortener</h1>
      <UrlForm
        url={url}
        ttl={ttl}
        customUrl={customUrl}
        setOriginalUrl={setOriginalUrl}
        setTtl={setTtl}
        setCustomUrl={setCustomUrl}
        handleSubmit={handleSubmit}
      />
      {shortUrl && <ResultBox shortUrl={shortUrl} />}
      {message && <MessageBox message={message} />}
      <UrlList allUrls={allUrls} handleGetAll= {handleGetAll} handleDelete={handleDelete} />
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
