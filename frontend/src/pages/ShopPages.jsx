import { useEffect, useState } from "react";
import { Link, useNavigate, useParams, useSearchParams } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useChatbot } from "../context/ChatbotContext";
import PaymentPanel, { PaymentMethods } from "../components/PaymentPanel";
import { errorMessage } from "../services/apiClient";
import { ArrowLeft, ArrowRight, Heart, Minus, Plus, ShoppingBag, Sparkles, Trash2 } from "../components/icons";
import {
  api,
  useData,
  Page,
  State,
  Action,
  Form,
  Field,
  ProductCard,
  Pager,
  Badge,
  money,
  media,
  labelOf,
} from "../components/CommerceUI";

const colorLabels = {
  "Aqua Blue": "Xanh nước biển", "Black": "Đen", "Black Green": "Đen xanh lá",
  "Blue/White": "Xanh dương/Trắng", "Bright White": "Trắng sáng", "Camel Gold": "Vàng camel",
  "Chocolate": "Nâu sô-cô-la", "Citadel": "Xám Citadel", "Dark Green": "Xanh lá đậm",
  "Dark Wine": "Đỏ rượu vang đậm", "Garnet": "Đỏ ngọc hồng lựu", "Gray": "Xám",
  "Grayish Pearl": "Xám ngọc trai", "Green": "Xanh lá", "Green/White": "Xanh lá/Trắng",
  "Jelly": "Màu thạch", "Kaki": "Kaki", "Lavender": "Tím oải hương",
  "Light Beige": "Be nhạt", "Lychee Red": "Đỏ vải", "Mood Indigo": "Chàm",
  "Navy": "Xanh navy", "Navy Blue": "Xanh navy", "Navy Peony": "Xanh mẫu đơn đậm",
  "Neutral Gray": "Xám trung tính", "Olive Green": "Xanh ô liu", "Paradise Green": "Xanh thiên đường",
  "Pastel Green": "Xanh pastel", "Pearl White": "Trắng ngọc trai", "Pink": "Hồng",
  "Purple Rose Red": "Tím đỏ hoa hồng", "Purple/White": "Tím/Trắng", "Sand Beige": "Be cát",
  "Silver": "Bạc", "Vermillion Orange": "Cam đỏ", "Warm White": "Trắng ấm",
  "White": "Trắng", "White Blue": "Trắng/Xanh dương", "White Gray": "Trắng/Xám",
  "White Yellow": "Trắng/Vàng", "White/Black Trim": "Trắng viền Đen", "White/Pink": "Trắng/Hồng",
};
const colorLabel = (color) => colorLabels[color] || (color.match(/^[A-Z]{3,}\d/) ? `Mã màu ${color}` : color);
const sizeOrder = { XS: 1, S: 2, M: 3, L: 4, XL: 5, XXL: 6, "2XL": 6, "3XL": 7,
  "3U": 20, "3U5": 21, "4U": 22, "4U5": 23, "4U6": 24, "5U": 25 };
const sortSizes = (sizes) => [...sizes].sort((a, b) => {
  const numericA = /^\d+$/.test(a), numericB = /^\d+$/.test(b);
  if (numericA || numericB) return numericA && numericB ? Number(a) - Number(b) : numericA ? -1 : 1;
  return (sizeOrder[a] ?? 99) - (sizeOrder[b] ?? 99) || a.localeCompare(b, "vi");
});
export function ProductListPage() {
  const { openChatbot } = useChatbot();
  const [page, setPage] = useState(0);
  const [searchParams, setSearchParams] = useSearchParams();
  const brands = useData("/brands"), cats = useData("/categories");
  const filter = Object.fromEntries(searchParams);
  const categorySlug = filter.category;
  const matchText = { racket:"vợt", shoes:"giày", apparel:"trang phục", bags:"túi", accessories:"phụ kiện", shuttlecock:"cầu" }[categorySlug];
  const category = cats.data?.find(c => matchText && c.categoryName?.toLocaleLowerCase("vi").includes(matchText));
  if (category && !filter.categoryId) filter.categoryId = String(category.categoryId);
  delete filter.category;
  const query = useData("/products?" + new URLSearchParams({ ...filter, page, size: 12 }));
  const search = searchParams.toString();
  useEffect(() => {
    setPage(0);
  }, [search]);
  return (
    <Page
      title="Tìm dụng cụ cho đường cầu của bạn"
      subtitle="Sản phẩm mới từ cửa hàng. Lọc theo thông số và nhu cầu thi đấu."
      action={
        <button type="button" className="button primary" onClick={() => openChatbot("profile")}>
          <Sparkles size={16} />Gợi ý dành cho tôi
        </button>
      }
    >
      <form
        key={`${search}|${filter.categoryId || ""}|${brands.data?.length || 0}|${cats.data?.length || 0}`}
        className="filters"
        onSubmit={(e) => {
          e.preventDefault();
          setPage(0);
          setSearchParams(
            Object.fromEntries(
              [...new FormData(e.currentTarget)].filter(([, v]) => v !== ""),
            ),
          );
        }}
      >
        <label>
          Tìm kiếm
          <input name="keyword" placeholder="Tên sản phẩm" defaultValue={filter.keyword||""} />
        </label>
        <label>
          Thương hiệu
          <select name="brandId" defaultValue={filter.brandId||""}>
            <option value="">Tất cả</option>
            {brands.data?.map((b) => (
              <option key={b.brandId} value={b.brandId}>
                {b.brandName}
              </option>
            ))}
          </select>
        </label>
        <label>
          Danh mục
          <select name="categoryId" defaultValue={filter.categoryId||""}>
            <option value="">Tất cả</option>
            {cats.data?.map((c) => (
              <option key={c.categoryId} value={c.categoryId}>
                {c.categoryName}
              </option>
            ))}
          </select>
        </label>
        <label>
          Giá từ
          <input name="minPrice" type="number" min="0" defaultValue={filter.minPrice || ""} />
        </label>
        <label>
          Giá đến
          <input name="maxPrice" type="number" min="0" defaultValue={filter.maxPrice || ""} />
        </label>
        <label>
          Trọng lượng
          <select name="weight" defaultValue={filter.weight || ""}>
            <option value="">Tất cả</option>
            {["2U", "3U", "4U", "5U", "6U"].map((v) => (
              <option key={v}>{v}</option>
            ))}
          </select>
        </label>
        <label>
          Độ cứng
          <select name="flex" defaultValue={filter.flex || ""}>
            <option value="">Tất cả</option>
            {["FLEXIBLE", "MEDIUM", "STIFF", "EXTRA_STIFF"].map((v) => (
              <option key={v} value={v}>
                {labelOf(v)}
              </option>
            ))}
          </select>
        </label>
        <label>
          Sắp xếp
          <select name="sort" defaultValue={filter.sort || "createdAt,desc"}>
            <option value="createdAt,desc">Mới nhất</option>
            <option value="price,asc">Giá tăng dần</option>
            <option value="price,desc">Giá giảm dần</option>
          </select>
        </label>
        <button className="primary">Áp dụng</button>
      </form>
      <State query={query} empty={query.data?.content?.length === 0} emptyTitle="Chưa có sản phẩm phù hợp" emptyMessage="Thử thay đổi bộ lọc hoặc quay lại khi bộ sưu tập được cập nhật.">
        <div className="product-grid">
          {query.data?.content?.map((p) => (
            <ProductCard key={p.id} product={p} />
          ))}
        </div>
      </State>
      <Pager query={query} page={page} setPage={setPage} />
    </Page>
  );
}
export function PromotionsPage() {
  const [page, setPage] = useState(0);
  const query = useData(`/products?onSale=true&page=${page}&size=12`);
  return (
    <Page title="Khuyến mại" subtitle="Sản phẩm có giá ưu đãi.">
      <State query={query} empty={!query.data?.content?.length} emptyTitle="Chưa có sản phẩm ưu đãi" emptyMessage="Ưu đãi mới sẽ được cập nhật tại đây.">
        <div className="product-grid">
          {query.data?.content?.map((product) => (
            <ProductCard key={product.id} product={product} />
          ))}
        </div>
      </State>
      <Pager query={query} page={page} setPage={setPage} />
    </Page>
  );
}

export function ProductDetailPage() {
  const { id } = useParams(),
    query = useData("/products/" + id),
    variantsQuery = useData("/products/" + id + "/variants"),
    reviews = useData("/products/" + id + "/reviews"),
    { user } = useAuth(),
    [quantity, setQuantity] = useState(1),
    [variantId, setVariantId] = useState(""),
    [added, setAdded] = useState(false),
    [saved, setSaved] = useState(false);
  const p = query.data;
  const variants = variantsQuery.data || [];
  const selectedVariant = variants.find((v) => String(v.variantId) === String(variantId));
  const hasColorOptions = variants.some((v) => v.color !== "Mặc định");
  const hasSizeOptions = variants.some((v) => v.size !== "Tiêu chuẩn");
  const chooseVariant = (color, size) => {
    const available = variants.filter((v) => v.stock > 0 && v.status === "ACTIVE");
    const matching = available.find((v) =>
      (!color || v.color === color) && (!size || v.size === size))
      || available.find((v) => !color || v.color === color)
      || available.find((v) => !size || v.size === size);
    setVariantId(matching ? String(matching.variantId) : "");
  };
  const availableStock = variants.length ? (selectedVariant?.stock || 0) : p?.stock;
  const productHasStock = variants.length
    ? variants.some((v) => v.stock > 0 && v.status === "ACTIVE")
    : (p?.stock || 0) > 0;
  return (
    <Page title={p?.productName || "Chi tiết sản phẩm"}>
      <State query={query}>
        {p && (
          <>
            <div className="detail-grid">
              <div className="gallery">
                {p.images?.length ? (
                  p.images.map((i) => (
                    <img
                      key={i.imageId}
                      src={media(i.imageUrl)}
                      alt={p.productName}
                    />
                  ))
                ) : (
                  <div className="product-art">
                    <span>Chưa có ảnh</span>
                  </div>
                )}
              </div>
              <section className="panel">
                <Badge value={p.status} />
                <h2>{p.brand?.name}</h2>
                <p className="price large">{money(p.effectivePrice)}</p>
                {p.salePrice != null && <del>{money(p.price)}</del>}
                <p className="product-description">{p.description}</p>
                <p>Còn {p.stock} sản phẩm</p>
                {variants.length > 0 && <div className="variant-picker">
                  {hasColorOptions && <label className="field">Màu<select value={selectedVariant?.color || ""} onChange={(e) => chooseVariant(e.target.value, selectedVariant?.size)}><option value="">Chọn màu</option>{[...new Set(variants.filter((v) => v.color !== "Mặc định").map((v) => v.color))].map((color) => <option key={color} value={color} disabled={!variants.some((v) => v.color === color && v.stock > 0 && v.status === "ACTIVE")}>{colorLabel(color)}</option>)}</select></label>}
                  {hasSizeOptions && <label className="field">Size<select value={selectedVariant?.size || ""} onChange={(e) => chooseVariant(selectedVariant?.color, e.target.value)}><option value="">Chọn size</option>{sortSizes(new Set(variants.filter((v) => v.size !== "Tiêu chuẩn").map((v) => v.size))).map((size) => <option key={size} value={size} disabled={!variants.some((v) => v.size === size && (!selectedVariant?.color || v.color === selectedVariant.color) && v.stock > 0 && v.status === "ACTIVE")}>{size}</option>)}</select></label>}
                  {!hasColorOptions && !hasSizeOptions && <label className="field">Lựa chọn<select value={variantId} onChange={(e) => setVariantId(e.target.value)}><option value="">Chọn sản phẩm</option>{variants.map((v) => <option key={v.variantId} value={v.variantId} disabled={v.stock < 1 || v.status !== "ACTIVE"}>Còn {v.stock} sản phẩm</option>)}</select></label>}
                  {selectedVariant && <small>Còn {selectedVariant.stock} sản phẩm</small>}
                </div>}
                {user ? (
                  <>
                    <Field
                      label="Số lượng"
                      type="number"
                      value={quantity}
                      min="1"
                      max={availableStock}
                      onChange={(e) => setQuantity(Number(e.target.value))}
                    />
                    {productHasStock && (
                      <Action
                        className="primary"
                        onClick={() => {
                          if (variants.length > 0 && !selectedVariant) {
                            throw new Error("Vui lòng chọn màu hoặc size trước khi thêm vào giỏ.");
                          }
                          return api("/cart/items", "post", {
                            productId: p.id,
                            ...(selectedVariant ? { variantId: selectedVariant.variantId } : {}),
                            quantity,
                          });
                        }}
                        onDone={() => setAdded(true)}
                      >
                        <ShoppingBag size={16} />Thêm vào giỏ
                      </Action>
                    )}
                    <Action
                      onClick={() => api("/favorites/" + p.id, "post")}
                      onDone={() => setSaved(true)}
                    >
                      <Heart size={16} />Yêu thích
                    </Action>
                    {added && (
                      <p role="status">
                        Đã thêm vào giỏ. <Link to="/cart">Xem giỏ hàng</Link>
                      </p>
                    )}
                    {saved && <p role="status">Đã lưu yêu thích.</p>}
                  </>
                ) : (
                  <Link className="button primary" to="/login">
                    Đăng nhập để mua hàng
                  </Link>
                )}
              </section>
            </div>
            {p.specification && (
              <section className="panel">
                <h2>Thông số vợt</h2>
                <dl className="spec-grid">
                  {[
                    ["Trọng lượng", "weightClass"],
                    ["Độ cứng", "shaftFlexibility"],
                    ["Cân bằng", "balanceType"],
                    ["Lối chơi", "playStyle"],
                    ["Trình độ", "skillLevel"],
                    ["Sức căng tối đa (lbs)", "maxTensionLbs"],
                    ["Chiều dài (mm)", "racketLengthMm"],
                    ["Cán", "gripSize"],
                    ["Vật liệu", "material"],
                  ].map(([label, key]) => (
                    <div key={key}>
                      <dt>{label}</dt>
                      <dd>{labelOf(p.specification[key]) ?? "—"}</dd>
                    </div>
                  ))}
                </dl>
              </section>
            )}
          </>
        )}
      </State>
      <section className="panel">
        <h2>Đánh giá người mua</h2>
        <State query={reviews} empty={!reviews.data?.content?.length}>
          {reviews.data?.content?.map((r) => (
            <article key={r.reviewId}>
              <strong>{r.rating}/5</strong>
              <p>{r.comment}</p>
            </article>
          ))}
        </State>
      </section>
    </Page>
  );
}
export function CartPage() {
  const q = useData("/cart");
  return (
    <Page
      title="Giỏ hàng"
      action={
        <Link className="button" to="/products">
          <ArrowLeft size={16} />Tiếp tục mua sắm
        </Link>
      }
    >
      <State query={q} empty={!q.data?.items?.length} emptyTitle="Giỏ hàng của bạn đang trống" emptyMessage="Khám phá cửa hàng để chọn dụng cụ cho trận cầu tiếp theo.">
        {q.data?.items?.map((i) => (
          <div className="line-item panel" key={i.cartItemId}>
            <Link to={"/products/" + i.product.id}>
              {i.product.productName}
            </Link>
            {i.variant && <small>{i.variant.color} / {i.variant.size} / SKU {i.variant.sku}</small>}
            <span>{money(i.unitPrice)}</span>
            <div className="quantity">
              <Action
                aria-label="Giảm số lượng"
                title="Giảm số lượng"
                disabled={i.quantity <= 1}
                onClick={() =>
                  api("/cart/items/" + i.cartItemId, "put", {
                    quantity: i.quantity - 1,
                  })
                }
                onDone={q.reload}
              >
                <Minus size={15} />
              </Action>
              <span>{i.quantity}</span>
              <Action
                aria-label="Tăng số lượng"
                title="Tăng số lượng"
                onClick={() =>
                  api("/cart/items/" + i.cartItemId, "put", {
                    quantity: i.quantity + 1,
                  })
                }
                onDone={q.reload}
              >
                <Plus size={15} />
              </Action>
            </div>
            <strong>{money(i.totalPrice)}</strong>
            <Action
              danger
              onClick={() => api("/cart/items/" + i.cartItemId, "delete")}
              onDone={q.reload}
            >
              <Trash2 size={15} />Xóa
            </Action>
          </div>
        ))}
        {!!q.data?.items?.length && (
          <div className="panel summary">
            <h2>Tạm tính: {money(q.data.subtotalAmount)}</h2>
            <p>Phí giao hàng được xác nhận khi tạo đơn.</p>
            <Link className="button primary" to="/checkout">
              Đến thanh toán<ArrowRight size={16} />
            </Link>
          </div>
        )}
      </State>
    </Page>
  );
}
export function CheckoutPage() {
  const addresses = useData("/addresses"), methods = useData("/payments/methods"), cart = useData("/cart"), navigate = useNavigate();
  const [method, setMethod] = useState("COD"), [busy, setBusy] = useState(false), [error, setError] = useState("");
  async function checkout(event) {
    event.preventDefault();
    if (busy || !methods.data?.some(item => item.code === method && item.available)) return;
    const fields = Object.fromEntries(new FormData(event.currentTarget));
    setBusy(true); setError("");
    try {
      const result = await api("/orders", "post", { addressId: Number(fields.addressId), paymentMethod: method, note: fields.note });
      navigate("/orders/" + result.order.orderId);
    } catch (e) { setError(errorMessage(e)); } finally { setBusy(false); }
  }
  return (
    <Page
      title="Thanh toán cửa hàng"
      subtitle="Kiểm tra địa chỉ trước khi đặt hàng."
    >
      <State query={addresses}>
        {addresses.data?.length ? (
          <State query={cart} empty={!cart.data?.items?.length} emptyTitle="Giỏ hàng trống">
            <div className="checkout-layout">
              <form className="checkout-form" onSubmit={checkout}>
                <label className="field"><span>Địa chỉ nhận hàng</span><select name="addressId" required defaultValue={addresses.data.find(a => a.isDefault)?.addressId || addresses.data[0].addressId}>
                  {addresses.data.map(a => <option key={a.addressId} value={a.addressId}>{a.receiverName} · {a.detailAddress}, {a.ward}, {a.province}</option>)}
                </select></label>
                <State query={methods}><PaymentMethods methods={methods.data || []} value={method} onChange={setMethod} /></State>
                {method === "BANK_TRANSFER" && <p className="muted transfer-preview-hint" role="status">Thông tin tài khoản và nội dung chuyển khoản sẽ hiển thị sau khi bạn đặt hàng.</p>}
                <label className="field"><span>Ghi chú</span><textarea name="note" maxLength={1000} rows={3} /></label>
                {error && <p className="notice error" role="alert">{error}</p>}
                <button type="submit" className="primary" disabled={busy || !methods.data?.some(item => item.code === method && item.available)}><ShoppingBag size={17} />{busy ? "Đang đặt hàng..." : "Đặt hàng"}</button>
              </form>
              <aside className="checkout-summary"><h2>Đơn hàng của bạn</h2>
                {cart.data?.items?.map(item => <div className="checkout-item" key={item.cartItemId}>
                  {item.product.thumbnailUrl && <img src={media(item.product.thumbnailUrl)} alt="" />}
                  <span>{item.product.productName}<small>Số lượng: {item.quantity}</small></span><strong>{money(item.totalPrice)}</strong>
                </div>)}
                <p className="checkout-subtotal">Tạm tính <strong>{money(cart.data?.subtotalAmount)}</strong></p>
              </aside>
            </div>
          </State>
        ) : (
          <p>
            Bạn cần <Link to="/addresses">thêm địa chỉ</Link> trước khi đặt
            hàng.
          </p>
        )}
      </State>
    </Page>
  );
}
export function OrdersPage() {
  const [page, setPage] = useState(0),
    q = useData("/orders/my?page=" + page);
  return (
    <Page title="Đơn hàng cửa hàng">
      <State query={q} empty={!q.data?.content?.length}>
        {q.data?.content?.map((o) => (
          <Link
            key={o.orderId}
            className="panel line-item"
            to={"/orders/" + o.orderId}
          >
            <strong>{o.orderCode}</strong>
            <Badge value={o.orderStatus} />
            <span>{money(o.totalAmount)}</span>
          </Link>
        ))}
      </State>
      <Pager query={q} page={page} setPage={setPage} />
    </Page>
  );
}
export function OrderDetailPage() {
  const { id } = useParams(),
    q = useData("/orders/" + id),
    o = q.data?.order;
  return (
    <Page title={o?.orderCode || "Đơn hàng"} action={o && <button type="button" className="primary no-print" onClick={() => window.print()}>In hóa đơn</button>}>
      <State query={q}>
        {o && (
          <>
            <div className="no-print">
            <section className="panel">
              <Badge value={o.orderStatus} />
              <Badge value={o.paymentStatus} />
              <p>
                {o.receiverName} · {o.receiverPhone}
              </p>
              <p>{o.shippingAddress}</p>
              {["PENDING", "CONFIRMED"].includes(o.orderStatus) && (
                <Action
                  danger
                  onClick={() => api("/orders/" + id + "/cancel", "post")}
                  onDone={q.reload}
                >
                  Hủy đơn hàng
                </Action>
              )}
            </section>
            <PaymentPanel order={o} onUpdate={q.reload} />
            {q.data.items.map((i) => (
              <section className="panel" key={i.orderItemId}>
                <Link to={"/products/" + i.productId}>{i.productName}</Link>
                <p>{i.variantSku ? i.variantColor + " / " + i.variantSize + " / SKU " + i.variantSku : "Chưa ghi nhận biến thể"}</p>
                <p>
                  {i.quantity} × {money(i.unitPrice)} = {money(i.totalPrice)}
                </p>
                {o.orderStatus === "COMPLETED" && (
                  <Form
                    fields={[
                      {
                        name: "rating",
                        label: "Số sao",
                        options: ["5", "4", "3", "2", "1"],
                      },
                      {
                        name: "comment",
                        label: "Nhận xét",
                        type: "textarea",
                        required: false,
                        maxLength: 2000,
                      },
                    ]}
                    initial={{ rating: 5, comment: "" }}
                    submit="Gửi đánh giá"
                    onSubmit={(v) =>
                      api("/reviews", "post", {
                        orderItemId: i.orderItemId,
                        rating: Number(v.rating),
                        comment: v.comment,
                      })
                    }
                  />
                )}
              </section>
            ))}
            <section className="panel summary">
              <p>
                Tạm tính {money(o.subtotalAmount)} · Giao hàng{" "}
                {money(o.shippingFee)}
              </p>
              <h2>Tổng cộng {money(o.totalAmount)}</h2>
            </section>
            </div>
            <article className="invoice-print">
              <header><h1>DH BADMINTON</h1><p>HÓA ĐƠN / PHIẾU ĐƠN HÀNG</p></header>
              <hr />
              <h2>Đơn hàng {o.orderCode}</h2>
              <p>Ngày tạo: {o.createdAt ? new Date(o.createdAt).toLocaleString("vi-VN") : "—"}</p>
              <p>Trạng thái đơn: {o.orderStatus} · Thanh toán: {o.paymentStatus === "PAID" ? "ĐÃ THANH TOÁN" : "CHƯA THANH TOÁN"}</p>
              <h3>Người nhận</h3>
              <p>{o.receiverName} · {o.receiverPhone}</p>
              <p>{o.shippingAddress}</p>
              <table><thead><tr><th>Sản phẩm</th><th>SL</th><th>Đơn giá</th><th>Thành tiền</th></tr></thead>
                <tbody>{q.data.items.map((item) => <tr key={item.orderItemId}><td>{item.productName}{item.variantSku ? ` (${item.variantColor || ""} ${item.variantSize || ""})` : ""}</td><td>{item.quantity}</td><td>{money(item.unitPrice)}</td><td>{money(item.totalPrice)}</td></tr>)}</tbody>
              </table>
              <div className="invoice-totals"><p>Tạm tính: {money(o.subtotalAmount)}</p><p>Phí giao hàng: {money(o.shippingFee)}</p><h3>Tổng cộng: {money(o.totalAmount)}</h3></div>
              <footer>Cảm ơn bạn đã mua hàng tại DH Badminton.</footer>
            </article>
          </>
        )}
      </State>
    </Page>
  );
}
export function FavoritesPage() {
  const [page, setPage] = useState(0);
  const q = useData("/favorites?page=" + page);
  return (
    <Page title="Sản phẩm yêu thích">
      <State query={q} empty={!q.data?.content?.length}>
        <div className="product-grid">
          {q.data?.content?.map((p) => (
            <div key={p.id}>
              <ProductCard product={p} />
              <Action
                onClick={() => api("/favorites/" + p.id, "delete")}
                onDone={q.reload}
              >
                Bỏ yêu thích
              </Action>
            </div>
          ))}
        </div>
      </State>
      <Pager query={q} page={page} setPage={setPage} />
    </Page>
  );
}
