import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { api, Badge, media, money, Page, Pager, State, useData } from "../components/CommerceUI";
import { Check, Eye, Package, Search, Tag, UploadCloud, X } from "../components/icons";
import { errorMessage } from "../services/apiClient";

export default function AdminPromotionsPage() {
  const [page, setPage] = useState(0);
  const [keyword, setKeyword] = useState("");
  const [search, setSearch] = useState("");
  const [published, setPublished] = useState("");
  const [selected, setSelected] = useState(null);
  const [busy, setBusy] = useState(false);
  const editor = useRef(null);
  const params = new URLSearchParams({ page, size: 12, keyword: search });
  if (published !== "") params.set("published", published);
  const query = useData(`/admin/products?${params}`);

  useEffect(() => {
    if (selected && window.matchMedia("(max-width: 1000px)").matches) {
      editor.current?.scrollIntoView({ block: "start", behavior: "smooth" });
    }
  }, [selected?.id]);

  return <Page title="Quản lý ưu đãi" action={<Link className="button" to="/promotions"><Eye size={16} />Xem ưu đãi</Link>}>
    <form className="filters promotion-filters" onSubmit={event => { event.preventDefault(); setSearch(keyword.trim()); setPage(0); }}>
      <label>Tìm sản phẩm<input type="search" value={keyword} maxLength={230} onChange={event => setKeyword(event.target.value)} disabled={busy} /></label>
      <label>Trạng thái ưu đãi<select value={published} disabled={busy} onChange={event => { setPublished(event.target.value); setPage(0); }}>
        <option value="">Tất cả</option><option value="true">Đã đăng ưu đãi</option><option value="false">Chưa đăng ưu đãi</option>
      </select></label>
      <button disabled={busy}><Search size={16} />Tìm kiếm</button>
    </form>
    <div className="promotion-workspace">
      <section className="promotion-catalog" aria-label="Chọn sản phẩm ưu đãi">
        <State query={query} empty={!query.data?.content?.length} emptyTitle="Chưa có sản phẩm phù hợp" emptyMessage="Không tìm thấy sản phẩm trong danh sách này.">
          <div className="promotion-product-list">
            {query.data?.content?.map(product => <div className={`promotion-product-row${selected?.id === product.id ? " is-selected" : ""}`} key={product.id}>
              <div className="promotion-thumb">{product.thumbnailUrl ? <img src={media(product.thumbnailUrl)} alt={product.productName} loading="lazy" /> : <Package size={24} aria-hidden="true" />}</div>
              <div className="promotion-product-copy"><strong>{product.productName}</strong><span>{money(product.price)}</span>
                <div><Badge value={product.status} /><span className={`badge${product.promotionPublished ? " badge-active" : ""}`}>{product.promotionPublished ? "Đã đăng ưu đãi" : "Chưa đăng ưu đãi"}</span></div>
              </div>
              <button type="button" disabled={busy} aria-label={`Chọn ${product.productName}`} aria-pressed={selected?.id === product.id} onClick={() => setSelected(product)}><Tag size={16} /><span>Chọn</span></button>
            </div>)}
          </div>
        </State>
        <Pager query={query} page={page} setPage={setPage} />
      </section>
      <section className="promotion-editor" ref={editor} aria-label="Thiết lập ưu đãi">
        {selected ? <PromotionEditor key={selected.id} product={selected} busy={busy} setBusy={setBusy} onUpdated={saved => { setSelected(saved); query.reload(); }} />
          : <div className="promotion-no-selection"><Tag size={26} /><h2>Chưa chọn sản phẩm</h2></div>}
      </section>
    </div>
  </Page>;
}

function PromotionEditor({ product, busy, setBusy, onUpdated }) {
  const [discountType, setDiscountType] = useState(product.salePrice != null ? "SALE_PRICE" : "PERCENT");
  const [value, setValue] = useState(product.salePrice ?? 20);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [ending, setEnding] = useState(false);
  const inFlight = useRef(false);
  const price = Number(product.price);
  const amount = value === "" ? NaN : Number(value);
  const preview = discountType === "PERCENT" ? Math.round(price * (100 - amount)) / 100 : amount;
  const valid = Number.isFinite(amount) && amount >= 0 && (discountType !== "PERCENT" || amount > 0 && amount <= 100)
    && preview >= 0 && preview < price;
  const available = ["ACTIVE", "OUT_OF_STOCK"].includes(product.status) && price > 0;

  async function save(end = false) {
    if (inFlight.current) return;
    inFlight.current = true;
    setBusy(true); setError(""); setMessage("");
    try {
      const saved = await api(`/admin/products/${product.id}/promotion`, end ? "delete" : "put", end ? undefined : { discountType, value: Number(value) });
      onUpdated(saved);
      setEnding(false);
      setMessage(end ? "Đã kết thúc ưu đãi và khôi phục giá gốc." : "Đã đăng ưu đãi lên cửa hàng.");
      if (end) { setDiscountType("PERCENT"); setValue(20); }
    } catch (err) { setError(errorMessage(err)); }
    finally { inFlight.current = false; setBusy(false); }
  }

  return <>
    <div className="promotion-editor-heading"><Tag size={19} /><h2>Thiết lập ưu đãi</h2></div>
    <h3>{product.productName}</h3>
    <span className={`badge${product.promotionPublished ? " badge-active" : ""}`}>{product.promotionPublished ? "Đã đăng ưu đãi" : "Chưa đăng ưu đãi"}</span>
    <dl className="promotion-prices"><div><dt>Giá gốc</dt><dd>{money(price)}</dd></div>{product.salePrice != null && <div><dt>Giá bán hiện tại</dt><dd>{money(product.effectivePrice)}</dd></div>}</dl>
    {!available && <p className="notice error" role="alert">{product.status === "INACTIVE" ? "Sản phẩm đã ngừng kinh doanh." : "Giá gốc phải lớn hơn 0 để áp dụng ưu đãi."}</p>}
    <form onSubmit={event => { event.preventDefault(); if (valid && available) save(); }}>
      <fieldset className="promotion-fields" disabled={busy || !available || ending}>
        <legend>Mức ưu đãi</legend>
        <div className="promotion-modes">
          {[{ type: "PERCENT", symbol: "%", label: "Giảm theo phần trăm" }, { type: "SALE_PRICE", symbol: "₫", label: "Giá bán ưu đãi" }].map(mode => <label key={mode.type} title={mode.label} className={discountType === mode.type ? "is-active" : ""}>
            <input type="radio" name="discountType" value={mode.type} checked={discountType === mode.type} onChange={() => { setDiscountType(mode.type); setValue(mode.type === "PERCENT" ? 20 : product.salePrice ?? ""); setMessage(""); setError(""); }} /><span aria-hidden="true">{mode.symbol}</span><span>{mode.label}</span>
          </label>)}
        </div>
        <label className="field"><span>{discountType === "PERCENT" ? "Mức giảm (%)" : "Giá ưu đãi (₫)"}</span><input type="number" required step="0.01" min={discountType === "PERCENT" ? "0.01" : "0"} max={discountType === "PERCENT" ? "100" : Math.max(0, price - 0.01)} value={value} onChange={event => { setValue(event.target.value); setMessage(""); }} /></label>
        <dl className="promotion-prices promotion-preview"><div><dt>Giá sau ưu đãi</dt><dd>{valid ? money(preview) : "--"}</dd></div><div><dt>Tiết kiệm</dt><dd>{valid ? money(price - preview) : "--"}</dd></div></dl>
        <button className="primary" disabled={!valid} type="submit"><UploadCloud size={16} />{busy ? "Đang xử lý…" : product.promotionPublished ? "Cập nhật ưu đãi" : "Đăng ưu đãi"}</button>
      </fieldset>
    </form>
    {product.promotionPublished && <div className="promotion-end">
      {ending ? <div role="group" aria-label="Xác nhận kết thúc ưu đãi"><p>Kết thúc ưu đãi và khôi phục giá gốc?</p><div className="promotion-end-actions"><button className="danger" disabled={busy} onClick={() => save(true)}><X size={16} />Kết thúc</button><button disabled={busy} onClick={() => setEnding(false)}>Hủy</button></div></div>
        : <button disabled={busy} onClick={() => { setEnding(true); setMessage(""); setError(""); }}><X size={16} />Kết thúc ưu đãi</button>}
    </div>}
    {error && <p role="alert" className="notice error">{error}</p>}
    {message && <p role="status" className="notice promotion-feedback"><Check size={16} />{message}</p>}
  </>;
}
