import { useEffect, useRef, useState } from "react";
import { Link, NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import { ArrowUpRight, Check, ChevronDown, Globe2, Heart, Menu, Search, ShieldCheck, ShoppingBag, Sparkles, Truck, UserRound, X } from "./icons";
import { useAuth } from "../context/AuthContext";
import { apiClient } from "../services/apiClient";
import Brand from "./Brand";

const accountLinks = [
  ["/profile", "Hồ sơ", "Profile"], ["/player-profile", "Hồ sơ người chơi", "Player profile"],
  ["/addresses", "Địa chỉ", "Addresses"], ["/orders", "Đơn hàng", "Orders"],
  ["/favorites", "Yêu thích", "Favorites"], ["/my-listings", "Tin của tôi", "My listings"],
  ["/market-orders", "Giao dịch", "Transactions"], ["/my-inspections", "Kiểm định", "Inspections"],
  ["/notifications", "Thông báo", "Notifications"],
];
function LanguageSwitcher({ language, setLanguage }) {
  const [open, setOpen] = useState(false);
  const ref = useRef(null);
  useEffect(() => {
    const close = (event) => { if (!ref.current?.contains(event.target) || event.key === "Escape") setOpen(false); };
    document.addEventListener("pointerdown", close);
    document.addEventListener("keydown", close);
    return () => { document.removeEventListener("pointerdown", close); document.removeEventListener("keydown", close); };
  }, []);
  function choose(value) { localStorage.setItem("dh-language", value); setLanguage(value); setOpen(false); }
  return <div className="language-control" ref={ref}>
    <button className="language-chip" type="button" aria-label={language === "en" ? "Choose language" : "Chọn ngôn ngữ"} aria-haspopup="menu" aria-expanded={open} onClick={() => setOpen(!open)}><Globe2 size={16} /><span>{language.toUpperCase()}</span><ChevronDown size={12} /></button>
    {open && <div className="language-menu" role="menu">{[["vi", "Tiếng Việt"], ["en", "English"]].map(([value, label]) => <button key={value} role="menuitemradio" aria-checked={language === value} onClick={() => choose(value)}>{label}{language === value && <Check size={15} />}</button>)}</div>}
  </div>;
}

function CategoryMenu({ user, en }) {
  const [open, setOpen] = useState(false);
  const ref = useRef(null);
  const links = user
    ? [...accountLinks, ...(user.role === "ADMIN" ? [["/admin", "Quản trị", "Admin"]] : []), ...(["ADMIN", "INSPECTOR"].includes(user.role) ? [["/inspector", "Bàn kiểm định", "Inspection desk"]] : [])]
    : [["/login", "Đăng nhập", "Sign in"]];

  useEffect(() => {
    const close = (event) => {
      if (!ref.current?.contains(event.target) || event.key === "Escape") setOpen(false);
    };
    document.addEventListener("pointerdown", close);
    document.addEventListener("keydown", close);
    return () => {
      document.removeEventListener("pointerdown", close);
      document.removeEventListener("keydown", close);
    };
  }, []);

  return <div className="category-menu" ref={ref}>
    <button type="button" className="category-menu-trigger" aria-haspopup="menu" aria-expanded={open} onClick={() => setOpen(value => !value)}>
      <Menu size={15} />{en ? "Categories" : "Danh mục"}<ChevronDown size={14} />
    </button>
    {open && <div className="category-menu-popover" role="menu">
      {links.map(([to, vi, english]) => <NavLink key={to} to={to} role="menuitem" onClick={() => setOpen(false)}>{en ? english : vi}</NavLink>)}
    </div>}
  </div>;
}

function ProductSearch({ en, navigate }) {
  const [keyword, setKeyword] = useState("");
  const [products, setProducts] = useState([]);
  const [open, setOpen] = useState(false);
  const ref = useRef(null);
  const trimmed = keyword.trim();

  useEffect(() => {
    if (trimmed.length < 2) {
      setProducts([]);
      return undefined;
    }
    let live = true;
    const timer = window.setTimeout(() => {
      apiClient.get("/products", { params: { keyword: trimmed, page: 0, size: 5 } })
        .then(response => {
          if (live) {
            setProducts(response.data.data?.content || []);
            setOpen(true);
          }
        })
        .catch(() => { if (live) setProducts([]); });
    }, 220);
    return () => { live = false; window.clearTimeout(timer); };
  }, [trimmed]);

  useEffect(() => {
    const close = event => {
      if (!ref.current?.contains(event.target) || event.key === "Escape") setOpen(false);
    };
    document.addEventListener("pointerdown", close);
    document.addEventListener("keydown", close);
    return () => {
      document.removeEventListener("pointerdown", close);
      document.removeEventListener("keydown", close);
    };
  }, []);

  function submit(event) {
    event.preventDefault();
    setOpen(false);
    navigate(`/products?${new URLSearchParams(trimmed ? { keyword: trimmed } : {})}`);
  }
  function choose(product) {
    setOpen(false);
    setKeyword("");
    navigate(`/products/${product.id}`);
  }

  return <div className="header-search-wrap" ref={ref}>
    <form className="header-search" onSubmit={submit} role="search">
      <Search size={18} />
      <input value={keyword} onFocus={() => trimmed.length >= 2 && setOpen(true)} onChange={event => setKeyword(event.target.value)} placeholder={en ? "Search rackets, shoes, brands..." : "Tìm vợt, giày, thương hiệu bạn yêu thích..."} aria-label={en ? "Search products" : "Tìm kiếm sản phẩm"} aria-autocomplete="list" aria-controls="product-search-suggestions" aria-expanded={open && products.length > 0} />
      <button type="submit" aria-label={en ? "Search" : "Tìm kiếm"}><ArrowUpRight size={18} /></button>
    </form>
    {open && products.length > 0 && <div className="product-search-suggestions" id="product-search-suggestions" role="listbox">
      {products.map(product => <button key={product.id} type="button" role="option" onClick={() => choose(product)}>
        {product.thumbnailUrl ? <img src={product.thumbnailUrl.startsWith("/media/") ? new URL(product.thumbnailUrl, new URL(apiClient.defaults.baseURL, window.location.origin)).href : product.thumbnailUrl} alt="" /> : <span className="search-product-placeholder">DH</span>}
        <span><strong>{product.productName}</strong><small>{product.brand?.name || product.category?.name || (en ? "Product" : "Sản phẩm")}</small></span>
        <b>{new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND", maximumFractionDigits: 0 }).format(product.effectivePrice ?? product.salePrice ?? product.price ?? 0)}</b>
      </button>)}
    </div>}
  </div>;
}

export default function Layout() {
  const { user } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);
  const [language, setLanguage] = useState(() => localStorage.getItem("dh-language") || "vi");
  const en = language === "en";
  useEffect(() => { document.documentElement.lang = language; }, [language]);
  useEffect(() => { setMenuOpen(false); window.scrollTo(0, 0); }, [location.pathname]);
  useEffect(() => { const close = event => { if (event.key === "Escape") setMenuOpen(false); }; document.addEventListener("keydown", close); return () => document.removeEventListener("keydown", close); }, []);
  const nav = [["/", en ? "Home" : "Trang chủ"], ["/products", en ? "Products" : "Sản phẩm"], ["/marketplace", "Marketplace"], ["/promotions", en ? "Offers" : "Ưu đãi"], ["/news", en ? "News" : "Tin tức"]];
  return <div className="site-shell">
    <a className="skip-link" href="#main-content">{en ? "Skip to content" : "Đến nội dung chính"}</a>
    <div className="announcement"><span><Sparkles size={13} /> DH BADMINTON</span><p>{en ? "Gear for every point." : "Dụng cụ cho từng đường cầu."}</p></div>
    <header className="site-header"><div className="header-main"><Brand />
      <div className="header-actions"><Link className="icon-button optional-action" to="/favorites" aria-label={en ? "Favorites" : "Yêu thích"} title={en ? "Favorites" : "Yêu thích"}><Heart size={20} /></Link><Link className="icon-button" to="/cart" aria-label={en ? "Cart" : "Giỏ hàng"} title={en ? "Cart" : "Giỏ hàng"}><ShoppingBag size={20} /></Link><Link className="icon-button account-button" to={user ? "/profile" : "/login"} aria-label={en ? "Account" : "Tài khoản"} title={en ? "Account" : "Tài khoản"}>{user ? <span className="mini-avatar">{user.fullName?.charAt(0)}</span> : <UserRound size={20} />}</Link><LanguageSwitcher language={language} setLanguage={setLanguage} /><button className="icon-button mobile-menu-toggle" aria-label={menuOpen ? "Đóng menu" : "Mở menu"} aria-controls="primary-navigation" aria-expanded={menuOpen} onClick={() => setMenuOpen(!menuOpen)}>{menuOpen ? <X size={21} /> : <Menu size={21} />}</button></div>
    </div><div className="search-row"><ProductSearch en={en} navigate={navigate} /><div className="header-assurances"><span><ShieldCheck size={16} />{en ? "Authentic products" : "Cam kết chính hãng"}</span><span><Truck size={16} />{en ? "Nationwide delivery" : "Giao hàng toàn quốc"}</span></div></div><nav className="category-rail" aria-label={en ? "Store navigation" : "Điều hướng cửa hàng"}><div className="category-rail-inner"><CategoryMenu user={user} en={en} /><nav id="primary-navigation" className={`main-nav${menuOpen ? " is-open" : ""}`} aria-label={en ? "Main navigation" : "Điều hướng chính"}>{nav.map(([to, label]) => <NavLink key={to} to={to} end={to === "/"}>{label}</NavLink>)}</nav></div></nav></header>
    <main id="main-content"><Outlet /></main>
    <footer className="site-footer"><div className="footer-grid"><div><Brand /><p>{en ? "Made for your next game." : "Đồng hành cùng từng đường cầu."}</p><span className="footer-signature"><Sparkles size={14} /> PLAY WITH PURPOSE</span></div><div><h3>{en ? "Explore" : "Khám phá"}</h3><Link to="/products">{en ? "All products" : "Tất cả sản phẩm"}</Link><Link to="/marketplace">Marketplace</Link><Link to="/promotions">{en ? "Offers" : "Ưu đãi"}</Link></div><div><h3>{en ? "Your account" : "Tài khoản"}</h3><Link to="/profile">{en ? "Profile" : "Hồ sơ cá nhân"}</Link><Link to="/orders">{en ? "Orders" : "Đơn hàng"}</Link><Link to="/sell">{en ? "Sell your gear" : "Đăng bán dụng cụ"}</Link></div><div><h3>{en ? "Get in touch" : "Kết nối với DH"}</h3><a href="tel:0865930618">0865 930 618</a><a href="mailto:support@dhbadminton.vn">support@dhbadminton.vn</a><span>{en ? "Vietnam" : "Việt Nam"}</span></div></div><div className="footer-bottom"><span>© 2026 DH Badminton Store</span><span>PLAY WITH PURPOSE.</span></div></footer>
  </div>;
}
