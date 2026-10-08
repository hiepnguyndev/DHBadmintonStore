import { useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import Alert from "@mui/material/Alert";
import Button from "@mui/material/Button";
import { useForm } from "react-hook-form";
import { authApi } from "../services/authApi";
import { errorMessage, applyServerErrors } from "../services/apiClient";
import { useAuth } from "../context/AuthContext";
import FormField from "../components/FormField";

export default function RegisterPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [contactType, setContactType] = useState("email");
  const [error, setErrorMessage] = useState("");
  const {
    register,
    handleSubmit,
    getValues,
    setError,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm();

  if (user) return <Navigate to="/profile" replace />;

  async function submit(data) {
    setErrorMessage("");
    try {
      const contact = data.contact.trim();
      await authApi.register({
        fullName: data.fullName,
        [contactType]: contact,
        password: data.password,
        confirmPassword: data.confirmPassword,
      });
      navigate("/login", {
        state: { registered: true, identifier: contact },
        replace: true,
      });
    } catch (e) {
      setErrorMessage(errorMessage(e));
      applyServerErrors(e, setError);
    }
  }

  return (
    <section className="auth-shell">
      <aside className="auth-aside">
        <span className="eyebrow">YOUR GAME STARTS HERE</span>
        <h2>
          Một tài khoản.
          <br />
          Nhiều
          <br />
          <em>kết nối.</em>
        </h2>
        <p>Bắt đầu hành trình cầu lông của riêng bạn.</p>
      </aside>
      <div className="auth-form">
        <span className="eyebrow">GIA NHẬP DH</span>
        <h1>Tạo tài khoản</h1>
        <p className="muted">Đăng ký bằng email hoặc số điện thoại và mật khẩu.</p>
        {error && (
          <Alert severity="error" role="alert">
            {error}
          </Alert>
        )}
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
          <div
            className="contact-methods"
            role="group"
            aria-label="Phương thức đăng ký"
          >
            <Button
              type="button"
              fullWidth
              variant={contactType === "email" ? "contained" : "outlined"}
              aria-pressed={contactType === "email"}
              onClick={() => {
                setValue("contact", "");
                setContactType("email");
              }}
            >
              Email
            </Button>
            <Button
              type="button"
              fullWidth
              variant={contactType === "phone" ? "contained" : "outlined"}
              aria-pressed={contactType === "phone"}
              onClick={() => {
                setValue("contact", "");
                setContactType("phone");
              }}
            >
              Số điện thoại
            </Button>
          </div>
          <FormField
            key={contactType}
            name="contact"
            label={contactType === "email" ? "Email" : "Số điện thoại"}
            type={contactType === "email" ? "email" : "tel"}
            placeholder={
              contactType === "phone" ? "0912 345 678 hoặc +84912..." : undefined
            }
            autoComplete={contactType === "email" ? "email" : "tel"}
            register={register}
            errors={errors}
            rules={{
              required: contactType === "email" ? "Nhập email" : "Nhập số điện thoại",
              ...(contactType === "phone"
                ? {
                    maxLength: { value: 30, message: "Tối đa 30 ký tự" },
                    pattern: {
                      value: /^[+0-9\s().-]{8,30}$/,
                      message: "Số điện thoại chưa đúng định dạng",
                    },
                  }
                : {}),
            }}
          />
          {contactType === "phone" && (
            <p className="contact-hint">
              Có thể nhập số Việt Nam 10 chữ số hoặc số quốc tế bắt đầu bằng +.
            </p>
          )}
          <FormField
            name="password"
            label="Mật khẩu"
            type="password"
            autoComplete="new-password"
            register={register}
            errors={errors}
            rules={{
              required: "Nhập mật khẩu",
              minLength: { value: 8, message: "Ít nhất 8 ký tự" },
            }}
          />
          <FormField
            name="confirmPassword"
            label="Xác nhận mật khẩu"
            type="password"
            autoComplete="new-password"
            register={register}
            errors={errors}
            rules={{
              required: "Xác nhận mật khẩu",
              validate: (v) =>
                v === getValues("password") || "Mật khẩu xác nhận không khớp",
            }}
          />
          <Button
            type="submit"
            fullWidth
            size="large"
            variant="contained"
            disabled={isSubmitting}
            sx={{ mt: 2 }}
          >
            {isSubmitting ? "Đang tạo tài khoản…" : "Tạo tài khoản"}
          </Button>
        </form>
        <p className="form-footer">
          Đã có tài khoản? <Link to="/login">Đăng nhập</Link>
        </p>
      </div>
    </section>
  );
}
