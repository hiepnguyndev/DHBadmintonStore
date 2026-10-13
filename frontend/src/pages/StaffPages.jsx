import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import Button from "@mui/material/Button";
import {
  api,
  useData,
  Page,
  State,
  Action,
  Form,
  Upload,
  Pager,
  Badge,
  DateInput,
  formatDate,
  money,
  media,
} from "../components/CommerceUI";
import { useAuth } from "../context/AuthContext";
import { errorMessage } from "../services/apiClient";
import { ArrowUpRight, CircleAlert, Download, Package, Pencil, Plus, RotateCw, ShoppingBag, Wallet } from "../components/icons";
export function AdminPage() {
  const q = useData("/admin/statistics/overview");
  const products=useData("/products?page=0&size=100"), orders=useData("/admin/orders?page=0&size=5");
  const to=new Date(), from=new Date(); from.setDate(to.getDate()-6);
  const day=(d)=>`${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,"0")}-${String(d.getDate()).padStart(2,"0")}`;
  const revenue=useData(`/admin/statistics/revenue?from=${day(from)}&to=${day(to)}&groupBy=DAY`);
  const [search,setSearch]=useState(""),[category,setCategory]=useState(""),[status,setStatus]=useState(""), [stockProductId, setStockProductId] = useState(null);
  const productRows=(products.data?.content||[]).filter(p=>(!search||`${p.productName} ${p.id}`.toLowerCase().includes(search.toLowerCase()))&&(!category||p.category?.name===category)&&(!status||p.status===status)).slice(0,5);
  const allProducts=products.data?.content||[], categories=[...new Set(allProducts.map(p=>p.category?.name).filter(Boolean))];
  const series=(revenue.data||[]), amount=new Map(series.map(r=>[r.period,Number(r.revenue)||0]));
  const dates=Array.from({length:7},(_,i)=>{const d=new Date(from);d.setDate(from.getDate()+i);return d;});
  const vals=dates.map(d=>amount.get(day(d))||0), max=Math.max(...vals,1), points=vals.map((v,i)=>`${38+i*88},${170-v/max*132}`).join(" ");
  const reload=()=>{q.reload();products.reload();orders.reload();revenue.reload();};
  const exportCsv=()=>{const csv=["SKU,Tên sản phẩm,Danh mục,Giá,Tồn kho,Trạng thái",...allProducts.map(p=>[p.id,p.productName,p.category?.name,p.effectivePrice,p.stock,p.status].map(v=>`"${String(v??"").replaceAll('"','""')}"`).join(","))].join("\r\n");const url=URL.createObjectURL(new Blob(["\uFEFF"+csv],{type:"text/csv;charset=utf-8"}));const a=document.createElement("a");a.href=url;a.download="danh-sach-san-pham.csv";a.click();URL.revokeObjectURL(url);};
  const recent=orders.data?.content?.slice(0,5)||[];
  return <Page title="Tổng quan cửa hàng" subtitle="Hoạt động kinh doanh, tồn kho và các đơn hàng cần xử lý." action={<button className="admin-refresh" onClick={reload}><RotateCw size={15} />Cập nhật báo cáo</button>}>
    <State query={q}><div className="admin-metrics"><article><span><Package size={18} /></span><div><small>Tổng sản phẩm</small><strong>{q.data?.products??0}</strong><em>Danh mục đang quản lý</em></div></article><article><span><ShoppingBag size={18} /></span><div><small>Đơn hàng mới</small><strong>{q.data?.pendingOrders??0}</strong><em>Đang chờ xử lý</em></div></article><article><span><Wallet size={18} /></span><div><small>Doanh thu</small><strong>{money(q.data?.shopRevenue)}</strong><em>Các đơn đã thanh toán</em></div></article><article><span><CircleAlert size={18} /></span><div><small>Sắp hết hàng</small><strong>{q.data?.lowStockProducts??0}</strong><em>Tồn kho từ 1 đến 5 món</em></div></article></div></State>
    <div className="admin-dashboard-row">
      <section className="admin-card admin-chart-card"><div className="admin-card-heading"><h2>Doanh thu 7 ngày gần đây</h2><span>{day(from)} / {day(to)}</span></div>
        <State query={revenue} empty={!series.length} emptyTitle="Chưa có doanh thu" emptyMessage="Doanh thu được ghi nhận ngay khi đơn được thanh toán.">
          <div className="admin-chart"><div className="chart-y-labels">{[1, .75, .5, .25, 0].map(ratio => <span key={ratio}>{new Intl.NumberFormat("vi-VN", { notation: "compact", maximumFractionDigits: 1 }).format(max * ratio)}</span>)}</div>
            <svg viewBox="0 0 620 205" role="img" aria-label="Biểu đồ doanh thu bảy ngày">
              {[30, 66, 102, 138, 174].map(y => <line key={y} x1="38" y1={y} x2="598" y2={y} stroke="#dce8df" />)}
              <polygon points={`38,174 ${points} 566,174`} fill="#e0f1e6" /><polyline points={points} fill="none" stroke="#087c63" strokeWidth="3" />
              {vals.map((v, i) => <circle key={i} cx={38 + i * 88} cy={170 - v / max * 132} r="4" fill="#087c63" stroke="white" strokeWidth="2" />)}
              {dates.map((d, i) => <text key={i} x={38 + i * 88} y="196" textAnchor="middle" fill="#82998a" fontSize="11">{["CN", "T2", "T3", "T4", "T5", "T6", "T7"][d.getDay()]}</text>)}
            </svg>
          </div>
        </State>
      </section>
      <section className="admin-card admin-orders-card"><div className="admin-card-heading"><h2>Đơn hàng gần đây</h2><Link to="/admin/orders">Xem tất cả <ArrowUpRight size={13} /></Link></div>
        <State query={orders} empty={!recent.length} emptyTitle="Chưa có đơn hàng" emptyMessage="Các đơn mới sẽ được hiển thị tại đây."><div className="admin-table-wrap"><table className="admin-table"><thead><tr><th>Mã đơn</th><th>Khách hàng</th><th>Tổng tiền</th><th>Trạng thái</th></tr></thead><tbody>{recent.map(o => <tr key={o.orderId}><td><Link to={`/orders/${o.orderId}`}>#{o.orderCode}</Link></td><td>{o.receiverName}</td><td>{money(o.totalAmount)}</td><td><Badge value={o.orderStatus} /></td></tr>)}</tbody></table></div></State>
      </section>
    </div>
    <section className="admin-card admin-product-card"><div className="admin-card-heading"><div><h2>Danh mục sản phẩm</h2><p>Sản phẩm, giá bán và tồn kho hiện tại</p></div><Button startIcon={<Plus size={16} />} variant="contained" component={Link} to="/admin/products">Thêm sản phẩm</Button></div>
      <div className="admin-product-filters"><input aria-label="Tìm sản phẩm" placeholder="Tên sản phẩm, mã sản phẩm..." value={search} onChange={e => setSearch(e.target.value)} /><select aria-label="Danh mục sản phẩm" value={category} onChange={e => setCategory(e.target.value)}><option value="">Tất cả danh mục</option>{categories.map(c => <option key={c}>{c}</option>)}</select><select aria-label="Trạng thái sản phẩm" value={status} onChange={e => setStatus(e.target.value)}><option value="">Tất cả trạng thái</option><option value="ACTIVE">Còn hàng</option><option value="OUT_OF_STOCK">Hết hàng</option><option value="INACTIVE">Ngừng bán</option></select><button className="admin-export" disabled={!allProducts.length} onClick={exportCsv}><Download size={15} />Xuất CSV</button></div>
      <State query={products}><div className="admin-table-wrap"><table className="admin-table admin-products-table"><thead><tr><th>Mã</th><th>Tên sản phẩm</th><th>Danh mục</th><th>Giá bán</th><th>Tồn kho</th><th>Trạng thái</th><th>Thao tác</th></tr></thead><tbody>
        {productRows.map(p => <ProductInventoryRows key={p.id} product={p} expanded={stockProductId === p.id} onToggle={() => setStockProductId(stockProductId === p.id ? null : p.id)} />)}
        {!productRows.length && <tr><td colSpan={7} className="admin-empty-row">Chưa có sản phẩm phù hợp.</td></tr>}
      </tbody></table></div></State>
    </section>
  </Page>;
}
const specFields = [
  {
    name: "weightClass",
    label: "Trọng lượng",
    options: ["2U", "3U", "4U", "5U", "6U"],
  },
  {
    name: "balanceType",
    label: "Cân bằng",
    options: ["HEAD_HEAVY", "EVEN_BALANCE", "HEAD_LIGHT"],
  },
  {
    name: "shaftFlexibility",
    label: "Độ cứng",
    options: ["FLEXIBLE", "MEDIUM", "STIFF", "EXTRA_STIFF"],
  },
  {
    name: "playStyle",
    label: "Lối chơi",
    options: ["ATTACK", "DEFENSE", "BALANCED", "SPEED", "CONTROL"],
  },
  {
    name: "skillLevel",
    label: "Trình độ",
    options: ["BEGINNER", "INTERMEDIATE", "ADVANCED"],
  },
];
const productFields = [
  { name: "productName", label: "Tên sản phẩm", maxLength: 230 },
  { name: "description", label: "Mô tả", type: "textarea", required: false },
  { name: "price", label: "Giá (₫)", type: "number", min: 0 },
  {
    name: "salePrice",
    label: "Giá bán ưu đãi (₫)",
    type: "number",
    min: 0,
    required: false,
  },
  { name: "stock", label: "Tồn kho", type: "number", min: 0 },
  {
    name: "status",
    label: "Trạng thái",
    options: ["ACTIVE", "INACTIVE", "OUT_OF_STOCK"],
  },
  {
    name: "isRacket",
    label: "Thông số vợt",
    options: [
      { value: "yes", label: "Sản phẩm là vợt" },
      { value: "no", label: "Dụng cụ khác" },
    ],
  },
  ...specFields,
];
export function AdminProductsPage() {
  const [page, setPage] = useState(0);
  const cats = useData("/categories"),
    brands = useData("/brands"),
    q = useData("/admin/products?page=" + page + "&size=20"),
    [selected, setSelected] = useState(null), [stockProductId, setStockProductId] = useState(null);
  useEffect(() => {
    const editId = new URLSearchParams(window.location.search).get("edit");
    const product = q.data?.content?.find((item) => String(item.id) === editId);
    if (product) setSelected(product);
  }, [q.data]);
  const [imageFile, setImageFile] = useState(null);
  const [imageInputKey, setImageInputKey] = useState(0);
  const [imageMessage, setImageMessage] = useState("");
  const [imagePreview, setImagePreview] = useState("");
  useEffect(() => {
    if (!imageFile) {
      setImagePreview("");
      return undefined;
    }
    const previewUrl = URL.createObjectURL(imageFile);
    setImagePreview(previewUrl);
    return () => URL.revokeObjectURL(previewUrl);
  }, [imageFile]);
  function chooseProductImage(event) {
    const file = event.target.files?.[0] || null;
    if (!file) return;
    if (!["image/png", "image/jpeg"].includes(file.type)) {
      setImageMessage("Chỉ hỗ trợ ảnh PNG hoặc JPEG.");
      setImageFile(null);
      setImageInputKey((key) => key + 1);
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      setImageMessage("Ảnh phải có dung lượng tối đa 5 MB.");
      setImageFile(null);
      setImageInputKey((key) => key + 1);
      return;
    }
    setImageMessage("");
    setImageFile(file);
  }
  return (
    <Page
      title="Quản lý sản phẩm"
      action={<Link to="/admin/promotions">Quản lý ưu đãi</Link>}
    >
      <div className="detail-grid">
        <section className="panel">
          <h2>{selected ? "Sửa sản phẩm" : "Thêm sản phẩm"}</h2>
          <div className="product-image-picker">
            {imagePreview ? (
              <img className="product-image-preview" src={imagePreview} alt="Xem trước ảnh sản phẩm" />
            ) : selected?.images?.[0]?.imageUrl ? (
              <img className="product-image-preview" src={media(selected.images[0].imageUrl)} alt={selected.productName} />
            ) : (
              <div className="product-image-placeholder" aria-hidden="true">Ảnh</div>
            )}
            <div className="product-image-copy">
              <h3>Ảnh sản phẩm</h3>
              <p>Chọn ảnh chính. Ảnh sẽ được tải lên sau khi lưu thông tin sản phẩm.</p>
              <label>
                Chọn ảnh PNG/JPEG, tối đa 5 MB
                <input
                  key={imageInputKey}
                  type="file"
                  accept="image/png,image/jpeg"
                  onChange={chooseProductImage}
                />
              </label>
              {imageFile && <small>Đã chọn: {imageFile.name}</small>}
            </div>
            {imageMessage && <p className="notice error product-image-message" role="alert">{imageMessage}</p>}
          </div>
          {cats.data && brands.data && (
            <Form
              key={selected?.id || "new"}
              fields={[
                {
                  name: "categoryId",
                  label: "Danh mục",
                  options: cats.data.map((c) => ({
                    value: c.categoryId,
                    label: c.categoryName,
                  })),
                },
                {
                  name: "brandId",
                  label: "Thương hiệu",
                  options: brands.data.map((b) => ({
                    value: b.brandId,
                    label: b.brandName,
                  })),
                },
                ...productFields,
              ]}
              initial={
                selected
                  ? {
                      ...selected,
                      categoryId: selected.category.id,
                      brandId: selected.brand.id,
                      isRacket: selected.specification ? "yes" : "no",
                      ...selected.specification,
                    }
                  : {
                      categoryId: cats.data[0]?.categoryId,
                      brandId: brands.data[0]?.brandId,
                      status: "ACTIVE",
                      price: 1000000,
                      stock: 1,
                      isRacket: "yes",
                      weightClass: "4U",
                      balanceType: "EVEN_BALANCE",
                      shaftFlexibility: "MEDIUM",
                      playStyle: "BALANCED",
                      skillLevel: "INTERMEDIATE",
                    }
              }
              submit={selected ? "Lưu thay đổi" : "Tạo sản phẩm"}
              onSubmit={async (v) => {
                const payload = {
                  productName: v.productName,
                  description: v.description || null,
                  price: Number(v.price),
                  salePrice:
                    v.salePrice === "" || v.salePrice == null
                      ? null
                      : Number(v.salePrice),
                  stock: Number(v.stock),
                  status: v.status,
                  categoryId: Number(v.categoryId),
                  brandId: Number(v.brandId),
                  specification:
                    v.isRacket === "yes"
                      ? Object.fromEntries(
                          specFields.map((f) => [f.name, v[f.name]]),
                        )
                      : null,
                };
                const saved = await api(
                  "/products" + (selected ? "/" + selected.id : ""),
                  selected ? "put" : "post",
                  payload,
                );
                if (imageFile) {
                  const data = new FormData();
                  data.append("file", imageFile);
                  try {
                    const uploadedImage = await api(
                      "/products/" + (saved.id || selected?.id) + "/images",
                      "post",
                      data,
                    );
                    saved.images = [...(saved.images || []), uploadedImage];
                    if (uploadedImage?.isPrimary) {
                      saved.thumbnailUrl = uploadedImage.imageUrl;
                    }
                    setImageMessage("");
                  } catch (error) {
                    setImageMessage(
                      "Sản phẩm đã lưu nhưng ảnh chưa tải lên được: " +
                        errorMessage(error) +
                        ". Bạn có thể thử lại ở mục ảnh bổ sung bên dưới.",
                    );
                  }
                  setImageFile(null);
                  setImageInputKey((key) => key + 1);
                }
                return saved;
              }}
              onDone={(p) => {
                setSelected(p);
                q.reload();
              }}
            />
          )}
          {selected && (
            <>
              <VariantAdminPanel productId={selected.id} />
              <h3>Ảnh bổ sung</h3>
              <p className="muted">Tải thêm ảnh khác sau khi sản phẩm đã được tạo.</p>
              <Upload
                path={"/products/" + selected.id + "/images"}
                onDone={q.reload}
              />
              <button onClick={() => setSelected(null)}>
                Thêm sản phẩm khác
              </button>
              <Action
                danger
                onClick={() => api("/products/" + selected.id, "delete")}
                onDone={() => {
                  setSelected(null);
                  q.reload();
                }}
              >
                Ngừng kinh doanh
              </Action>
            </>
          )}
        </section>
        <section>
          <State query={q} empty={!q.data?.content?.length}>
            {q.data?.content?.map((p) => (
              <div className="panel line-item" key={p.id}>
                <strong>{p.productName}</strong>
                <span>{money(p.effectivePrice)}</span>
                <span>Tồn kho: {p.stock ?? 0}</span>
                <button onClick={() => setStockProductId(stockProductId === p.id ? null : p.id)}>{stockProductId === p.id ? "Ẩn tồn theo màu/size" : "Xem tồn theo màu/size"}</button>
                <button
                  onClick={() => {
                    setSelected(p);
                    setImageFile(null);
                    setImageMessage("");
                    setImageInputKey((key) => key + 1);
                  }}
                >
                  Sửa
                </button>
                {stockProductId === p.id && <VariantStockTable productId={p.id} />}
              </div>
            ))}
          </State>
          <Pager query={q} page={page} setPage={setPage} />
          <section className="panel">
            <h2>Danh mục và thương hiệu</h2>
            <CatalogStatus query={cats} type="categories" idKey="categoryId" nameKey="categoryName" />
            <CatalogStatus query={brands} type="brands" idKey="brandId" nameKey="brandName" />
            <h2>Thêm danh mục</h2>
            <Form
              fields={[{ name: "categoryName", label: "Tên danh mục" }]}
              onSubmit={(v) => api("/categories", "post", v)}
              onDone={cats.reload}
            />
            <h2>Thêm thương hiệu</h2>
            <Form
              fields={[{ name: "brandName", label: "Tên thương hiệu" }]}
              onSubmit={(v) => api("/brands", "post", v)}
              onDone={brands.reload}
            />
          </section>
        </section>
      </div>
    </Page>
  );
}
function ProductInventoryRows({ product, expanded, onToggle }) {
  return <>
    <tr><td>SP{String(product.id).padStart(3, "0")}</td><td className="admin-product-name">{product.thumbnailUrl && <img src={media(product.thumbnailUrl)} alt="" />}<strong>{product.productName}</strong></td><td>{product.category?.name || "—"}</td><td>{money(product.effectivePrice)}</td><td>{product.stock ?? 0}</td><td><Badge value={product.status} /></td><td><button className="link-button" onClick={onToggle}>{expanded ? "Ẩn tồn" : "Xem tồn"}</button><Link className="icon-button" aria-label={`Chỉnh sửa ${product.productName}`} title="Chỉnh sửa sản phẩm" to={`/admin/products?edit=${product.id}`}><Pencil size={16} /></Link></td></tr>
    {expanded && <tr className="variant-stock-row"><td colSpan={7}><VariantStockTable productId={product.id} /></td></tr>}
  </>;
}
function VariantStockTable({ productId }) {
  const q = useData("/admin/products/" + productId + "/variants");
  return <State query={q} empty={!q.data?.length} emptyTitle="Sản phẩm chưa có màu hoặc size" emptyMessage="Tồn kho hiện đang được quản lý chung cho sản phẩm này.">
    <div className="variant-stock-table"><strong>Tồn kho theo màu và size</strong><table className="admin-table"><thead><tr><th>Màu</th><th>Size</th><th>Tồn kho</th><th>Trạng thái</th></tr></thead><tbody>{q.data?.map((variant) => <tr key={variant.variantId}><td>{variant.color}</td><td>{variant.size}</td><td>{variant.stock}</td><td><Badge value={variant.status} /></td></tr>)}</tbody></table></div>
  </State>;
}function VariantAdminPanel({ productId }) {
  const q = useData("/admin/products/" + productId + "/variants");
  const totalStock = q.data?.reduce((sum, variant) => sum + variant.stock, 0) || 0;
  return <section className="panel"><h3>Biến thể sản phẩm · Tổng tồn: {totalStock}</h3>
    <State query={q} empty={!q.data?.length}>{q.data?.map((v) => <div className="line-item" key={v.variantId}>
      <strong>Màu: {v.color} · Size: {v.size}</strong><span>Tồn kho: {v.stock} · {v.status}</span>
      <Form key={v.variantId + ":" + v.stock + ":" + v.status} fields={[{name:"sku",label:"SKU"},{name:"color",label:"Màu"},{name:"size",label:"Size"},{name:"stock",label:"Tồn kho",type:"number",min:0},{name:"status",label:"Trạng thái",options:["ACTIVE","INACTIVE","OUT_OF_STOCK"]}]} initial={v} submit="Cập nhật" onSubmit={(x)=>api("/admin/product-variants/"+v.variantId,"put",{...x,stock:Number(x.stock)})} onDone={q.reload}/>
    </div>)}</State>
    <h4>Thêm biến thể</h4><Form fields={[{name:"sku",label:"SKU"},{name:"color",label:"Màu"},{name:"size",label:"Size"},{name:"stock",label:"Tồn kho",type:"number",min:0}]} initial={{stock:0}} submit="Tạo biến thể" onSubmit={(x)=>api("/admin/products/"+productId+"/variants","post",{...x,stock:Number(x.stock)})} onDone={q.reload}/>
  </section>;
}
export function AdminOrdersPage() {
  const today = new Date().toLocaleDateString("en-CA", { timeZone: "Asia/Ho_Chi_Minh" });
  const [page, setPage] = useState(0), [date, setDate] = useState(today),
    q = useData("/admin/orders?" + new URLSearchParams({ page, size: 20, ...(date ? { date } : {}) }));
  const [confirmation, setConfirmation] = useState(""), [resetting, setResetting] = useState(false), [resetMessage, setResetMessage] = useState(""), [resetError, setResetError] = useState("");
  const next = {
    PENDING: "CONFIRMED",
    CONFIRMED: "PREPARING",
    PREPARING: "SHIPPING",
    SHIPPING: "COMPLETED",
  };
  async function resetOrders() {
    if (confirmation !== "XOA_DON_HANG" || resetting) return;
    setResetting(true); setResetError(""); setResetMessage("");
    try {
      const result = await api("/admin/orders/reset", "post", { confirmation });
      setResetMessage(`Đã xóa ${result.ordersDeleted} đơn hàng và ${result.orderItemsDeleted} dòng đơn. Doanh thu đã về 0.`);
      setConfirmation(""); setPage(0); q.reload();
    } catch (error) { setResetError(errorMessage(error)); } finally { setResetting(false); }
  }
  return (
    <Page title="Xử lý đơn cửa hàng" subtitle="Theo dõi và cập nhật trạng thái đơn bán từ cửa hàng.">
      <section className="admin-date-orders" aria-labelledby="daily-orders-heading">
        <div><span className="eyebrow">ĐƠN HÀNG THEO NGÀY</span><h2 id="daily-orders-heading">Đơn phát sinh trong ngày</h2><p className="muted">{date ? `Đang xem ngày ${formatDate(date)}.` : "Đang xem tất cả đơn hàng."}</p></div>
        <div className="daily-order-controls"><label className="field"><span>Chọn ngày</span><DateInput name="date" value={date} onChange={(event) => { setDate(event.target.value); setPage(0); }} /></label><button type="button" onClick={() => { setDate(""); setPage(0); }} disabled={!date}>Tất cả đơn</button></div>
      </section>
      <div className="daily-order-count" aria-live="polite"><strong>{q.data?.totalElements ?? 0}</strong> đơn hàng {date ? "trong ngày đã chọn" : "trong toàn bộ hệ thống"}</div>
      <State query={q} empty={!q.data?.content?.length} emptyTitle="Chưa có đơn hàng trong ngày này" emptyMessage="Chọn một ngày khác hoặc xem toàn bộ đơn hàng.">
        {q.data?.content?.map((o) => (
          <div className="panel line-item" key={o.orderId}>
            <Link to={"/orders/" + o.orderId}>{o.orderCode}</Link>
            <Badge value={o.orderStatus} />
            <Badge value={o.paymentStatus} />
            <strong>{money(o.totalAmount)}</strong>
            {next[o.orderStatus] && (
              <Action
                onClick={() =>
                  api("/admin/orders/" + o.orderId + "/status", "patch", {
                    status: next[o.orderStatus],
                  })
                }
                onDone={q.reload}
              >
                Chuyển sang <Badge value={next[o.orderStatus]} />
              </Action>
            )}
          </div>
        ))}
      </State>
      <Pager query={q} page={page} setPage={setPage} />
      <section className="admin-reset-orders" aria-labelledby="reset-orders-heading">
        <div><span className="eyebrow">VÙNG QUẢN TRỊ</span><h2 id="reset-orders-heading">Reset đơn hàng và doanh thu</h2><p>Chỉ xóa đơn của cửa hàng, dòng đơn, đánh giá gắn với đơn và thông tin thanh toán. Tồn kho được hoàn lại; sản phẩm, tài khoản, địa chỉ và Marketplace được giữ nguyên.</p></div>
        <div className="reset-orders-controls"><label className="field"><span>Nhập <strong>XOA_DON_HANG</strong> để xác nhận</span><input value={confirmation} onChange={(event) => setConfirmation(event.target.value)} autoComplete="off" /></label><button type="button" className="danger" disabled={confirmation !== "XOA_DON_HANG" || resetting} onClick={resetOrders}>{resetting ? "Đang reset..." : "Reset đơn hàng"}</button></div>
        {resetMessage && <p className="notice" role="status">{resetMessage}</p>}{resetError && <p className="notice error" role="alert">{resetError}</p>}
      </section>
    </Page>
  );
}
export function AdminListingsPage() {
  const [page, setPage] = useState(0),
    q = useData(
      "/admin/marketplace/listings?status=PENDING_REVIEW&page=" + page,
    );
  return (
    <Page title="Tin Marketplace chờ duyệt">
      <State query={q} empty={!q.data?.content?.length}>
        {q.data?.content?.map((item) => (
          <section className="panel" key={item.listing.listingId}>
            <Link to={"/marketplace/" + item.listing.listingId}>
              <h2>{item.listing.productName}</h2>
            </Link>
            <p>{item.listing.description}</p>
            <p>{money(item.listing.sellingPrice)}</p>
            <Action
              onClick={() =>
                api(
                  "/admin/marketplace/listings/" +
                    item.listing.listingId +
                    "/approve",
                  "post",
                )
              }
              onDone={q.reload}
            >
              Duyệt tin
            </Action>
            <Form
              fields={[
                { name: "reason", label: "Lý do từ chối", maxLength: 1000 },
              ]}
              submit="Từ chối"
              onSubmit={(v) =>
                api(
                  "/admin/marketplace/listings/" +
                    item.listing.listingId +
                    "/reject",
                  "post",
                  v,
                )
              }
              onDone={q.reload}
            />
          </section>
        ))}
      </State>
      <Pager query={q} page={page} setPage={setPage} />
    </Page>
  );
}
export function AdminDisputesPage() {
  const [page, setPage] = useState(0),
    q = useData("/admin/disputes?page=" + page);
  return (
    <Page title="Giải quyết tranh chấp">
      <State query={q} empty={!q.data?.content?.length}>
        {q.data?.content?.map((d) => (
          <section className="panel" key={d.disputeId}>
            <Link to={"/disputes/" + d.disputeId}>
              <h2>Tranh chấp #{d.disputeId}</h2>
            </Link>
            <Badge value={d.status} />
            <p>{d.description}</p>
            {!["RESOLVED", "REJECTED"].includes(d.status) && (
              <Form
                fields={[
                  {
                    name: "resolution",
                    label: "Phương án xử lý",
                    options: [
                      {
                        value: "RELEASE_TO_SELLER",
                        label: "Giải ngân cho người bán",
                      },
                      {
                        value: "FULL_REFUND",
                        label: "Hoàn toàn bộ tiền cho người mua",
                      },
                      {
                        value: "REJECT_DISPUTE",
                        label: "Bác tranh chấp, hoàn tất giao dịch",
                      },
                    ],
                  },
                  {
                    name: "resolutionNote",
                    label: "Căn cứ và kết luận",
                    type: "textarea",
                    maxLength: 5000,
                  },
                ]}
                initial={{ resolution: "RELEASE_TO_SELLER" }}
                submit="Xác nhận xử lý"
                onSubmit={(v) =>
                  api("/admin/disputes/" + d.disputeId + "/resolve", "post", v)
                }
                onDone={q.reload}
              />
            )}
          </section>
        ))}
      </State>
      <Pager query={q} page={page} setPage={setPage} />
    </Page>
  );
}
export function AdminUsersPage() {
  const [page, setPage] = useState(0),
    [keyword, setKeyword] = useState(""),
    q = useData("/admin/users?" + new URLSearchParams({ page, keyword }));
  return (
    <Page title="Quản lý người dùng">
      <form
        className="filters"
        onSubmit={(e) => {
          e.preventDefault();
          setPage(0);
          setKeyword(new FormData(e.currentTarget).get("keyword"));
        }}
      >
        <label>
          Tên hoặc email
          <input name="keyword" />
        </label>
        <button>Tìm kiếm</button>
      </form>
      <State query={q}>
        {q.data?.content?.map((u) => (
          <div className="panel line-item" key={u.id}>
            <div>
              <strong>{u.fullName}</strong>
              <p>{u.email}</p>
            </div>
            <Badge value={u.role} />
            <Action
              danger
              onClick={() => api("/admin/users/" + u.id + "/lock", "post")}
              onDone={q.reload}
            >
              Khóa
            </Action>
            <Action
              onClick={() => api("/admin/users/" + u.id + "/unlock", "post")}
              onDone={q.reload}
            >
              Mở khóa
            </Action>
          </div>
        ))}
      </State>
      <Pager query={q} page={page} setPage={setPage} />
    </Page>
  );
}
export function InspectionsPage({ mine = false }) {
  const [page, setPage] = useState(0),
    q = useData(
      (mine ? "/inspections/my" : "/inspector/inspections") + "?page=" + page,
    ),
    { user } = useAuth();
  return (
    <Page title={mine ? "Kiểm định của tôi" : "Bàn làm việc kiểm định"}>
      <State query={q} empty={!q.data?.content?.length}>
        {q.data?.content?.map((i) => (
          <section className="panel" key={i.inspectionId}>
            <h2>{i.inspectionCode}</h2>
            <Badge value={i.status} />
            <p>{i.note}</p>
            <Link to={"/marketplace/" + i.listingId}>Xem dụng cụ</Link>
            {!mine && (
              <div className="actions">
                {i.status === "REQUESTED" && (
                  <Action
                    onClick={() =>
                      api(
                        "/inspector/inspections/" + i.inspectionId + "/accept",
                        "post",
                      )
                    }
                    onDone={q.reload}
                  >
                    Nhận kiểm định
                  </Action>
                )}
                {i.inspectorId === user.id && i.status === "ACCEPTED" && (
                  <Action
                    onClick={() =>
                      api(
                        "/inspector/inspections/" + i.inspectionId + "/start",
                        "post",
                      )
                    }
                    onDone={q.reload}
                  >
                    Bắt đầu kiểm định
                  </Action>
                )}
                {i.inspectorId === user.id && i.status === "INSPECTING" && (
                  <Link
                    className="button"
                    to={"/inspector/" + i.inspectionId + "/report"}
                  >
                    Lập báo cáo
                  </Link>
                )}
              </div>
            )}
          </section>
        ))}
      </State>
      <Pager query={q} page={page} setPage={setPage} />
    </Page>
  );
}
export function InspectionReportPage() {
  const { id } = useParams();
  const bools = [
    "modelVerified",
    "serialVerified",
    "crackDetected",
    "repairDetected",
  ];
  return (
    <Page title="Báo cáo kiểm định">
      <Form
        fields={[
          ...bools.map((name, index) => ({
            name,
            label: [
              "Model đúng",
              "Serial xác thực",
              "Có vết nứt",
              "Có dấu sửa chữa",
            ][index],
            options: [
              { value: "false", label: "Không" },
              { value: "true", label: "Có" },
            ],
          })),
          ...[
            "frameCondition",
            "paintCondition",
            "grommetCondition",
            "handleCondition",
          ].map((name, index) => ({
            name,
            label: [
              "Tình trạng khung",
              "Tình trạng sơn",
              "Tình trạng gen",
              "Tình trạng tay cầm",
            ][index],
            maxLength: 50,
          })),
          {
            name: "overallScore",
            label: "Điểm tổng kiểm định (0–10)",
            type: "number",
            min: 0,
            max: 10,
            step: 0.01,
          },
          {
            name: "recommendedMinPrice",
            label: "Giá tham khảo thấp (₫)",
            type: "number",
            min: 0,
          },
          {
            name: "recommendedMaxPrice",
            label: "Giá tham khảo cao (₫)",
            type: "number",
            min: 0,
          },
          {
            name: "notes",
            label: "Ghi chú chuyên môn",
            type: "textarea",
            required: false,
            maxLength: 5000,
          },
        ]}
        initial={{
          modelVerified: "false",
          serialVerified: "false",
          crackDetected: "false",
          repairDetected: "false",
        }}
        submit="Hoàn tất kiểm định"
        onSubmit={(v) =>
          api("/inspector/inspections/" + id + "/report", "post", {
            ...v,
            ...Object.fromEntries(bools.map((k) => [k, v[k] === "true"])),
          })
        }
      />
    </Page>
  );
}
export function StatisticsPage() {
  const [range, setRange] = useState({
      from: "2026-01-01",
      to: "2026-12-31",
      groupBy: "MONTH",
    }),
    q = useData("/admin/statistics/revenue?" + new URLSearchParams(range)),
    products = useData("/admin/statistics/top-products"),
    sellers = useData("/admin/statistics/top-sellers");
  return (
    <Page title="Thống kê">
      <Form
        fields={[
          { name: "from", label: "Từ ngày", type: "date" },
          { name: "to", label: "Đến ngày", type: "date" },
          {
            name: "groupBy",
            label: "Nhóm theo",
            options: [
              { value: "DAY", label: "Ngày" },
              { value: "MONTH", label: "Tháng" },
              { value: "YEAR", label: "Năm" },
            ],
          },
        ]}
        initial={range}
        submit="Xem báo cáo"
        onSubmit={async (v) => setRange(v)}
      />
      <section className="panel">
        <h2>Doanh thu đơn cửa hàng đã hoàn thành</h2>
        <State query={q}>
          <table>
            <thead>
              <tr>
                <th>Kỳ</th>
                <th>Đơn</th>
                <th>Doanh thu</th>
              </tr>
            </thead>
            <tbody>
              {q.data?.map((r) => (
                <tr key={r.period}>
                  <td>{r.period}</td>
                  <td>{r.orderCount}</td>
                  <td>{money(r.revenue)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </State>
      </section>
      <section className="panel">
        <h2>Sản phẩm bán nhiều</h2>
        <State query={products} empty={!products.data?.length}>
          {products.data?.map((p) => (
            <p key={p.productId}>
              {p.productName} · {p.quantity} sản phẩm · {money(p.revenue)}
            </p>
          ))}
        </State>
      </section>
      <section className="panel">
        <h2>Người bán có nhiều giao dịch hoàn thành</h2>
        <State query={sellers} empty={!sellers.data?.length}>
          {sellers.data?.map((s) => (
            <p key={s.sellerId}>
              <Link to={"/sellers/" + s.sellerId}>{s.fullName}</Link> ·{" "}
              {s.completedTransactions} giao dịch
            </p>
          ))}
        </State>
      </section>
    </Page>
  );
}
export function AdminShipmentsPage() {
  const [order, setOrder] = useState(null);
  return (
    <Page title="Cập nhật vận chuyển Marketplace">
      <Form
        fields={[
          {
            name: "orderId",
            label: "ID giao dịch Marketplace",
            type: "number",
            min: 1,
          },
        ]}
        submit="Tra cứu giao dịch"
        onSubmit={(v) => api("/market-orders/" + v.orderId)}
        onDone={setOrder}
      />
      {order && (
        <section className="panel">
          <h2>{order.order.orderCode}</h2>
          <Badge value={order.order.orderStatus} />
          {order.shipment ? (
            <>
              <p>
                {order.shipment.carrier} · {order.shipment.trackingCode}
              </p>
              <Badge value={order.shipment.shippingStatus} />
              <Form
                key={order.shipment.shipmentId}
                fields={[
                  {
                    name: "shippingStatus",
                    label: "Trạng thái vận chuyển",
                    options: [
                      "PICKED_UP",
                      "IN_TRANSIT",
                      "DELIVERED",
                      "FAILED",
                      "RETURNED",
                    ],
                  },
                ]}
                initial={{ shippingStatus: "PICKED_UP" }}
                onSubmit={(v) =>
                  api(
                    "/shipments/" + order.shipment.shipmentId + "/status",
                    "patch",
                    v,
                  )
                }
                onDone={(shipment) => setOrder((v) => ({ ...v, shipment }))}
              />
            </>
          ) : (
            <p>Người bán chưa tạo vận đơn.</p>
          )}
        </section>
      )}
    </Page>
  );
}
function CatalogStatus({query,type,idKey,nameKey}) {
  return <State query={query}>{query.data?.map(item=><div className="line-item" key={item[idKey]}>
    <span>{item[nameKey]} · <Badge value={item.status}/></span>
    <Action onClick={()=>api("/"+type+"/"+item[idKey],"put",{[nameKey]:item[nameKey],description:item.description,logoUrl:item.logoUrl,status:item.status==="ACTIVE"?"INACTIVE":"ACTIVE"})} onDone={query.reload}>{item.status==="ACTIVE"?"Ẩn":"Kích hoạt"}</Action>
  </div>)}</State>;
}
export function AuditLogsPage() {
  const [page,setPage]=useState(0), q=useData("/admin/audit-logs?page="+page+"&size=20");
  return <Page title="Nhật ký hệ thống"><State query={q} empty={!q.data?.content?.length}>
    {q.data?.content?.map(log=><article className="panel" key={log.auditId}>
      <strong>{log.action} · {log.entityType} #{log.entityId}</strong>
      <p>{formatDate(log.createdAt)} · {log.userId ? "Người dùng #"+log.userId : "Hệ thống"}</p>
      <details><summary>Dữ liệu thay đổi</summary><p>Trước: {log.oldData || "—"}</p><p>Sau: {log.newData || "—"}</p></details>
    </article>)}
  </State><Pager query={q} page={page} setPage={setPage}/></Page>;
}
