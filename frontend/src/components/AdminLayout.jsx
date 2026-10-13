import { useEffect, useState } from "react";
import { Link, NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import { ArrowUpRight, ChartNoAxesCombined, ClipboardList, LayoutDashboard, LogOut, Menu, Package, ScrollText, ShieldAlert, Sparkles, Tag, Truck, Users, X } from "./icons";
import { useAuth } from "../context/AuthContext";
import Brand from "./Brand";

const groups = [["CỬA HÀNG", [["/admin", "Tổng quan", LayoutDashboard], ["/admin/products", "Sản phẩm", Package], ["/admin/promotions", "Ưu đãi", Tag], ["/admin/orders", "Đơn hàng", ClipboardList], ["/admin/shipments", "Vận chuyển", Truck]]], ["CỘNG ĐỒNG", [["/admin/posts", "Bài viết", ScrollText], ["/admin/listings", "Duyệt tin đăng", ScrollText], ["/admin/disputes", "Tranh chấp", ShieldAlert], ["/admin/users", "Người dùng", Users]]], ["HỆ THỐNG", [["/admin/statistics", "Báo cáo & thống kê", ChartNoAxesCombined], ["/admin/audit-logs", "Nhật ký hoạt động", ScrollText]]]];
export default function AdminLayout() {
  const { user, logout } = useAuth();
  const location = useLocation(), navigate = useNavigate();
  const [open, setOpen] = useState(false);
  useEffect(() => { setOpen(false); window.scrollTo(0, 0); }, [location.pathname]);
  useEffect(() => { const close = event => { if (event.key === "Escape") setOpen(false); }; document.addEventListener("keydown", close); return () => document.removeEventListener("keydown", close); }, []);
  async function signOut() { try { await logout(); } catch { /* The local session is already cleared. */ } navigate("/login"); }
  const label = groups.flatMap(([, links]) => links).find(([to]) => to === location.pathname)?.[1] || "Quản trị";
  return <div className="admin-page-shell">{open && <button className="admin-backdrop" aria-label="Đóng điều hướng" onClick={() => setOpen(false)} />}
    <aside className={`admin-sidebar${open ? " is-open" : ""}`}><div className="admin-brand"><Brand to="/admin" /><button className="icon-button mobile-menu-toggle" aria-label="Đóng menu" onClick={() => setOpen(false)}><X size={20} /></button></div><span className="workspace-label"><span /> STORE WORKSPACE</span><nav aria-label="Điều hướng quản trị">{groups.map(([group, links]) => <div className="admin-nav-group" key={group}><small>{group}</small>{links.map(([to, title, Icon]) => <NavLink key={to} to={to} end={to === "/admin"} className="admin-nav-link"><Icon size={18} /><span>{title}</span></NavLink>)}</div>)}</nav><div className="admin-sidebar-bottom"><Link to="/"><ArrowUpRight size={17} />Xem cửa hàng</Link><button onClick={signOut}><LogOut size={17} />Đăng xuất</button></div></aside>
    <div className="admin-workspace"><header className="admin-topbar"><button className="icon-button mobile-menu-toggle" aria-label="Mở menu" aria-expanded={open} onClick={() => setOpen(true)}><Menu size={21} /></button><div className="admin-breadcrumb">Workspace<span>/</span><strong>{label}</strong></div><div className="admin-account"><span className="admin-avatar">{user?.fullName?.charAt(0) || "A"}</span><div><strong>{user?.fullName || "Admin"}</strong><small>Quản trị viên</small></div></div></header><main className="admin-content"><Outlet /></main><div className="admin-workspace-footer"><span>DH BADMINTON STORE</span><span><Sparkles size={13} /> STORE OPERATIONS</span></div></div>
  </div>;
}
