import { Link, NavLink, Outlet, useNavigate } from "react-router-dom";
import Button from "@mui/material/Button";
import { useAuth } from "../context/AuthContext";
export default function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  async function signOut() {
    try {
      await logout();
    } catch {
      /* local state is cleared even when the network is unavailable */
    }
    navigate("/login");
  }
  return (
    <>
      <div className="topline">
        DH BADMINTON STORE <span>CÙNG BẠN TRÊN MỖI ĐƯỜNG CẦU</span>
      </div>
      <header className="header">
        <Link
          to="/"
          className="brand"
          aria-label="DH Badminton Store trang chủ"
        >
          <span className="brand-mark">dh.</span>
          <span>
            BADMINTON
            <br />
            <small>STORE</small>
          </span>
        </Link>
        <nav aria-label="Điều hướng chính">
          <NavLink to="/products">Cửa hàng</NavLink>
          <NavLink to="/recommendations">Gợi ý cho bạn</NavLink>
          <NavLink to="/marketplace">Marketplace</NavLink>
          {user && (
            <>
              <NavLink to="/profile">Hồ sơ</NavLink>
              <NavLink to="/addresses">Địa chỉ</NavLink>
            </>
          )}
        </nav>
        <div className="header-actions">
          {user ? (
            <>
              <span className="greeting">{user.fullName}</span>
              <Button variant="outlined" onClick={signOut}>
                Đăng xuất
              </Button>
            </>
          ) : (
            <>
              <Button component={Link} to="/login">
                Đăng nhập
              </Button>
              <Button variant="contained" component={Link} to="/register">
                Tạo tài khoản
              </Button>
            </>
          )}
        </div>
      </header>
      {user && (
        <nav className="account-nav" aria-label="Tài khoản">
          {[
            ["/cart", "Giỏ hàng"],
            ["/orders", "Đơn cửa hàng"],
            ["/favorites", "Yêu thích"],
            ["/my-listings", "Tin của tôi"],
            ["/market-orders", "Giao dịch Marketplace"],
            ["/my-inspections", "Kiểm định"],
            ["/notifications", "Thông báo"],
          ].map(([to, label]) => (
            <NavLink key={to} to={to}>
              {label}
            </NavLink>
          ))}
          {user.role === "ADMIN" && <NavLink to="/admin">Quản trị</NavLink>}
          {["INSPECTOR", "ADMIN"].includes(user.role) && (
            <NavLink to="/inspector">Bàn kiểm định</NavLink>
          )}
        </nav>
      )}
      <main>
        <Outlet />
      </main>
      <footer>
        <Link to="/" className="footer-brand">
          dh. badminton store
        </Link>
        <p>Cầu lông. Phong cách. Kết nối.</p>
        <span>© 2026 DH Badminton Store</span>
      </footer>
    </>
  );
}
