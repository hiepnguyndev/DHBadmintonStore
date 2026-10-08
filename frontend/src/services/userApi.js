import { apiClient } from "./apiClient";
export const userApi = {
  me: () => apiClient.get("/users/me").then((r) => r.data.data),
  update: (request) =>
    apiClient.put("/users/me", request).then((r) => r.data.data),
  changePassword: (request) => apiClient.put("/users/change-password", request),
  addresses: () => apiClient.get("/addresses").then((r) => r.data.data),
  createAddress: (request) =>
    apiClient.post("/addresses", request).then((r) => r.data.data),
  updateAddress: (id, request) =>
    apiClient.put(`/addresses/${id}`, request).then((r) => r.data.data),
  deleteAddress: (id) => apiClient.delete(`/addresses/${id}`),
};
