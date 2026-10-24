import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { useChatbot } from "../context/ChatbotContext";
import { useAuth } from "../context/AuthContext";
import { apiClient, errorMessage } from "../services/apiClient";
import { media, money } from "./CommerceUI";
import { ArrowUpRight, Bot, LoaderCircle, MessageCircle, Package, RotateCw, Send, SlidersHorizontal, Target, X } from "./icons";
import "./ChatbotWidget.css";

function isProfileRequest(value) {
  const text = value.toLowerCase().normalize("NFD").replace(/\p{M}/gu, "").replace(/đ/g, "d");
  const mentionsProfileAdvice = /(?:goi y|tu van|de xuat|recommend|suggest|advice).*(?:ho so|profile)|(?:ho so|profile).*(?:goi y|tu van|de xuat|recommend|suggest|advice)/.test(text);
  // Preserve custom category, budget, and preference details for the catalog flow.
  const hasCustomCriteria = /\d|\b(?:vot|giay|ao|quan|tui|balo|cuoc|qua cau|phu kien|racket|shoe|shoes|bag|shirt|budget|under|below|above|color|colour|size|mau|kich co|ngan sach|duoi|4u|3u|5u|6u|brand|hang|uu tien|them|ngoai tru|khong muon)\b/.test(text);
  return mentionsProfileAdvice && !hasCustomCriteria;
}

function ProductImage({ url }) {
  const [failed, setFailed] = useState(false);
  useEffect(() => setFailed(false), [url]);
  return <span className="chatbot-product-image">{url && !failed ? <img src={media(url)} alt="" loading="lazy" onError={() => setFailed(true)} /> : <Package size={22} />}</span>;
}

export default function ChatbotWidget() {
  const { open, profileRequest, openChatbot, closeChatbot } = useChatbot();
  const { user } = useAuth();
  const [language, setLanguage] = useState(() => document.documentElement.lang === "en" ? "en" : "vi");
  const [messages, setMessages] = useState([]);
  const [text, setText] = useState("");
  const [busy, setBusy] = useState(false);
  const request = useRef(null), history = useRef([]), contextToken = useRef(null), sequence = useRef(0);
  const handledProfileRequest = useRef(0), previousUser = useRef(user?.id), lastWasProfile = useRef(false);
  const input = useRef(null), log = useRef(null);
  const en = language === "en";

  useEffect(() => {
    const observer = new MutationObserver(() => setLanguage(document.documentElement.lang === "en" ? "en" : "vi"));
    observer.observe(document.documentElement, { attributes: true, attributeFilter: ["lang"] });
    return () => observer.disconnect();
  }, []);
  useEffect(() => {
    const accountChanged = previousUser.current !== user?.id;
    const expiredProfile = previousUser.current != null && !user && lastWasProfile.current;
    previousUser.current = user?.id;
    request.current?.abort();
    request.current = null;
    history.current = [];
    contextToken.current = null;
    if (accountChanged) {
      handledProfileRequest.current = profileRequest;
      lastWasProfile.current = false;
    }
    setMessages(expiredProfile ? [{ id: ++sequence.current, role: "assistant", text: en ? "Please sign in to get recommendations from your profile." : "Bạn cần đăng nhập để nhận gợi ý từ hồ sơ của mình.", action: "login" }] : []);
    setText(""); setBusy(false);
  }, [user?.id]);
  useEffect(() => () => request.current?.abort(), []);
  useEffect(() => {
    if (!open) return;
    input.current?.focus();
    const escape = event => { if (event.key === "Escape") { event.preventDefault(); closeChatbot(); } };
    document.addEventListener("keydown", escape);
    return () => document.removeEventListener("keydown", escape);
  }, [open, closeChatbot]);
  useEffect(() => {
    if (log.current) log.current.scrollTop = log.current.scrollHeight;
  }, [messages, open]);
  useEffect(() => {
    if (!open || busy || request.current || profileRequest === handledProfileRequest.current) return;
    handledProfileRequest.current = profileRequest;
    send(en ? "Recommendations from my profile" : "Gợi ý từ hồ sơ", undefined, "profile");
  }, [open, profileRequest, busy]);

  function reset() {
    request.current?.abort(); request.current = null;
    history.current = [];
    contextToken.current = null;
    handledProfileRequest.current = profileRequest;
    lastWasProfile.current = false;
    setMessages([]); setText(""); setBusy(false);
    input.current?.focus();
  }

  async function send(value, retryId, source) {
    value = value.trim();
    if (!value || value.length > 1000 || request.current) return;
    const controller = new AbortController();
    request.current = controller;
    const replyId = retryId ?? ++sequence.current;
    const fromProfile = source === "profile" || isProfileRequest(value);
    lastWasProfile.current = fromProfile;
    setBusy(true); setText("");
    if (retryId) setMessages(items => items.map(item => item.id === retryId ? { ...item, pending: true, error: false, text: "" } : item));
    else setMessages(items => [...items, { id: ++sequence.current, role: "user", text: value }, { id: replyId, role: "assistant", pending: true, prompt: value, source: fromProfile ? "profile" : "chat" }]);
    const complete = result => setMessages(items => items.map(item => item.id === replyId ? { ...item, pending: false, ...result } : item));
    try {
      if (fromProfile) {
        if (!user) {
          complete({ text: en ? "Please sign in to get recommendations from your profile." : "Bạn cần đăng nhập để nhận gợi ý từ hồ sơ của mình.", action: "login" });
          return;
        }
        const response = await apiClient({ url: "/recommendations", method: "get", params: { limit: 3 }, signal: controller.signal });
        if (controller.signal.aborted) return;
        const recommendations = response.data.data;
        if (!Array.isArray(recommendations) || recommendations.some(result =>
          !result?.product || typeof result.product.productName !== "string" ||
          (result.matchScore != null && !Number.isFinite(result.matchScore)) ||
          (result.reasons != null && !Array.isArray(result.reasons)) ||
          (result.cautions != null && !Array.isArray(result.cautions))
        )) throw new Error("Invalid recommendation response");
        complete({ recommendations, text: recommendations.length
          ? (en ? "These rackets are recommended for your saved playing profile." : "Đây là những cây vợt được gợi ý theo hồ sơ người chơi đã lưu của bạn.")
          : (en ? "There are no available rackets to recommend from your profile yet." : "Hiện chưa có vợt còn hàng để gợi ý từ hồ sơ của bạn."), action: "profile" });
        return;
      }
      const response = await apiClient({ url: "/ai/chat", method: "post", data: { message: value, history: history.current, language, ...(contextToken.current ? { contextToken: contextToken.current } : {}) }, signal: controller.signal, withCredentials: true, timeout: 45000 });
      const result = response.data.data;
      if (controller.signal.aborted) return;
      if (!result || typeof result.answer !== "string" || !Array.isArray(result.products)) throw new Error("Invalid chat response");
      contextToken.current = result.engine === "PHOBERT_V1" ? (result.contextToken ?? null) : null;
      history.current = [...history.current, value].slice(-10);
      complete({ text: result.answer, products: result.products, error: result.resultKind === "UNAVAILABLE" });
    } catch (error) {
      if (controller.signal.aborted) return;
      if (fromProfile && error.response?.data?.code === "PROFILE_REQUIRED") {
        complete({ text: en ? "You have not saved a player profile yet." : "Bạn chưa có hồ sơ người chơi để nhận gợi ý cá nhân hóa.", action: "profile" });
        return;
      }
      if (fromProfile && error.response?.status === 401) {
        complete({ text: en ? "Please sign in to get recommendations from your profile." : "Bạn cần đăng nhập để nhận gợi ý từ hồ sơ của mình.", action: "login" });
        return;
      }
      if (fromProfile && error.response?.status === 403) {
        complete({ text: en ? "Profile recommendations are available to player accounts. You can still ask me about products." : "Gợi ý từ hồ sơ dành cho tài khoản người chơi. Bạn vẫn có thể hỏi DH Chatbot về sản phẩm." });
        return;
      }
      const message = en ? (error.response?.status === 429 ? "Too many messages. Please try again in a minute." : "Cannot reach the assistant right now. Please try again.") : errorMessage(error);
      complete({ error: true, text: message });
    } finally {
      if (request.current === controller) { request.current = null; setBusy(false); }
    }
  }

  const examples = en ? ["Racket under 1 million", "Shoes under 2 million", "Beginner racket 4U"] : ["Vợt dưới 1 triệu", "Giày dưới 2 triệu", "Vợt 4U cho người mới chơi"];
  return <div className="chatbot-root" translate="no">
    {open && <section id="chatbot-panel" className="chatbot-panel" role="dialog" aria-labelledby="chatbot-title" aria-modal="false">
      <header className="chatbot-header"><span className="chatbot-avatar"><Bot size={23} /></span><div><h2 id="chatbot-title">DH Chatbot</h2><p>{en ? "Local badminton assistant" : "Trợ lý cầu lông cục bộ"}</p></div><button type="button" className="chatbot-icon" onClick={reset} title={en ? "New conversation" : "Cuộc trò chuyện mới"} aria-label={en ? "New conversation" : "Cuộc trò chuyện mới"}><RotateCw size={18} /></button><button type="button" className="chatbot-icon" onClick={closeChatbot} title={en ? "Close DH Chatbot" : "Đóng DH Chatbot"} aria-label={en ? "Close DH Chatbot" : "Đóng DH Chatbot"}><X size={20} /></button></header>
      <div className="chatbot-profile-tools"><button type="button" disabled={busy} onClick={() => send(en ? "Recommendations from my profile" : "Gợi ý từ hồ sơ", undefined, "profile")}><Target size={16} />{en ? "From my profile" : "Gợi ý từ hồ sơ"}</button><Link className="chatbot-icon" to="/player-profile" onClick={closeChatbot} title={en ? "Edit player profile" : "Chỉnh sửa hồ sơ người chơi"} aria-label={en ? "Edit player profile" : "Chỉnh sửa hồ sơ người chơi"}><SlidersHorizontal size={17} /></Link></div>
      <div ref={log} className="chatbot-log" role="log" aria-live="polite" aria-relevant="additions text" aria-label={en ? "Conversation" : "Cuộc trò chuyện"}>
        <div className="chatbot-message assistant"><span className="chatbot-speaker">DH Chatbot</span><p>{en ? "Hello! What gear are you looking for for your next game?" : "Chào bạn! Bạn đang tìm dụng cụ nào cho trận cầu sắp tới?"}</p></div>
        {messages.length === 0 && <div className="chatbot-suggestions">{examples.map(example => <button key={example} type="button" onClick={() => send(example)}><MessageCircle size={14} />{example}<ArrowUpRight size={14} /></button>)}</div>}
        {messages.map((item, index) => <div key={item.id} className={`chatbot-message ${item.role}${item.error ? " failed" : ""}`}>
          <span className="chatbot-speaker">{item.role === "user" ? (en ? "You" : "Bạn") : "DH Chatbot"}</span>
          {item.pending ? <p className="chatbot-pending" role="status"><LoaderCircle size={16} className="spin" />{en ? "Thinking..." : "Đang trả lời..."}</p> : <p role={item.error ? "alert" : undefined}>{item.text}</p>}
          {item.error && index === messages.length - 1 && <button className="chatbot-retry" type="button" disabled={busy} onClick={() => send(item.prompt, item.id, item.source)}><RotateCw size={14} />{en ? "Try again" : "Thử lại"}</button>}
          {item.products?.length > 0 && <div className="chatbot-products">{item.products.map(product => <Link key={product.id} className="chatbot-product" to={`/products/${product.id}`} onClick={closeChatbot}>
            <ProductImage url={product.thumbnailUrl} /><span className="chatbot-product-copy"><strong>{product.productName}</strong><b>{money(product.effectivePrice)}</b><small>{product.stock == null ? (en ? "Stock unconfirmed" : "Chưa xác minh tồn kho") : product.stock > 0 ? (en ? "In stock" : "Còn hàng") : (en ? "Out of stock" : "Hết hàng")}</small></span><ArrowUpRight size={15} />
          </Link>)}</div>}
          {item.recommendations?.length > 0 && <div className="chatbot-profile-results">{item.recommendations.map(result => <article key={result.productId}>
            <Link className="chatbot-product" to={`/products/${result.productId}`} onClick={closeChatbot}><ProductImage url={result.product.thumbnailUrl} /><span className="chatbot-product-copy"><strong>{result.product.productName}</strong><b>{money(result.product.effectivePrice)}</b>{Number.isFinite(result.matchScore) ? <span className="chatbot-match-score"><Target size={12} />{result.matchScore}% {en ? "match" : "phù hợp"}</span> : <span className="chatbot-match-score">{en ? "Specs incomplete" : "Chưa đủ thông số để chấm"}</span>}</span><ArrowUpRight size={15} /></Link>
            {result.reasons?.length > 0 && <ul className="chatbot-reasons">{result.reasons.map((reason, i) => <li key={i}>{reason}</li>)}</ul>}
            {result.cautions?.map((caution, i) => <p className="chatbot-caution" key={i}>{caution}</p>)}
          </article>)}</div>}
          {item.action && <Link className="chatbot-message-action" to={item.action === "login" ? "/login" : "/player-profile"} onClick={closeChatbot}>{item.action === "login" ? (en ? "Sign in" : "Đăng nhập") : (en ? "Update player profile" : "Cập nhật hồ sơ người chơi")}<ArrowUpRight size={14} /></Link>}
        </div>)}
      </div>
      <form className="chatbot-composer" onSubmit={event => { event.preventDefault(); send(text); }}>
        <div><textarea ref={input} rows={2} value={text} maxLength={1000} aria-label={en ? "Enter a message" : "Nhập tin nhắn"} placeholder={en ? "What are you looking for?" : "Bạn đang tìm sản phẩm nào?"} onChange={event => setText(event.target.value)} onKeyDown={event => { if (event.key === "Enter" && !event.shiftKey && !event.nativeEvent.isComposing) { event.preventDefault(); send(text); } }} /><span className="chatbot-count">{text.length}/1000</span></div>
        <button type="submit" className="chatbot-send" disabled={busy || !text.trim()} title={en ? "Send message" : "Gửi tin nhắn"} aria-label={en ? "Send message" : "Gửi tin nhắn"}><Send size={19} /></button>
      </form>
    </section>}
    <button type="button" className={`chatbot-launcher${open ? " is-open" : ""}`} onClick={open ? closeChatbot : openChatbot} aria-label={open ? (en ? "Minimize DH Chatbot" : "Thu gọn DH Chatbot") : (en ? "Open DH Chatbot" : "Mở DH Chatbot")} title={open ? (en ? "Minimize DH Chatbot" : "Thu gọn DH Chatbot") : (en ? "Open DH Chatbot" : "Mở DH Chatbot")} aria-expanded={open} aria-controls="chatbot-panel">{open ? <X size={23} /> : <MessageCircle size={25} />}<span>DH Chatbot</span></button>
  </div>;
}
