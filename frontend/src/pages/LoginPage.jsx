import { useState } from "react";
import { Link, Navigate, useLocation, useNavigate } from "react-router-dom";
import Alert from "@mui/material/Alert";
import Button from "@mui/material/Button";
import { useForm } from "react-hook-form";
import { useAuth } from "../context/AuthContext";
import { errorMessage, applyServerErrors } from "../services/apiClient";
import FormField from "../components/FormField";
import AuthAside from "../components/AuthAside";
import { ArrowRight } from "../components/icons";

export default function LoginPage() {
  const { user, login, sessionMessage } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [error, setErrorMessage] = useState("");
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm();
  const from = location.state?.from;
  const destinationFor = (role) => role === "ADMIN" ? "/admin"
    : typeof from === "string" && from.startsWith("/") && !from.startsWith("//")
      ? from : "/profile";
  if (user)
    return (
      <Navigate
        to={destinationFor(user.role)}
        replace
      />
    );

  async function submit(data) {
    setErrorMessage("");
    try {
      const identifier = data.identifier.trim();
      const credentials = identifier.includes("@")
        ? { email: identifier, password: data.password }
        : { phone: identifier, password: data.password };
      const loggedInUser = await login(credentials);
      navigate(destinationFor(loggedInUser.role), { replace: true });
    } catch (e) {
      setErrorMessage(errorMessage(e));
      applyServerErrors(e, setError);
    }
  }

  return (
    <section className="auth-shell">
      <AuthAside />
      <div className="auth-form">
        <span className="eyebrow">TÀI KHOẢN DH</span>
        <h1>Đăng nhập</h1>
        <p className="muted">Chào mừng bạn quay trở lại DH Badminton Store.</p>
        {location.state?.registered && (
          <Alert severity="success">
            Đăng ký thành công. Bạn có thể đăng nhập ngay.
          </Alert>
        )}
        {sessionMessage && <Alert severity="info">{sessionMessage}</Alert>}
        {error && (
          <Alert severity="error" role="alert">
            {error}
          </Alert>
        )}
        <form onSubmit={handleSubmit(submit)}>
          <FormField
            name="identifier"
            label="Email hoặc số điện thoại"
            type="text"
            autoComplete="username"
            defaultValue={location.state?.identifier ?? ""}
            register={register}
            errors={errors}
            rules={{ required: "Nhập email hoặc số điện thoại" }}
          />
          <FormField
            name="password"
            label="Mật khẩu"
            type="password"
            autoComplete="current-password"
            register={register}
            errors={errors}
            rules={{ required: "Nhập mật khẩu" }}
          />
          <Button
            type="submit"
            fullWidth
            size="large"
            variant="contained"
            endIcon={<ArrowRight size={18} />}
            disabled={isSubmitting}
            sx={{ mt: 2 }}
          >
            {isSubmitting ? "Đang đăng nhập…" : "Đăng nhập"}
          </Button>
        </form>
        <p className="form-footer">
          Chưa có tài khoản? <Link to="/register">Đăng ký ngay</Link>
        </p>
      </div>
    </section>
  );
}
