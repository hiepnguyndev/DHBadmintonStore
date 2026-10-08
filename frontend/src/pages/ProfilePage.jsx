import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Alert from "@mui/material/Alert";
import Button from "@mui/material/Button";
import CircularProgress from "@mui/material/CircularProgress";
import Chip from "@mui/material/Chip";
import { useForm } from "react-hook-form";
import { useAuth } from "../context/AuthContext";
import { userApi } from "../services/userApi";
import { errorMessage, applyServerErrors } from "../services/apiClient";
import FormField from "../components/FormField";
function PasswordForm() {
  const [message, setMessage] = useState(null);
  const {
    register,
    handleSubmit,
    getValues,
    setError,
    reset,
    formState: { errors, isSubmitting },
  } = useForm();
  async function submit(data) {
    setMessage(null);
    try {
      await userApi.changePassword(data);
      reset();
      setMessage({ type: "success", text: "Đã đổi mật khẩu." });
    } catch (e) {
      setMessage({ type: "error", text: errorMessage(e) });
      applyServerErrors(e, setError);
    }
  }
  return (
    <section className="panel">
      <h2>Đổi mật khẩu</h2>
      <p className="muted">Sử dụng mật khẩu riêng cho tài khoản của bạn.</p>
      {message && <Alert severity={message.type}>{message.text}</Alert>}
      <form onSubmit={handleSubmit(submit)}>
        <FormField
          name="currentPassword"
          label="Mật khẩu hiện tại"
          type="password"
          autoComplete="current-password"
          register={register}
          errors={errors}
          rules={{ required: "Nhập mật khẩu hiện tại" }}
        />
        <FormField
          name="newPassword"
          label="Mật khẩu mới"
          type="password"
          autoComplete="new-password"
          register={register}
          errors={errors}
          rules={{
            required: "Nhập mật khẩu mới",
            minLength: { value: 8, message: "Ít nhất 8 ký tự" },
          }}
        />
        <FormField
          name="confirmPassword"
          label="Xác nhận mật khẩu mới"
          type="password"
          autoComplete="new-password"
          register={register}
          errors={errors}
          rules={{
            validate: (v) =>
              v === getValues("newPassword") || "Mật khẩu xác nhận không khớp",
          }}
        />
        <Button
          variant="outlined"
          type="submit"
          disabled={isSubmitting}
          sx={{ mt: 2 }}
        >
          {isSubmitting ? "Đang lưu…" : "Đổi mật khẩu"}
        </Button>
      </form>
    </section>
  );
}
export default function ProfilePage() {
  const { user, setUser } = useAuth();
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");
  const [message, setMessage] = useState(null);
  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors, isSubmitting },
  } = useForm();
  async function load() {
    setLoading(true);
    setLoadError("");
    try {
      const data = await userApi.me();
      setUser(data);
      reset({
        fullName: data.fullName,
        phone: data.phone || "",
        avatarUrl: data.avatarUrl || "",
      });
    } catch (e) {
      setLoadError(errorMessage(e));
    } finally {
      setLoading(false);
    }
  }
  useEffect(() => {
    load();
  }, []);
  async function submit(data) {
    setMessage(null);
    try {
      const result = await userApi.update(data);
      setUser(result);
      setMessage({ type: "success", text: "Đã cập nhật hồ sơ." });
    } catch (e) {
      setMessage({ type: "error", text: errorMessage(e) });
      applyServerErrors(e, setError);
    }
  }
  return (
    <div className="page-container">
      <div className="page-heading">
        <div>
          <span className="eyebrow">KHÔNG GIAN CỦA BẠN</span>
          <h1>Hồ sơ cá nhân</h1>
        </div>
        <Button component={Link} to="/addresses" variant="outlined">
          Quản lý địa chỉ →
        </Button>
      </div>
      {loading ? (
        <CircularProgress aria-label="Đang tải hồ sơ" />
      ) : loadError ? (
        <Alert
          severity="error"
          action={<Button onClick={load}>Thử lại</Button>}
        >
          {loadError}
        </Alert>
      ) : (
        <div className="profile-grid">
          <section className="panel">
            <div className="profile-identity">
              <div className="avatar-letter">
                {user.fullName.charAt(0).toUpperCase()}
              </div>
              <div>
                <h2>{user.fullName}</h2>
                <span className="muted">{user.email}</span>
              </div>
              <Chip size="small" label={user.role} variant="outlined" />
            </div>
            {message && <Alert severity={message.type}>{message.text}</Alert>}
            <form onSubmit={handleSubmit(submit)}>
              <FormField
                name="fullName"
                label="Họ và tên"
                autoComplete="name"
                register={register}
                errors={errors}
                rules={{
                  required: "Nhập họ và tên",
                  maxLength: { value: 100, message: "Tối đa 100 ký tự" },
                }}
              />
              <FormField
                name="phone"
                label="Số điện thoại"
                autoComplete="tel"
                register={register}
                errors={errors}
                rules={{ maxLength: { value: 20, message: "Tối đa 20 ký tự" } }}
              />
              <FormField
                name="avatarUrl"
                label="Đường dẫn ảnh đại diện (URL)"
                type="url"
                register={register}
                errors={errors}
                rules={{
                  maxLength: { value: 500, message: "Tối đa 500 ký tự" },
                }}
              />
              <Button
                variant="contained"
                type="submit"
                disabled={isSubmitting}
                sx={{ mt: 2 }}
              >
                {isSubmitting ? "Đang lưu…" : "Lưu thay đổi"}
              </Button>
            </form>
          </section>
          <PasswordForm />
        </div>
      )}
    </div>
  );
}
