import { useEffect, useRef, useState } from "react";
import { useAuth } from "../context/AuthContext";
import { api, Badge, money } from "./CommerceUI";
import { apiClient, errorMessage } from "../services/apiClient";
import { Check, Copy, CreditCard, Landmark, LoaderCircle, QrCode, RotateCw, Truck, Wallet } from "./icons";

export const paymentNames = { COD: "Thanh toán khi nhận hàng", BANK_TRANSFER: "Chuyển khoản ngân hàng", VIETQR: "VietQR", VNPAY: "VNPAY", ZALOPAY: "ZaloPay", MOMO: "MoMo" };
const paymentIcons = { COD: Truck, BANK_TRANSFER: Landmark, VIETQR: QrCode, VNPAY: CreditCard, ZALOPAY: Wallet, MOMO: Wallet };
export function PaymentMethods({ methods, value, onChange }) {
  return <fieldset className="payment-methods"><legend>Phương thức thanh toán</legend>
    <div className="payment-options">{methods.filter(method => ["COD", "BANK_TRANSFER", "VIETQR"].includes(method.code)).map(method => {
      const Icon = paymentIcons[method.code] || Wallet;
      return <label key={method.code} className={`payment-option ${value === method.code ? "selected" : ""} ${method.available ? "" : "unavailable"}`}>
        <input type="radio" name="paymentMethod" value={method.code} checked={value === method.code} disabled={!method.available} onChange={() => onChange(method.code)} />
        <Icon size={22} aria-hidden="true" /><span><strong>{method.name}</strong>
          {!method.available ? <small>Tạm chưa khả dụng</small> : method.sandbox ? <small>Môi trường thử nghiệm</small> : null}</span>
      </label>;
    })}</div>
  </fieldset>;
}

export default function PaymentPanel({ order, onUpdate }) {
  const { user } = useAuth();
  const [payment, setPayment] = useState(null), [error, setError] = useState(""), [busy, setBusy] = useState(false);
  const [confirm, setConfirm] = useState(null), [copied, setCopied] = useState(""), [qrFailed, setQrFailed] = useState(false);
  const id = order.orderId;
  const bank = ["BANK_TRANSFER", "VIETQR"].includes(order.paymentMethod);
  const admin = user?.role === "ADMIN";
  const onUpdateRef = useRef(onUpdate);
  onUpdateRef.current = onUpdate;
  useEffect(() => {
    let live = true, loading = false, attempted = false;
    setPayment(null); setError(""); setQrFailed(false); setConfirm(null); setBusy(false); setCopied("");
    const load = async () => {
      if (loading) return;
      loading = true;
      try {
      let data = await api(`/orders/${id}/payment`);
      if (!live) return;
      setPayment(data);
      if (!attempted && !admin && order.orderStatus !== "CANCELLED" && order.paymentMethod === "VIETQR" && data.status === "UNPAID" && data.available && !data.expired && !data.qrUrl) {
        attempted = true;
        setBusy(true);
        data = (await apiClient({ url: `/orders/${id}/payment`, method: "post", timeout: 45000 })).data.data;
      }
      if (live) {
        setPayment(data); setError("");
        if (data.status !== order.paymentStatus) onUpdateRef.current();
      }
      } catch (e) { if (live) setError(errorMessage(e)); }
      finally { loading = false; if (live) setBusy(false); }
    };
    load();
    const timer = order.orderStatus !== "CANCELLED" && order.paymentStatus === "UNPAID" && order.paymentMethod !== "COD" ? setInterval(load, 7000) : null;
    return () => { live = false; if (timer) clearInterval(timer); };
  }, [id, order.paymentStatus, order.paymentMethod, order.orderStatus, admin]);
  const status = payment?.status || order.paymentStatus;
  async function refresh() {
    setBusy(true); setError("");
    try { setPayment(await api(`/orders/${id}/payment`)); onUpdate(); }
    catch (e) { setError(errorMessage(e)); } finally { setBusy(false); }
  }
  async function start() {
    setBusy(true); setError("");
    try {
      const result = (await apiClient({ url: `/orders/${id}/payment`, method: "post", timeout: 45000 })).data.data;
      setPayment(result); setQrFailed(false);
      if (result.checkoutUrl) window.location.assign(result.checkoutUrl);
    } catch (e) { setError(errorMessage(e)); } finally { setBusy(false); }
  }
  async function copy(text, label) {
    try { await navigator.clipboard.writeText(text); setCopied(`Đã sao chép ${label.toLowerCase()}.`); }
    catch { setError("Không thể sao chép. Bạn có thể chọn và sao chép thông tin chuyển khoản."); }
  }
  async function reconcile(event) {
    event.preventDefault(); setBusy(true); setError("");
    const fields = Object.fromEntries(new FormData(event.currentTarget));
    try {
      const endpoint = confirm === "refund" ? "refund-confirm" : "confirm";
      const result = await api(`/admin/orders/${id}/payment/${endpoint}`, "post", { amount: Number(fields.amount), reference: fields.reference.trim() });
      setPayment(result); setConfirm(null); onUpdate();
    } catch (e) { setError(errorMessage(e)); } finally { setBusy(false); }
  }
  return <section className="order-payment" aria-labelledby="payment-heading">
    <div className="payment-heading"><h2 id="payment-heading">Thanh toán</h2><Badge value={status} /></div>
    <p className="payment-name"><Wallet size={18} />{paymentNames[order.paymentMethod]} <strong>{money(order.totalAmount)}</strong></p>
    {payment?.sandbox && <p className="notice">Môi trường thử nghiệm, không dùng tiền thật.</p>}
    {order.paymentMethod === "COD" && status === "UNPAID" && <p>Thanh toán khi nhận hàng.</p>}
    {status === "PAID" && <p className="payment-confirmed"><Check size={18} />Thanh toán đã được xác nhận.</p>}
    {status === "REFUND_PENDING" && <p className="notice">Đơn đã hủy và đang chờ hoàn tiền.</p>}
    {status === "FAILED" && <p className="notice error">Giao dịch không thành công. Kiểm tra lịch sử thanh toán trước khi hủy đơn và đặt lại.</p>}
    {payment?.message && <p role="status" className="notice">{payment.message}</p>}
    {error && <p role="alert" className="notice error">{error}</p>}
    {!payment && !error && <p role="status">Đang tải thông tin thanh toán...</p>}
    {bank && payment?.accountNumber && !payment.expired && status === "UNPAID" && order.orderStatus !== "CANCELLED" && <div className="bank-payment">
      <div className="payment-qr">{qrFailed || !payment.qrUrl ? <p>Chưa tải được mã QR. Dùng thông tin chuyển khoản bên cạnh.</p> :
        <img src={payment.qrUrl} alt="Mã VietQR thanh toán đơn hàng" referrerPolicy="no-referrer" onError={() => setQrFailed(true)} />}</div>
      <div><dl className="bank-details">{[
        ["Ngân hàng", payment.bankName], ["Số tài khoản", payment.accountNumber], ["Chủ tài khoản", payment.accountName],
        ["Số tiền", String(payment.amount)], ["Nội dung", payment.transferContent],
      ].map(([label, value]) => <div key={label}><dt>{label}</dt><dd><span translate="no">{label === "Số tiền" ? money(value) : value}</span>
        <button type="button" className="icon-button" aria-label={`Sao chép ${label.toLowerCase()}`} title={`Sao chép ${label.toLowerCase()}`} onClick={() => copy(value, label)}><Copy size={16} /></button></dd></div>)}</dl>
      <p className="muted">Chuyển đúng số tiền và nội dung. Shop xác nhận sau khi tiền vào tài khoản.</p><p role="status">{copied}</p></div>
    </div>}
    <div className="payment-actions">
      {!admin && bank && (order.paymentMethod !== "VIETQR" || !payment?.qrUrl) && status === "UNPAID" && order.orderStatus !== "CANCELLED" && !payment?.expired &&
        <button type="button" className="primary" disabled={busy || !payment?.available} onClick={start}>
          {busy ? <LoaderCircle className="spin" size={17} /> : bank ? <QrCode size={17} /> : <CreditCard size={17} />}
          {bank ? "Thông tin chuyển khoản / QR" : `Thanh toán với ${paymentNames[order.paymentMethod]}`}
        </button>}
      <button type="button" disabled={busy} onClick={refresh}><RotateCw size={16} />Cập nhật trạng thái</button>
      {admin && bank && ["UNPAID", "FAILED"].includes(status) && <button type="button" disabled={busy} onClick={() => setConfirm("received")}><Check size={16} />Đối soát tiền vào</button>}
      {admin && status === "REFUND_PENDING" && <button type="button" disabled={busy} onClick={() => setConfirm("refund")}><Check size={16} />Ghi nhận hoàn tiền</button>}
    </div>
    {payment && !payment.available && status === "UNPAID" && order.paymentMethod !== "COD" && <p className="muted">Phương thức này tạm chưa khả dụng. Liên hệ shop để kiểm tra thanh toán.</p>}
    {confirm && <form className="payment-reconcile" onSubmit={reconcile}>
      <h3>{confirm === "refund" ? "Xác nhận đã hoàn tiền thực tế" : "Xác nhận tiền đã vào tài khoản"}</h3>
      <label className="field"><span>Số tiền đối soát (VND)</span><input name="amount" type="number" min="1" step="0.01" defaultValue={order.totalAmount} required /></label>
      <label className="field"><span>Mã giao dịch trên sao kê</span><input name="reference" autoComplete="off" minLength={3} maxLength={100} required /></label>
      <label className="reconcile-check"><input type="checkbox" required />{confirm === "refund" ? "Đã hoàn tiền và đối chiếu giao dịch" : "Đã đối chiếu tiền nhận trên sao kê ngân hàng"}</label>
      <div className="payment-actions"><button type="submit" className="primary" disabled={busy}><Check size={16} />Xác nhận</button><button type="button" disabled={busy} onClick={() => setConfirm(null)}>Hủy</button></div>
    </form>}
  </section>;
}
