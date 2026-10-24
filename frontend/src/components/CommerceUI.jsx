import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { ArrowLeft, ArrowRight, CircleAlert, Inbox, LoaderCircle, Package, RotateCw, Save, Sparkles, UploadCloud } from "./icons";
import { apiClient, errorMessage } from "../services/apiClient";
export async function api(path, method = "get", data) {
  return (await apiClient({ url: path, method, data })).data.data;
}
export const money = (v) =>
  new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(
    v ?? 0,
  );
export const formatDate = (value) => {
  if (!value) return "";
  const iso = String(value).match(/^(\d{4})-(\d{2})-(\d{2})/);
  if (iso) return `${iso[3]}/${iso[2]}/${iso[1]}`;
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? "" : new Intl.DateTimeFormat("vi-VN", { timeZone: "Asia/Ho_Chi_Minh" }).format(date);
};
const parseDate = (value) => {
  const match = String(value).trim().match(/^(\d{2})\/(\d{2})\/(\d{4})$/);
  if (!match) return "";
  const [, day, month, year] = match;
  const date = new Date(Date.UTC(Number(year), Number(month) - 1, Number(day)));
  return date.getUTCFullYear() === Number(year) && date.getUTCMonth() === Number(month) - 1 && date.getUTCDate() === Number(day)
    ? `${year}-${month}-${day}` : "";
};
export function DateInput({ name, value, onChange, required = true, ...rest }) {
  const [text, setText] = useState(() => formatDate(value));
  useEffect(() => { setText(formatDate(value)); }, [value]);
  return <input name={name} type="text" inputMode="numeric" placeholder="dd/mm/yyyy" pattern="\d{2}/\d{2}/\d{4}" value={text} required={required}
    onChange={(event) => {
      const next = event.target.value;
      setText(next);
      const iso = parseDate(next);
      if (iso || next === "") onChange({ target: { name, value: iso } });
    }} {...rest} />;
}
export const media = (url) =>
  !url
    ? ""
    : url.startsWith("/media/")
      ? new URL(url, new URL(apiClient.defaults.baseURL, window.location.origin)).href
      : url;
export function useData(path) {
  const [data, setData] = useState(null),
    [error, setError] = useState(""),
    [errorCode, setErrorCode] = useState(null),
    [loading, setLoading] = useState(true),
    [version, setVersion] = useState(0);
  useEffect(() => {
    let live = true;
    setLoading(true);
    setData(null);
    setError("");
    setErrorCode(null);
    api(path)
      .then((d) => {
        if (live) setData(d);
      })
      .catch((e) => {
        if (live) {
          setError(errorMessage(e));
          setErrorCode(e.response?.data?.code || "NETWORK_ERROR");
        }
      })
      .finally(() => {
        if (live) setLoading(false);
      });
    return () => {
      live = false;
    };
  }, [path, version]);
  return {
    data,
    error,
    errorCode,
    loading,
    reload: () => setVersion((v) => v + 1),
  };
}
export function State({ query, children, empty = false, emptyTitle = "Chưa có dữ liệu", emptyMessage = "Nội dung sẽ xuất hiện tại đây khi có cập nhật mới." }) {
  if (query.loading)
    return (
      <div role="status" className="loading-state"><LoaderCircle className="spin" size={24} /><span>Đang tải…</span></div>
    );
  if (query.error)
    return (
      <div role="alert" className="notice error query-error">
        <CircleAlert size={20} /><span>{query.error}</span>
        <button onClick={query.reload}><RotateCw size={15} />Thử lại</button>
      </div>
    );
  if (empty) return <div className="empty-state"><span className="empty-symbol"><Inbox size={28} /></span><h2>{emptyTitle}</h2><p>{emptyMessage}</p></div>;
  return children;
}
export function Page({ title, subtitle, children, action }) {
  return (
    <div className="page-container commerce">
      <div className="page-title">
        <div>
          <p className="eyebrow"><span className="eyebrow-dot" /> DH BADMINTON / WORKSPACE</p>
          <h1>{title}</h1>
          {subtitle && <p className="muted">{subtitle}</p>}
        </div>
        {action}
      </div>
      {children}
    </div>
  );
}
export function Action({ children, onClick, onDone, danger = false, disabled = false, ...buttonProps }) {
  const [busy, setBusy] = useState(false),
    [error, setError] = useState("");
  return (
    <span className="action">
      <button
        className={danger ? "danger" : ""}
        {...buttonProps}
        disabled={busy || disabled}
        onClick={async () => {
          setBusy(true);
          setError("");
          try {
            await onClick();
            onDone?.();
          } catch (e) {
            setError(errorMessage(e));
          } finally {
            setBusy(false);
          }
        }}
      >
        {busy ? "Đang xử lý…" : children}
      </button>
      {error && (
        <span role="alert" className="inline-error">
          {error}
        </span>
      )}
    </span>
  );
}
export function Field({
  name,
  label,
  value,
  onChange,
  options,
  type = "text",
  required = true,
  ...rest
}) {
  const binary = options?.length === 2 && options.every(option => typeof option === "object" && ["true", "false"].includes(option.value));
  if (binary) return <label className="field field-toggle"><span>{label}</span><span className="toggle-control"><input type="checkbox" role="switch" name={name} checked={value === true || value === "true"} onChange={event => onChange({ target: { name, value: event.target.checked ? "true" : "false" } })} {...rest} /><span className="toggle-track" aria-hidden="true" /><span className="toggle-value">{value === true || value === "true" ? "Có" : "Không"}</span></span></label>;
  return (
    <label className="field">
      <span>{label}</span>
      {options ? (
        <select
          name={name}
          value={value ?? ""}
          onChange={onChange}
          required={required}
          {...rest}
        >
          {options.map((o) =>
            typeof o === "string" ? (
              <option key={o} value={o}>
                {labelOf(o)}
              </option>
            ) : (
              <option key={o.value} value={o.value}>
                {o.label}
              </option>
            ),
          )}
        </select>
      ) : type === "textarea" ? (
        <textarea
          name={name}
          value={value ?? ""}
          onChange={onChange}
          required={required}
          {...rest}
        />
      ) : type === "date" ? (
        <DateInput name={name} value={value} onChange={onChange} required={required} {...rest} />
      ) : (
        <input
          name={name}
          value={value ?? ""}
          type={type}
          onChange={onChange}
          required={required}
          {...rest}
        />
      )}
    </label>
  );
}
export function Form({
  fields,
  initial = {},
  onSubmit,
  submit = "Lưu",
  onDone,
}) {
  const [values, setValues] = useState(initial),
    [busy, setBusy] = useState(false),
    [error, setError] = useState(""),
    [done, setDone] = useState(false);
  return (
    <form
      className="data-form"
      onSubmit={async (e) => {
        e.preventDefault();
        setBusy(true);
        setError("");
        setDone(false);
        try {
          const r = await onSubmit(values);
          setDone(true);
          onDone?.(r);
        } catch (err) {
          setError(errorMessage(err));
        } finally {
          setBusy(false);
        }
      }}
    >
      <div className="form-grid">
        {fields.map((f) => (
          <Field
            key={f.name}
            {...f}
            value={values[f.name] ?? ""}
            onChange={(e) =>
              setValues((v) => ({
                ...v,
                [f.name]:
                  f.type === "number"
                    ? e.target.value === ""
                      ? ""
                      : Number(e.target.value)
                    : e.target.value,
              }))
            }
          />
        ))}
      </div>
      {error && (
        <p role="alert" className="notice error">
          {error}
        </p>
      )}
      {done && (
        <p role="status" className="notice">
          Đã lưu thành công.
        </p>
      )}
      <button className="primary" disabled={busy}>
        {busy ? <LoaderCircle className="spin" size={16} /> : <Save size={16} />}{busy ? "Đang xử lý…" : submit}
      </button>
    </form>
  );
}
export function Upload({ path, onDone, extra = {} }) {
  const [file, setFile] = useState(null);
  return (
    <div className="upload">
      <label>
        Ảnh PNG/JPEG, tối đa 5 MB{" "}
        <input
          type="file"
          accept="image/png,image/jpeg"
          onChange={(e) => setFile(e.target.files[0])}
        />
      </label>
      {file && (
        <Action
          onClick={async () => {
            const data = new FormData();
            data.append("file", file);
            Object.entries(extra).forEach(([k, v]) => data.append(k, v));
            await api(path, "post", data);
          }}
          onDone={onDone}
        >
          <UploadCloud size={16} />Tải ảnh lên
        </Action>
      )}
    </div>
  );
}
export const labels = {
  PENDING: "Chờ xác nhận",
  CONFIRMED: "Đã xác nhận",
  PREPARING: "Đang chuẩn bị",
  SHIPPING: "Đang vận chuyển",
  COMPLETED: "Hoàn thành",
  CANCELLED: "Đã hủy",
  PENDING_PAYMENT: "Chờ thanh toán",
  WAITING_SHIPMENT: "Chờ gửi hàng",
  DELIVERED: "Đã giao hàng",
  DISPUTED: "Đang tranh chấp",
  REFUNDED: "Đã hoàn tiền",
  REFUND_PENDING: "Chờ hoàn tiền",
  FAILED: "Thanh toán thất bại",
  DRAFT: "Bản nháp",
  PENDING_REVIEW: "Chờ duyệt",
  AVAILABLE: "Đang bán",
  RESERVED: "Đã được giữ",
  SOLD: "Đã bán",
  REJECTED: "Bị từ chối",
  HIDDEN: "Đã ẩn",
  ACTIVE: "Đang hoạt động",
  OUT_OF_STOCK: "Hết hàng",
  INACTIVE: "Ngừng kinh doanh",
  BEGINNER: "Mới chơi",
  INTERMEDIATE: "Trung bình",
  ADVANCED: "Nâng cao",
  ATTACK: "Tấn công",
  DEFENSE: "Phòng thủ",
  BALANCED: "Toàn diện",
  SPEED: "Tốc độ",
  CONTROL: "Kiểm soát",
  WEAK: "Yếu",
  MEDIUM: "Trung bình",
  STRONG: "Khỏe",
  SINGLES: "Đánh đơn",
  DOUBLES: "Đánh đôi",
  BOTH: "Cả hai",
  FLEXIBLE: "Dẻo",
  STIFF: "Cứng",
  EXTRA_STIFF: "Rất cứng",
  HEAD_HEAVY: "Nặng đầu",
  HEAD_LIGHT: "Nhẹ đầu",
  EVEN_BALANCE: "Cân bằng",
  WAITING_PAYMENT: "Chờ thanh toán",
  HELD: "Đang giữ tiền",
  RELEASED: "Đã giải ngân",
  REQUESTED: "Chờ tiếp nhận",
  ACCEPTED: "Đã tiếp nhận",
  INSPECTING: "Đang kiểm định",
  OPEN: "Đang mở",
  RESOLVED: "Đã giải quyết",
  PAID: "Đã thanh toán",
  UNPAID: "Chưa thanh toán",
  RIGHT: "Tay phải",
  LEFT: "Tay trái",
};
export const labelOf = (v) => labels[v] || v;
export function Badge({ value }) {
  return <span className={`badge badge-${String(value || "").toLowerCase()}`}>{labelOf(value)}</span>;
}
export function Pager({ query, page, setPage }) {
  return query.data?.totalPages > 1 ? (
    <div className="pager">
      <button disabled={page === 0} onClick={() => setPage(page - 1)}>
        <ArrowLeft size={16} />Trước
      </button>
      <span>
        Trang {page + 1} / {query.data.totalPages}
      </span>
      <button
        disabled={page + 1 >= query.data.totalPages}
        onClick={() => setPage(page + 1)}
      >
        Sau<ArrowRight size={16} />
      </button>
    </div>
  ) : null;
}
export function ProductCard({ product, score }) {
  const onSale = product.salePrice != null && Number(product.salePrice) < Number(product.price);
  const discount = onSale ? Math.floor((Number(product.price) - Number(product.salePrice)) * 100 / Number(product.price)) : 0;
  const specification = [product.specification?.weightClass, labelOf(product.specification?.shaftFlexibility)].filter(Boolean).join(" · ");
  return (
    <article className="product-card">
      <Link to={"/products/" + product.id}>
        <div className="product-art">
          {product.thumbnailUrl ? (
            <img
              loading="lazy"
              src={media(product.thumbnailUrl)}
              alt={product.productName}
            />
          ) : (
            <span className="no-photo"><Package size={36} />CHƯA CÓ ẢNH</span>
          )}
          {score != null && <strong className="score"><Sparkles size={13} />{score}% phù hợp</strong>}
          {onSale && <strong className="sale-badge">{discount > 0 ? `-${discount}%` : "Ưu đãi"}</strong>}
        </div>
        <div className="product-copy">
          <small>{product.brand?.name}</small>
          <h3>{product.productName}</h3>
          <p className="price">{money(product.effectivePrice)}{onSale && <del>{money(product.price)}</del>}</p>
          {specification && <p className="muted">{specification}</p>}
          <Badge value={product.status} />
        </div>
      </Link>
    </article>
  );
}
