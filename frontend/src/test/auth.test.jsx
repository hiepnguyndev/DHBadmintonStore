import { afterEach, describe, expect, it, vi } from "vitest";
import {
  cleanup,
  fireEvent,
  render,
  screen,
  waitFor,
} from "@testing-library/react";
import { MemoryRouter, Routes, Route } from "react-router-dom";
import { AuthProvider } from "../context/AuthContext";
import LoginPage from "../pages/LoginPage";
import RegisterPage from "../pages/RegisterPage";
import ProtectedRoute, { AdminRoute } from "../routes/ProtectedRoute";
import { authApi } from "../services/authApi";
import { userApi } from "../services/userApi";
vi.mock("../services/authApi", () => ({
  authApi: { login: vi.fn(), register: vi.fn(), logout: vi.fn() },
}));
vi.mock("../services/userApi", () => ({ userApi: { me: vi.fn() } }));
afterEach(() => {
  cleanup();
  vi.clearAllMocks();
  localStorage.removeItem("dh-auth-token");
});
function view(page) {
  return render(
    <MemoryRouter>
      <AuthProvider>{page}</AuthProvider>
    </MemoryRouter>,
  );
}
describe("Auth screens", () => {
  it("restores a valid stored browser session", async () => {
    localStorage.setItem("dh-auth-token", "saved-token");
    userApi.me.mockResolvedValue({ id: 7, fullName: "Saved user", role: "USER" });
    view(<LoginPage />);
    await screen.findByText("Saved user").catch(() => {});
    expect(userApi.me).toHaveBeenCalledOnce();
  });
  it("requires login before profile", () => {
    render(
      <MemoryRouter initialEntries={["/profile"]}>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<div>Sign in required</div>} />
            <Route element={<ProtectedRoute />}>
              <Route path="/profile" element={<div>Private profile</div>} />
            </Route>
          </Routes>
        </AuthProvider>
      </MemoryRouter>,
    );
    expect(screen.getByText("Sign in required")).toBeInTheDocument();
    expect(screen.queryByText("Private profile")).not.toBeInTheDocument();
  });
  it("shows credential error without leaking details", async () => {
    authApi.login.mockRejectedValue({
      response: {
        status: 401,
        data: { message: "Email hoặc mật khẩu không đúng" },
      },
    });
    view(<LoginPage />);
    fireEvent.change(screen.getByLabelText("Email hoặc số điện thoại"), {
      target: { value: "a@example.com" },
    });
    fireEvent.change(screen.getByLabelText("Mật khẩu"), {
      target: { value: "incorrect" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Đăng nhập" }));
    expect(
      await screen.findByText("Email hoặc mật khẩu không đúng"),
    ).toBeInTheDocument();
    expect(authApi.login).toHaveBeenCalledWith({
      email: "a@example.com",
      password: "incorrect",
    });
  });
  it("blocks mismatched password confirmation before API", async () => {
    view(<RegisterPage />);
    fireEvent.change(screen.getByLabelText("Họ và tên"), {
      target: { value: "Name" },
    });
    fireEvent.change(screen.getByLabelText("Email"), {
      target: { value: "a@example.com" },
    });
    fireEvent.change(screen.getByLabelText("Mật khẩu"), {
      target: { value: "Password123!" },
    });
    fireEvent.change(screen.getByLabelText("Xác nhận mật khẩu"), {
      target: { value: "Different123!" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Tạo tài khoản" }));
    expect(
      await screen.findByText("Mật khẩu xác nhận không khớp"),
    ).toBeInTheDocument();
    expect(authApi.register).not.toHaveBeenCalled();
  });
  it("valid registration navigates to login without auto issuing a token", async () => {
    authApi.register.mockResolvedValue({ userId: 1, email: "a@example.com" });
    render(
      <MemoryRouter initialEntries={["/register"]}>
        <AuthProvider>
          <Routes>
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/login" element={<LoginPage />} />
          </Routes>
        </AuthProvider>
      </MemoryRouter>,
    );
    fireEvent.change(screen.getByLabelText("Họ và tên"), {
      target: { value: "Name" },
    });
    fireEvent.change(screen.getByLabelText("Email"), {
      target: { value: "a@example.com" },
    });
    fireEvent.change(screen.getByLabelText("Mật khẩu"), {
      target: { value: "Password123!" },
    });
    fireEvent.change(screen.getByLabelText("Xác nhận mật khẩu"), {
      target: { value: "Password123!" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Tạo tài khoản" }));
    expect(
      await screen.findByText("Đăng ký thành công. Bạn có thể đăng nhập ngay."),
    ).toBeInTheDocument();
    expect(authApi.login).not.toHaveBeenCalled();
  });
});
