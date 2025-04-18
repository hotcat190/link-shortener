import "./MessageBox.css";
const MessageBox: React.FC<{ message: string }> = ({ message }) => (
    <div className={`message-box ${message.includes("Error") ? "error" : "success"}`}>
      <p>{message}</p>
    </div>
  );
  
  export default MessageBox;