import React, { useEffect } from "react";
import { useParams } from "react-router-dom";

const RedirectPage: React.FC = () => {
  const { shortId } = useParams<{ shortId: string }>();
  //const [originalUrl, setOriginalUrl] = useState<string>("");

  useEffect(() => {
    const redirect = async () => {
      try {
        const res = await fetch(`http://localhost:8080/short/${shortId}`);
        if (!res.ok) throw new Error("URL not found");
  
        const originalUrl = await res.text(); // 👈 use text() here
        window.location.href = originalUrl;
      } catch (err) {
        console.error("Redirect failed:", err);
      }
    };
  
    redirect();
  }, [shortId]);
  return (
    <div style={{ textAlign: "center", marginTop: "50px" }}>
      <h2>Redirecting to:</h2>
      <p style={{ wordBreak: "break-word", color: "#007bff" }}>
        {"http://localhost:8080/short/" + shortId || "Loading..."}
      </p>
    </div>
  );
};

export default RedirectPage;
