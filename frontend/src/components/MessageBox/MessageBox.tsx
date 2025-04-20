import "./MessageBox.css";

const MessageBox: React.FC<{ message: string }> = ({ message }) => {
  if (!message) return null;

  return (
    <div
      className={`message-box ${
        message.includes("Error") ? "error" : "success"
      }`}
      role="alert"
      aria-live="assertive"
    >
      <p>{message}</p>
    </div>
  );
};

export default MessageBox;
