import "./ResultBox.css";
const ResultBox: React.FC<{ shortUrl: string }> = ({ shortUrl }) => (
    <div className="result-box">
      <p>
        Shortened URL: <a href={shortUrl} target="_blank" rel="noopener noreferrer">{shortUrl}</a>
      </p>
    </div>
  );
  
  export default ResultBox;
  