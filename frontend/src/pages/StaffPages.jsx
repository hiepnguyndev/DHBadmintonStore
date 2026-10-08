import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
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
  money,
  media,
} from "../components/CommerceUI";
import { useAuth } from "../context/AuthContext";
import { errorMessage } from "../services/apiClient";
export function AdminPage() {
  const q = useData("/admin/statistics/overview");
  return (
    <Page title="Tổng quan quản trị" subtitle="Theo dõi và điều hành hoạt động của cửa hàng.">
      <State query={q}>
        <div className="stats-grid">
          {Object.entries(q.data || {}).map(([key, v]) => (
            <div className="panel" key={key}>
              <p>
                {
                  {
                    shopRevenue: "Doanh thu cửa hàng",
                    shopOrders: "Đơn cửa hàng",
                    marketplaceOrders: "Giao dịch Marketplace",
                    users: "Người dùng",
                    products: "Sản phẩm",
                    activeListings: "Tin đang bán",
                    pendingInspections: "Kiểm định đang chờ",
                    openDisputes: "Tranh chấp đang mở",
                  }[key]
                }
              </p>
              <strong>{key === "shopRevenue" ? money(v) : v}</strong>
            </div>
          ))}
        </div>
      </State>
    </Page>
  );
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
    q = useData("/products?page=" + page),
    [selected, setSelected] = useState(null);
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
      action={<Link to="/admin">Về quản trị</Link>}
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
          <State query={q}>
            {q.data?.content?.map((p) => (
              <div className="panel line-item" key={p.id}>
                <strong>{p.productName}</strong>
                <span>{money(p.effectivePrice)}</span>
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
export function AdminOrdersPage() {
  const [page, setPage] = useState(0),
    q = useData("/admin/orders?page=" + page);
  const next = {
    PENDING: "CONFIRMED",
    CONFIRMED: "PREPARING",
    PREPARING: "SHIPPING",
    SHIPPING: "COMPLETED",
  };
  return (
    <Page title="Xử lý đơn cửa hàng">
      <State query={q}>
        {q.data?.content?.map((o) => (
          <div className="panel line-item" key={o.orderId}>
            <Link to={"/orders/" + o.orderId}>{o.orderCode}</Link>
            <Badge value={o.orderStatus} />
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
      <State query={q}>
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
        <State query={products}>
          {products.data?.map((p) => (
            <p key={p.productId}>
              {p.productName} · {p.quantity} sản phẩm · {money(p.revenue)}
            </p>
          ))}
        </State>
      </section>
      <section className="panel">
        <h2>Người bán có nhiều giao dịch hoàn thành</h2>
        <State query={sellers}>
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
  return <Page title="Nhật ký hệ thống"><State query={q}>
    {q.data?.content?.map(log=><article className="panel" key={log.auditId}>
      <strong>{log.action} · {log.entityType} #{log.entityId}</strong>
      <p>{log.createdAt} · {log.userId ? "Người dùng #"+log.userId : "Hệ thống"}</p>
      <details><summary>Dữ liệu thay đổi</summary><p>Trước: {log.oldData || "—"}</p><p>Sau: {log.newData || "—"}</p></details>
    </article>)}
  </State><Pager query={q} page={page} setPage={setPage}/></Page>;
}
