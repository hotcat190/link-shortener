import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";

const RedirectPage: React.FC = () => {
  const { shortId } = useParams<{ shortId: string }>();
  const [originalUrl, setOriginalUrl] = useState<string>("");

  useEffect(() => {
    if (!shortId) return;

    fetch(`http://localhost:8080/short/${shortId}`)
      .then(async (res) => {
        if (res.status === 429) {
          throw new Error("Rate limit exceeded");
        }
        if (!res.ok) {
          throw new Error("Short URL not found");
        }
        const url = await res.text(); // not res.json()!
        setOriginalUrl(url);
        setTimeout(() => {
          window.location.href = url;
        }, 500); // Redirect after 1.5 seconds
      })
      .catch((err) => {
        console.error("Redirect error:", err);
        setOriginalUrl("Error: " + err.message);
      });
  }, [shortId]);

  return (
    <div style={{ textAlign: "center", marginTop: "50px" }}>
      <h2>Redirecting to:</h2>
      <p style={{ wordBreak: "break-word", color: "#007bff" }}>
        {originalUrl || "Loading..."}
      </p>
    </div>
  );
};

export default RedirectPage;
