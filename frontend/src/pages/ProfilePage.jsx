import { useEffect, useState } from "react";
import { Link, NavLink, useNavigate } from "react-router-dom";
import Alert from "@mui/material/Alert";
import Button from "@mui/material/Button";
import CircularProgress from "@mui/material/CircularProgress";
import Chip from "@mui/material/Chip";
import { useForm } from "react-hook-form";
import { useAuth } from "../context/AuthContext";
import { userApi } from "../services/userApi";
import { errorMessage, applyServerErrors } from "../services/apiClient";
import FormField from "../components/FormField";
import { ArrowUpRight, ClipboardList, Heart, House, LogOut, Mail, MapPin, Pencil, Save, ScrollText, Sparkles, Target, UserRound } from "../components/icons";
import { api, Form, State, useData, formatDate } from "../components/CommerceUI";
import { playerFields } from "./RecommendationPages";
import { useChatbot } from "../context/ChatbotContext";

const defaults = { skillLevel: "INTERMEDIATE", playStyle: "ATTACK", playType: "BOTH", wristStrength: "MEDIUM", dominantHand: "RIGHT", preferredWeight: "4U", balanceType: "EVEN_BALANCE", shaftFlexibility: "MEDIUM", shirtSize: "L", pantsSize: "L", shoeSize: 42, favoriteColors: "", favoriteBrands: "", minBudget: 500000, maxBudget: 5000000 };
const links = [["/", "Trang chủ", House], ["/profile", "Hồ sơ cá nhân", UserRound], ["/player-profile", "Hồ sơ người chơi", Target], ["/orders", "Đơn hàng của tôi", ClipboardList], ["/favorites", "Sản phẩm yêu thích", Heart], ["/addresses", "Địa chỉ giao hàng", MapPin], ["/my-listings", "Tin đăng của tôi", ScrollText]];

const toIsoDate = (value) => {
  const match = String(value || "").match(/^(\d{2})\/(\d{2})\/(\d{4})$/);
  return match ? `${match[3]}-${match[2]}-${match[1]}` : value;
};
export default function ProfilePage() {
  const { user, setUser, logout } = useAuth();
  const { openChatbot } = useChatbot();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true), [loadError, setLoadError] = useState(""), [message, setMessage] = useState(null);
  const player = useData("/player-profile");
  const { register, handleSubmit, reset, setError, formState: { errors, isSubmitting } } = useForm();
  async function load() {
    setLoading(true); setLoadError("");
    try { const data = await userApi.me(); setUser(data); reset({ fullName: data.fullName, phone: data.phone || "", avatarUrl: data.avatarUrl || "", dateOfBirth: formatDate(data.dateOfBirth), gender: data.gender || "", heightCm: data.heightCm || "", weightKg: data.weightKg || "" }); }
    catch (e) { setLoadError(errorMessage(e)); }
    finally { setLoading(false); }
  }
  useEffect(() => { load(); }, []);
  async function submit(data) {
    setMessage(null);
    try { const result = await userApi.update({ ...data, dateOfBirth: toIsoDate(data.dateOfBirth) }); setUser(result); setMessage({ type: "success", text: "Đã cập nhật hồ sơ." }); }
    catch (e) { setMessage({ type: "error", text: errorMessage(e) }); applyServerErrors(e, setError); }
  }
  async function signOut() { try { await logout(); } catch { /* Local session is cleared by the provider. */ } navigate("/login"); }
  function savePlayer(values) { return api("/player-profile", player.data ? "put" : "post", Object.fromEntries(playerFields.map(f => [f.name, values[f.name]]))); }

  return <div className="profile-layout">
    <aside className="profile-sidebar"><nav aria-label="Điều hướng tài khoản">{links.map(([to, label, Icon]) => <NavLink key={to} end to={to} className={({ isActive }) => `profile-side-link${isActive ? " active" : ""}`}><Icon size={17} />{label}</NavLink>)}<button className="profile-side-link" onClick={signOut}><LogOut size={17} />Đăng xuất</button></nav></aside>
    <div className="profile-workspace"><div className="profile-breadcrumb"><Link to="/">Trang chủ</Link> / Tài khoản / <b>Hồ sơ cá nhân</b></div>
      <section className="profile-banner"><div className="avatar-letter">{user?.fullName?.charAt(0)?.toUpperCase() || "DH"}</div><div className="profile-banner-copy"><h1>Hồ sơ cá nhân</h1><b>{user?.fullName}</b><Chip size="small" label="Thành viên" color="primary" /><p><Mail size={13} /> {user?.email || user?.phone}</p></div><a href="#personal-info" className="profile-edit-link"><Pencil size={13} /> Chỉnh sửa hồ sơ</a></section>
      {loading ? <div className="profile-loading"><CircularProgress aria-label="Đang tải hồ sơ" /></div> : loadError ? <Alert severity="error" action={<Button onClick={load}>Thử lại</Button>}>{loadError}</Alert> : <div className="profile-columns"><div className="profile-forms">
        <section className="profile-panel" id="personal-info"><div className="profile-panel-title"><h2><UserRound size={19} />Thông tin cá nhân</h2></div>{message && <Alert severity={message.type}>{message.text}</Alert>}<form className="profile-fields" onSubmit={handleSubmit(submit)}>
          <FormField name="fullName" label="Họ và tên" autoComplete="name" register={register} errors={errors} rules={{ required: "Nhập họ và tên", maxLength: { value: 100, message: "Tối đa 100 ký tự" } }} />
          <FormField name="dateOfBirth" label="Ngày sinh" type="text" placeholder="dd/mm/yyyy" inputProps={{ inputMode: "numeric", pattern: "\\d{2}/\\d{2}/\\d{4}" }} register={register} errors={errors} />
          <label className="profile-readonly"><span>Giới tính</span><select {...register("gender")}><option value="">Chưa cập nhật</option><option value="MALE">Nam</option><option value="FEMALE">Nữ</option><option value="OTHER">Khác</option></select></label>
          <FormField name="phone" label="Số điện thoại" autoComplete="tel" register={register} errors={errors} rules={{ maxLength: { value: 20, message: "Tối đa 20 ký tự" } }} />
          <FormField name="heightCm" label="Chiều cao (cm)" type="number" inputProps={{ min: 80, max: 260 }} register={register} errors={errors} />
          <FormField name="weightKg" label="Cân nặng (kg)" type="number" inputProps={{ min: 20, max: 300, step: "0.1" }} register={register} errors={errors} />
          <input type="hidden" {...register("avatarUrl")} /><Button variant="contained" type="submit" startIcon={<Save size={16} />} disabled={isSubmitting}>{isSubmitting ? "Đang lưu…" : "Lưu thay đổi"}</Button>
        </form></section>
        <section className="profile-panel"><div className="profile-panel-title"><h2><Target size={19} />Thông số & sở thích</h2><button type="button" onClick={() => openChatbot("profile")}>Gợi ý từ hồ sơ<ArrowUpRight size={13} /></button></div>{player.loading ? <State query={player} /> : player.error && player.errorCode !== "PROFILE_REQUIRED" ? <State query={player} /> : <Form fields={playerFields} initial={Object.fromEntries(playerFields.map(f => [f.name, player.data?.[f.name] ?? defaults[f.name] ?? ""]))} submit="Lưu thông số & sở thích" onSubmit={savePlayer} onDone={player.reload} />}</section>
        <section className="profile-panel"><div className="profile-panel-title"><h2><MapPin size={19} />Địa chỉ giao hàng</h2></div><Button startIcon={<MapPin size={16} />} component={Link} to="/addresses" variant="outlined">Mở sổ địa chỉ</Button></section>
      </div><aside className="profile-recommend"><section className="profile-ai"><h3><Sparkles size={19} />DH Chatbot</h3><p>Vợt phù hợp với trình độ, lối chơi, thể lực và ngân sách trong hồ sơ của bạn.</p><Button startIcon={<Target size={16} />} onClick={() => openChatbot("profile")} variant="contained">Gợi ý từ hồ sơ</Button><Button endIcon={<ArrowUpRight size={16} />} onClick={openChatbot} variant="text">Tư vấn chọn dụng cụ</Button></section></aside></div>}
    </div>
  </div>;
}
