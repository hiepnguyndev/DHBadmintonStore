import { createContext, useCallback, useContext, useRef, useState } from "react";

const ChatbotContext = createContext(null);

export function ChatbotProvider({ children }) {
  const [open, setOpen] = useState(false);
  const [profileRequest, setProfileRequest] = useState(0);
  const returnFocus = useRef(null);
  const openChatbot = useCallback((source) => {
    returnFocus.current = document.activeElement;
    if (source === "profile") setProfileRequest(value => value + 1);
    setOpen(true);
  }, []);
  const closeChatbot = useCallback(() => {
    setOpen(false);
    if (returnFocus.current?.isConnected) returnFocus.current.focus();
  }, []);
  return <ChatbotContext.Provider value={{ open, profileRequest, openChatbot, closeChatbot }}>{children}</ChatbotContext.Provider>;
}

export function useChatbot() {
  return useContext(ChatbotContext);
}
