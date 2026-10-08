import { Link, NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const groups = [
  {
    title: "TỔNG QUAN",
    links: [["/admin", "Tổng quan", "◫", true]],
  },
  {
    title: "CỬA HÀNG",
    links: [
      ["/admin/products", "Sản phẩm", "▤"],
      ["/admin/orders", "Đơn cửa hàng", "≡"],
      ["/admin/shipments", "Vận chuyển", "⇢"],
    ],
  },
  {
    title: "MARKETPLACE",
    links: [
      ["/admin/listings", "Duyệt tin đăng", "◇"],
      ["/admin/disputes", "Tranh chấp", "!"],
    ],
  },
  {
    title: "HỆ THỐNG",
    links: [
      ["/admin/users", "Người dùng", "◎"],
      ["/admin/statistics", "Thống kê", "▥"],
      ["/admin/audit-logs", "Nhật ký hệ thống", "≋"],
    ],
  },
];

const pageNames = {
  "/admin": "Tổng quan",
  "/admin/products": "Sản phẩm",
  "/admin/orders": "Đơn cửa hàng",
  "/admin/shipments": "Vận chuyển",
  "/admin/listings": "Duyệt tin đăng",
  "/admin/disputes": "Tranh chấp",
  "/admin/users": "Người dùng",
  "/admin/statistics": "Thống kê",
  "/admin/audit-logs": "Nhật ký hệ thống",
};

export default function AdminLayout() {
  const { user, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  async function signOut() {
    try {
      await logout();
    } catch {
      // The local session is cleared even if the API is unavailable.
    }
    navigate("/login");
  }

  return (
    <div className="admin-shell">
      <aside className="admin-sidebar">
        <Link to="/admin" className="admin-brand">
          <span className="admin-brand-mark">DH</span>
          <span className="admin-brand-copy">
            <strong>BADMINTON</strong>
            <small>ADMIN CONSOLE</small>
          </span>
        </Link>

        <nav className="admin-sidebar-nav" aria-label="Điều hướng quản trị">
          {groups.map((group) => (
            <div className="admin-nav-group" key={group.title}>
              <p className="admin-nav-heading">{group.title}</p>
              {group.links.map(([to, label, icon, end]) => (
                <NavLink
                  key={to}
                  to={to}
                  end={Boolean(end)}
                  className={({ isActive }) =>
                    `admin-nav-link${isActive ? " active" : ""}`
                  }
                >
                  <span className="admin-nav-icon" aria-hidden="true">
                    {icon}
                  </span>
                  <span>{label}</span>
                </NavLink>
              ))}
            </div>
          ))}
        </nav>

        <div className="admin-sidebar-footer">
          <NavLink to="/inspector" className="admin-footer-link">
            Bàn kiểm định <span aria-hidden="true">↗</span>
          </NavLink>
          <Link to="/" className="admin-footer-link">
            Về cửa hàng <span aria-hidden="true">↗</span>
          </Link>
        </div>
      </aside>

      <div className="admin-main">
        <header className="admin-topbar">
          <div className="admin-breadcrumb">
            <span>DH BADMINTON STORE</span>
            <strong>{pageNames[location.pathname] || "Quản trị"}</strong>
          </div>
          <div className="admin-account">
            <span className="admin-avatar" aria-hidden="true">
              {user?.fullName?.trim()?.charAt(0)?.toUpperCase() || "A"}
            </span>
            <span className="admin-account-name">
              <strong>{user?.fullName || "Quản trị viên"}</strong>
              <small>ADMIN</small>
            </span>
            <button className="admin-logout" onClick={signOut}>
              Đăng xuất
            </button>
          </div>
        </header>
        <main className="admin-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}