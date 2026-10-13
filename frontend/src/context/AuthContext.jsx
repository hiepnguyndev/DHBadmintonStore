import { createContext, useContext, useEffect, useState } from "react";
import { authApi } from "../services/authApi";
import { userApi } from "../services/userApi";
import { setAccessToken, setUnauthorizedHandler } from "../services/apiClient";
const AuthContext = createContext(null);
const SESSION_KEY = "dh-auth-token";
function savedToken() {
  try { return localStorage.getItem(SESSION_KEY); } catch { return null; }
}
function persistToken(token) {
  try {
    if (token) localStorage.setItem(SESSION_KEY, token);
    else localStorage.removeItem(SESSION_KEY);
  } catch { /* Authentication still works for this tab when storage is unavailable. */ }
}
export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [sessionMessage, setSessionMessage] = useState("");
  const [restoring, setRestoring] = useState(true);
  useEffect(() => {
    setUnauthorizedHandler(() => {
      setUser(null);
      persistToken(null);
      setSessionMessage("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
    });
    const token = savedToken();
    if (!token) { setRestoring(false); return () => setUnauthorizedHandler(() => {}); }
    setAccessToken(token);
    userApi.me().then((currentUser) => {
      setUser(currentUser);
    }).catch(() => {
      setAccessToken(null);
      persistToken(null);
      setSessionMessage("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
    }).finally(() => setRestoring(false));
    return () => setUnauthorizedHandler(() => {});
  }, []);
  async function login(request) {
    const result = await authApi.login(request);
    setAccessToken(result.accessToken);
    persistToken(result.accessToken);
    setUser(result.user);
    setSessionMessage("");
    return result.user;
  }
  async function logout() {
    try {
      await authApi.logout();
    } finally {
      setAccessToken(null);
      persistToken(null);
      setUser(null);
      setSessionMessage("");
    }
  }
  if (restoring) return null;
  return (
    <AuthContext.Provider
      value={{ user, setUser, login, logout, sessionMessage }}
    >
      {children}
    </AuthContext.Provider>
  );
}
export const useAuth = () => useContext(AuthContext);
