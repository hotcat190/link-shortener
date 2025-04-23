import React, { useEffect } from "react";
import { useParams } from "react-router-dom";
import { BASE_BACKEND_URL } from "./constants";

const RedirectPage: React.FC = () => {
  const { shortId } = useParams<{ shortId: string }>();
  //const [originalUrl, setOriginalUrl] = useState<string>("");

  useEffect(() => {
    const redirect = async () => {
      console.log("Base URL called in RedirectPage:", BASE_BACKEND_URL);
      try {
        const res = await fetch(`${BASE_BACKEND_URL}/${shortId}`);
        if (!res.ok) throw new Error("URL not found");

        const originalUrl = await res.text(); // 👈 use text() here
        setTimeout(() => {
          window.location.href = originalUrl;
        }, 1000);
      } catch (err) {
        console.error("Redirect failed:", err);
      }
    };

    redirect();
  }, [shortId]);

  return (
    <div style={{ textAlign: "center", marginTop: "50px" }}>
      <h2 color="black">Redirecting to:</h2>
      <p style={{ wordBreak: "break-word", color: "#007bff" }}>
        {BASE_BACKEND_URL + "/" + shortId || "Loading..."}
      </p>
    </div>
  );
};

export default RedirectPage;
