import { createContext, useContext, useEffect, useState } from "react";
import { authApi } from "../services/authApi";
import { setAccessToken, setUnauthorizedHandler } from "../services/apiClient";
const AuthContext = createContext(null);
export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [sessionMessage, setSessionMessage] = useState("");
  useEffect(() => {
    setUnauthorizedHandler(() => {
      setUser(null);
      setSessionMessage("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
    });
    return () => setUnauthorizedHandler(() => {});
  }, []);
  async function login(request) {
    const result = await authApi.login(request);
    setAccessToken(result.accessToken);
    setUser(result.user);
    setSessionMessage("");
    return result.user;
  }
  async function logout() {
    try {
      await authApi.logout();
    } finally {
      setAccessToken(null);
      setUser(null);
      setSessionMessage("");
    }
  }
  return (
    <AuthContext.Provider
      value={{ user, setUser, login, logout, sessionMessage }}
    >
      {children}
    </AuthContext.Provider>
  );
}
export const useAuth = () => useContext(AuthContext);
