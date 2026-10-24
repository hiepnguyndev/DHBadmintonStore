import { apiClient } from "./apiClient";
export const authApi = {
  register: (request) =>
    apiClient.post("/auth/register", request).then((r) => r.data.data),
  login: (request) =>
    apiClient.post("/auth/login", request).then((r) => r.data.data),
  logout: () => apiClient.post("/auth/logout"),
};
