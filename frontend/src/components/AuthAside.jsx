import { ArrowUpRight, Sparkles } from "./icons";
import { Link } from "react-router-dom";

export default function AuthAside({ registering = false }) {
  return <aside className="auth-aside"><img src={`${import.meta.env.BASE_URL}images/badminton-player-pexels.jpg`} alt="Vận động viên cầu lông trên sân" /><div className="auth-aside-copy"><span className="eyebrow">YOUR GAME. YOUR MATCH.</span><h2>{registering ? <>Sẵn sàng cho<br />trận tiếp theo.</> : <>Trở lại sân.<br />Tiếp nối đam mê.</>}</h2><Link to="/products">DH BADMINTON STORE<ArrowUpRight size={18} /></Link></div><span className="auth-match-mark"><Sparkles size={16} /> MADE FOR YOUR GAME</span></aside>;
}
