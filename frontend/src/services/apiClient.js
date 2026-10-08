import axios from "axios";
// Access token stays in memory. A page reload requires login again (CHG-011: no refresh).
let accessToken = null;
let onUnauthorized = () => {};
export const setAccessToken = (token) => {
  accessToken = token;
};
export const setUnauthorizedHandler = (handler) => {
  onUnauthorized = handler;
};
export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api",
  timeout: 15000,
});
apiClient.interceptors.request.use((config) => {
  if (accessToken) config.headers.Authorization = `Bearer ${accessToken}`;
  return config;
});
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && accessToken) {
      accessToken = null;
      onUnauthorized();
    }
    return Promise.reject(error);
  },
);
export function errorMessage(error) {
  return (
    error.response?.data?.message ||
    (error.code === "ECONNABORTED"
      ? "Máy chủ phản hồi quá lâu. Vui lòng thử lại."
      : "Không kết nối được máy chủ. Kiểm tra backend và thử lại.")
  );
}
export function applyServerErrors(error, setError) {
  Object.entries(error.response?.data?.errors || {}).forEach(
    ([name, message]) => setError(name, { type: "server", message }),
  );
}
