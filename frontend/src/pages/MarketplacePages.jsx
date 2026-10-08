import { useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
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
function ListingCard({ item }) {
  const l = item.listing;
  return (
    <article className="product-card">
      <Link to={"/marketplace/" + l.listingId}>
        <div className="product-art">
          {item.images[0] ? (
            <img src={media(item.images[0].imageUrl)} alt={l.productName} />
          ) : (
            <span>CHƯA CÓ ẢNH</span>
          )}
          {item.verified && <strong className="score">Đã kiểm định</strong>}
        </div>
        <div className="product-copy">
          <h3>{l.productName}</h3>
          <p className="price">{money(l.sellingPrice)}</p>
          <p>
            Tình trạng {l.conditionGrade} · {l.usageMonths} tháng sử dụng
          </p>
          <Badge value={l.status} />
          <p className="muted">{item.seller.name}</p>
        </div>
      </Link>
    </article>
  );
}
export function MarketplacePage({ mine = false }) {
  const [page, setPage] = useState(0),
    [filters, setFilters] = useState({}),
    q = useData(
      (mine ? "/marketplace/my-listings" : "/marketplace/listings") +
        "?" +
        new URLSearchParams({ ...filters, page, size: 12 }),
    );
  return (
    <Page
      title={
        mine ? "Tin đăng của tôi" : "Sân chơi mới cho dụng cụ đã qua sử dụng"
      }
      subtitle="Xem tình trạng, báo cáo kiểm định và uy tín người bán trước khi giao dịch."
      action={
        <Link className="button primary" to="/sell">
          Đăng bán dụng cụ →
        </Link>
      }
    >
      {!mine && (
        <form
          className="filters"
          onSubmit={(e) => {
            e.preventDefault();
            setPage(0);
            setFilters(
              Object.fromEntries(
                [...new FormData(e.currentTarget)].filter(([, v]) => v !== ""),
              ),
            );
          }}
        >
          <label>
            Tìm kiếm
            <input name="keyword" placeholder="Tên vợt hoặc dụng cụ" />
          </label>
          <label>
            Tình trạng
            <select name="conditionGrade">
              <option value="">Tất cả</option>
              {["S", "A", "B", "C", "D"].map((v) => (
                <option key={v}>{v}</option>
              ))}
            </select>
          </label>
          <label>
            Kiểm định
            <select name="verified">
              <option value="">Tất cả</option>
              <option value="true">Đã kiểm định</option>
            </select>
          </label>
          <label>
            Giá tối đa
            <input name="maxPrice" type="number" min="0" />
          </label>
          <button className="primary">Tìm tin</button>
        </form>
      )}
      <State query={q} empty={!q.data?.content?.length}>
        <div className="product-grid">
          {q.data?.content?.map((l) => (
            <ListingCard key={l.listing.listingId} item={l} />
          ))}
        </div>
      </State>
      <Pager query={q} page={page} setPage={setPage} />
    </Page>
  );
}
export function ListingDetailPage() {
  const { id } = useParams(),
    q = useData("/marketplace/listings/" + id),
    { user } = useAuth(),
    navigate = useNavigate(),
    item = q.data,
    l = item?.listing;
  return (
    <Page title={l?.productName || "Tin đăng"}>
      <State query={q}>
        {l && (
          <>
            <div className="detail-grid">
              <div className="gallery">
                {item.images.map((i) => (
                  <img
                    key={i.imageId}
                    src={media(i.imageUrl)}
                    alt={l.productName}
                  />
                ))}
              </div>
              <section className="panel">
                <Badge value={l.status} />
                {item.verified && (
                  <span className="badge verified">
                    Đã kiểm định · còn hiệu lực
                  </span>
                )}
                <p className="price large">{money(l.sellingPrice)}</p>
                <p>{l.description}</p>
                <p>
                  Model: {l.model} · Tình trạng: {l.conditionGrade}
                </p>
                <p>Đã sử dụng {l.usageMonths} tháng</p>
                <p>
                  Nguồn tình trạng:{" "}
                  {item.conditionSource === "INSPECTION"
                    ? "Kiểm định viên"
                    : "Người bán khai báo"}
                </p>
                <Link to={"/sellers/" + item.seller.id}>
                  Người bán: {item.seller.name} →
                </Link>
                {user?.id === l.sellerId ? (
                  <div className="actions">
                    {["DRAFT", "REJECTED", "AVAILABLE"].includes(l.status) && (
                      <Link className="button" to={"/sell/" + id}>
                        Sửa tin
                      </Link>
                    )}
                    {["DRAFT", "REJECTED"].includes(l.status) && (
                      <Action
                        onClick={() =>
                          api("/marketplace/listings/" + id + "/submit", "post")
                        }
                        onDone={q.reload}
                      >
                        Gửi duyệt
                      </Action>
                    )}
                    <Action
                      onClick={() =>
                        api("/inspections", "post", { listingId: Number(id) })
                      }
                      onDone={() => navigate("/my-inspections")}
                    >
                      Yêu cầu kiểm định
                    </Action>

                  </div>
                ) : l.status === "AVAILABLE" ? (
                  <Link
                    className="button primary"
                    to={user ? "/marketplace/" + id + "/buy" : "/login"}
                  >
                    Mua qua Escrow mô phỏng
                  </Link>
                ) : null}
              </section>
            </div>
            {user?.id === l.sellerId && <ValuationPanel listingId={Number(id)} />}
            {item.specification && (
              <section className="panel">
                <h2>Thông tin dụng cụ</h2>
                <dl className="spec-grid">
                  {[
                    ["Trọng lượng", "weightClass"],
                    ["Cán", "gripSize"],
                    ["Cước", "stringModel"],
                    ["Sức căng (lbs)", "stringTensionLbs"],
                    ["Khung", "frameCondition"],
                    ["Sơn", "paintCondition"],
                    ["Gen", "grommetCondition"],
                    ["Tay cầm", "handleCondition"],
                    ["Sửa chữa", "repairHistory"],
                  ].map(([label, key]) => (
                    <div key={key}>
                      <dt>{label}</dt>
                      <dd>{item.specification[key] || "—"}</dd>
                    </div>
                  ))}
                </dl>
              </section>
            )}
            {item.inspectionReport && (
              <section className="panel">
                <h2>Báo cáo kiểm định</h2>
                <p>Điểm tình trạng {item.inspectionReport.overallScore}/10</p>
                <p>{item.inspectionReport.notes}</p>
                <p>
                  Giá tham khảo{" "}
                  {money(item.inspectionReport.recommendedMinPrice)} –{" "}
                  {money(item.inspectionReport.recommendedMaxPrice)}
                </p>
              </section>
            )}
          </>
        )}
      </State>
    </Page>
  );
}
const fields = [
  { name: "productName", label: "Tên sản phẩm", maxLength: 255 },
  { name: "model", label: "Model", maxLength: 150 },
  {
    name: "description",
    label: "Mô tả tình trạng",
    type: "textarea",
    maxLength: 20000,
  },
  {
    name: "originalPrice",
    label: "Giá mua ban đầu (₫)",
    type: "number",
    min: 0,
  },
  { name: "sellingPrice", label: "Giá bán (₫)", type: "number", min: 1 },
  { name: "purchaseDate", label: "Ngày mua", type: "date", required: false },
  {
    name: "usageMonths",
    label: "Số tháng sử dụng",
    type: "number",
    min: 0,
    max: 1200,
  },
  {
    name: "conditionGrade",
    label: "Mức tình trạng tự đánh giá",
    options: ["S", "A", "B", "C", "D"],
  },
  { name: "warrantyStatus", label: "Thông tin bảo hành", maxLength: 30 },
  {
    name: "warrantyUntil",
    label: "Bảo hành đến",
    type: "date",
    required: false,
  },
];
export function SellListingPage() {
  const { id } = useParams(),
    cats = useData("/categories"),
    brands = useData("/brands");
  return (
    <Page
      title={id ? "Chỉnh sửa tin đăng" : "Đăng bán dụng cụ"}
      subtitle="Thông tin và ảnh phải phản ánh đúng tình trạng thực tế. Tin được duyệt trước khi hiển thị."
    >
      {cats.data &&
        brands.data &&
        (id ? (
          <EditListing id={id} cats={cats.data} brands={brands.data} />
        ) : (
          <ListingForm cats={cats.data} brands={brands.data} />
        ))}
    </Page>
  );
}
function EditListing({ id, cats, brands }) {
  const q = useData("/marketplace/listings/" + id);
  return (
    <State query={q}>
      {q.data && (
        <ListingForm id={id} current={q.data} cats={cats} brands={brands} />
      )}
    </State>
  );
}
const usedFields = [
  {
    name: "weightClass",
    label: "Trọng lượng vợt",
    options: ["2U", "3U", "4U", "5U", "6U"],
  },
  { name: "gripSize", label: "Cỡ cán", required: false, maxLength: 20 },
  { name: "stringModel", label: "Loại cước", required: false, maxLength: 100 },
  {
    name: "stringTensionLbs",
    label: "Sức căng (lbs)",
    type: "number",
    required: false,
    min: 1,
    max: 99,
  },
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
      "Tình trạng cán",
    ][index],
    maxLength: 50,
  })),
  {
    name: "repairHistory",
    label: "Lịch sử sửa chữa",
    type: "textarea",
    required: false,
    maxLength: 5000,
  },
  { name: "serialNumber", label: "Serial", required: false, maxLength: 100 },
];
function ListingForm({ id, current, cats, brands }) {
  const navigate = useNavigate();
  const initial = current
    ? {
        ...current.listing,
        ...current.specification,
        isRacket: current.specification ? "yes" : "no",
      }
    : {
        categoryId: cats[0]?.categoryId,
        brandId: brands[0]?.brandId,
        conditionGrade: "A",
        usageMonths: 0,
        warrantyStatus: "UNKNOWN",
        originalPrice: 0,
        sellingPrice: 1000000,
        isRacket: "yes",
        weightClass: "4U",
        frameCondition: "Chưa ghi nhận",
        paintCondition: "Chưa ghi nhận",
        grommetCondition: "Chưa ghi nhận",
        handleCondition: "Chưa ghi nhận",
      };
  return (
    <>
      <Form
        key={id || "new"}
        fields={[
          {
            name: "categoryId",
            label: "Danh mục",
            options: cats.map((c) => ({
              value: c.categoryId,
              label: c.categoryName,
            })),
          },
          {
            name: "brandId",
            label: "Thương hiệu",
            options: brands.map((b) => ({
              value: b.brandId,
              label: b.brandName,
            })),
          },
          ...fields,
          {
            name: "isRacket",
            label: "Loại dụng cụ",
            options: [
              { value: "yes", label: "Vợt cầu lông" },
              { value: "no", label: "Dụng cụ khác" },
            ],
          },
          ...usedFields.map((f) => ({ ...f, required: false })),
        ]}
        initial={initial}
        submit={id ? "Lưu thay đổi" : "Tạo bản nháp và thêm ảnh"}
        onSubmit={(v) =>
          api(
            "/marketplace/listings" + (id ? "/" + id : ""),
            id ? "put" : "post",
            {
              ...Object.fromEntries(
                fields.map((f) => [f.name, v[f.name] || null]),
              ),
              categoryId: Number(v.categoryId),
              brandId: Number(v.brandId),
              originalPrice: Number(v.originalPrice),
              sellingPrice: Number(v.sellingPrice),
              usageMonths: Number(v.usageMonths),
              racketSpecification:
                v.isRacket === "yes"
                  ? Object.fromEntries(
                      usedFields.map((f) => [f.name, v[f.name] || null]),
                    )
                  : null,
            },
          )
        }
        onDone={(d) => navigate("/sell/" + d.listing.listingId)}
      />
      {id && (
        <section className="panel">
          <h2>Ảnh dụng cụ</h2>
          <p>
            Tải ảnh trước khi gửi duyệt. Thay ảnh sẽ yêu cầu duyệt lại và kiểm
            định lại.
          </p>
          <Upload path={"/marketplace/listings/" + id + "/images"} />
          <Link className="button" to={"/marketplace/" + id}>
            Xem tin và gửi duyệt
          </Link>
        </section>
      )}
    </>
  );
}
export function MarketBuyPage() {
  const { id } = useParams(),
    q = useData("/addresses"),
    listing = useData("/marketplace/listings/" + id),
    navigate = useNavigate();
  return (
    <Page title="Xác nhận mua dụng cụ">
      <p>
        Giá tin đăng: {money(listing.data?.listing.sellingPrice)}. Thanh toán và
        giữ tiền được mô phỏng trong đồ án.
      </p>
      <State query={q}>
        {q.data?.length ? (
          <Form
            fields={[
              {
                name: "addressId",
                label: "Địa chỉ nhận hàng",
                options: q.data.map((a) => ({
                  value: a.addressId,
                  label:
                    a.receiverName +
                    " — " +
                    a.detailAddress +
                    ", " +
                    a.province,
                })),
              },
            ]}
            initial={{ addressId: q.data[0].addressId }}
            submit="Tạo đơn Marketplace"
            onSubmit={(v) =>
              api("/market-orders", "post", {
                listingId: Number(id),
                addressId: Number(v.addressId),
                paymentMethod: "ESCROW_SIMULATION",
              })
            }
            onDone={(r) => navigate("/market-orders/" + r.order.marketOrderId)}
          />
        ) : (
          <Link to="/addresses">Thêm địa chỉ nhận hàng</Link>
        )}
      </State>
    </Page>
  );
}
export function MarketOrdersPage() {
  const [selling, setSelling] = useState(false),
    [page, setPage] = useState(0),
    q = useData(
      "/market-orders/" + (selling ? "selling" : "buying") + "?page=" + page,
    );
  return (
    <Page title="Giao dịch Marketplace">
      <div className="tabs">
        <button
          className={!selling ? "active" : ""}
          onClick={() => {
            setSelling(false);
            setPage(0);
          }}
        >
          Tôi mua
        </button>
        <button
          className={selling ? "active" : ""}
          onClick={() => {
            setSelling(true);
            setPage(0);
          }}
        >
          Tôi bán
        </button>
      </div>
      <State query={q} empty={!q.data?.content?.length}>
        {q.data?.content?.map((o) => (
          <Link
            className="panel line-item"
            key={o.marketOrderId}
            to={"/market-orders/" + o.marketOrderId}
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
export function MarketOrderDetailPage() {
  const { id } = useParams(),
    q = useData("/market-orders/" + id),
    { user } = useAuth(),
    navigate = useNavigate(),
    o = q.data?.order,
    buyer = user?.id === o?.buyerId;
  return (
    <Page title={o?.orderCode || "Giao dịch Marketplace"}>
      <State query={q}>
        {o && (
          <>
            <section className="panel">
              <Badge value={o.orderStatus} />
              <Badge value={q.data.escrow.status} />
              <p>
                {o.receiverName} · {o.receiverPhone}
              </p>
              <p>{o.shippingAddress}</p>
              <Link to={"/marketplace/" + o.listingId}>Xem dụng cụ</Link>
              <p>
                Giá sản phẩm {money(o.itemPrice)} · Vận chuyển{" "}
                {money(o.shippingFee)} · Phí nền tảng {money(o.platformFee)}
              </p>
              <h2>Tổng: {money(o.totalAmount)}</h2>
              {buyer && o.orderStatus === "PENDING_PAYMENT" && (
                <div className="actions">
                  <Action
                    onClick={() => api("/escrow/" + id + "/pay", "post")}
                    onDone={q.reload}
                  >
                    Thanh toán mô phỏng
                  </Action>
                  <Action onClick={() => api("/market-orders/" + id + "/cancel", "post")} onDone={q.reload}>Hủy đơn chưa thanh toán</Action>
                  <p>Đơn chưa thanh toán tự hết hạn sau thời gian giữ chỗ.</p>
                </div>
              )}
              {buyer && o.orderStatus === "DELIVERED" && (
                <Action
                  onClick={() =>
                    api("/market-orders/" + id + "/confirm-received", "post")
                  }
                  onDone={q.reload}
                >
                  Đã nhận hàng — hoàn tất giao dịch
                </Action>
              )}
              {q.data.shipment && (
                <p>
                  Vận đơn {q.data.shipment.carrier}:{" "}
                  {q.data.shipment.trackingCode} ·{" "}
                  <Badge value={q.data.shipment.shippingStatus} />
                </p>
              )}
            </section>
            {!buyer && o.orderStatus === "WAITING_SHIPMENT" && (
              <section className="panel">
                <h2>Gửi hàng cho người mua</h2>
                <Form
                  fields={[
                    { name: "carrier", label: "Đơn vị vận chuyển" },
                    { name: "trackingCode", label: "Mã vận đơn" },
                  ]}
                  onSubmit={(v) =>
                    api("/shipments", "post", {
                      ...v,
                      marketOrderId: Number(id),
                    })
                  }
                  onDone={q.reload}
                  submit="Tạo vận đơn"
                />
              </section>
            )}
            {q.data.disputeId ? (
              <Link className="button" to={"/disputes/" + q.data.disputeId}>
                Xem tranh chấp
              </Link>
            ) : (
              ["SHIPPING", "DELIVERED"].includes(o.orderStatus) && (
                <section className="panel">
                  <h2>Báo cáo vấn đề</h2>
                  <Form
                    fields={[
                      {
                        name: "reason",
                        label: "Lý do",
                        options: (o.orderStatus === "SHIPPING"
                          ? ["NOT_RECEIVED"]
                          : [
                              "NOT_AS_DESCRIBED",
                              "DAMAGED",
                              "SUSPECTED_FAKE",
                              "MISSING_ACCESSORIES",
                              "WRONG_ITEM",
                              "NOT_RECEIVED",
                              "OTHER",
                            ]
                        ).map((v) => ({
                          value: v,
                          label: {
                            NOT_RECEIVED: "Chưa nhận hàng",
                            NOT_AS_DESCRIBED: "Không đúng mô tả",
                            DAMAGED: "Hư hỏng",
                            SUSPECTED_FAKE: "Nghi hàng giả",
                            MISSING_ACCESSORIES: "Thiếu phụ kiện",
                            WRONG_ITEM: "Sai sản phẩm",
                            OTHER: "Khác",
                          }[v],
                        })),
                      },
                      {
                        name: "description",
                        label: "Mô tả chi tiết",
                        type: "textarea",
                        maxLength: 5000,
                      },
                    ]}
                    initial={{
                      reason:
                        o.orderStatus === "SHIPPING"
                          ? "NOT_RECEIVED"
                          : "NOT_AS_DESCRIBED",
                    }}
                    submit="Mở tranh chấp"
                    onSubmit={(v) =>
                      api("/disputes", "post", {
                        ...v,
                        marketOrderId: Number(id),
                      })
                    }
                    onDone={(d) => navigate("/disputes/" + d.disputeId)}
                  />
                </section>
              )
            )}
            {buyer && o.orderStatus === "COMPLETED" && !q.data.rated && (
              <section className="panel">
                <h2>Đánh giá người bán</h2>
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
                    },
                  ]}
                  initial={{ rating: 5, comment: "" }}
                  onSubmit={(v) =>
                    api("/seller-ratings", "post", {
                      ...v,
                      rating: Number(v.rating),
                      marketOrderId: Number(id),
                    })
                  }
                  onDone={q.reload}
                />
              </section>
            )}
          </>
        )}
      </State>
    </Page>
  );
}
export function DisputeDetailPage() {
  const { id } = useParams(),
    q = useData("/disputes/" + id),
    d = q.data?.dispute;
  return (
    <Page title="Chi tiết tranh chấp">
      <State query={q}>
        {d && (
          <>
            <section className="panel">
              <Badge value={d.status} />
              <p>{d.description}</p>
              {d.resolution && (
                <p>
                  Kết quả: {d.resolution} — {d.resolutionNote}
                </p>
              )}
              <Link to={"/market-orders/" + d.marketOrderId}>
                Xem giao dịch
              </Link>
            </section>
            <section className="panel">
              <h2>Bằng chứng</h2>
              <div className="gallery">
                {q.data.evidences.map((e) => (
                  <figure key={e.evidenceId}>
                    <img
                      src={e.fileUrl}
                      alt={e.description || "Bằng chứng tranh chấp"}
                    />
                    <figcaption>{e.description}</figcaption>
                  </figure>
                ))}
              </div>
              {!["RESOLVED", "REJECTED"].includes(d.status) && (
                <Upload
                  path={"/disputes/" + id + "/evidences"}
                  onDone={q.reload}
                />
              )}
            </section>
          </>
        )}
      </State>
    </Page>
  );
}
export function SellerProfilePage() {
  const { id } = useParams(),
    q = useData("/sellers/" + id),
    p = q.data;
  return (
    <Page title={p?.fullName || "Hồ sơ người bán"}>
      <State query={q}>
        {p && (
          <section className="panel">
            <h2>
              {p.reputation.newSeller
                ? "Người bán mới"
                : p.reputation.reputationScore + "/100 điểm uy tín"}
            </h2>
            <p>
              {p.completedTransactions} giao dịch hoàn thành · {p.ratingCount}{" "}
              đánh giá
            </p>
            <p>Điểm đánh giá: {p.averageRating ?? "Chưa có dữ liệu"}</p>
            <p>
              Tỷ lệ tranh chấp:{" "}
              {p.reputation.disputeRate == null
                ? "Chưa có dữ liệu"
                : p.reputation.disputeRate + "%"}
            </p>
            <p>
              Tỷ lệ hủy do người bán:{" "}
              {p.reputation.cancellationRate == null
                ? "Chưa có dữ liệu"
                : p.reputation.cancellationRate + "%"}
            </p>
            <p>Tham gia {new Date(p.joinedAt).toLocaleDateString("vi-VN")}</p>
          </section>
        )}
      </State>
    </Page>
  );
}
export function NotificationsPage() {
  const [page, setPage] = useState(0),
    q = useData("/notifications?page=" + page);
  return (
    <Page
      title="Thông báo"
      action={
        <Action
          onClick={() => api("/notifications/read-all", "patch")}
          onDone={q.reload}
        >
          Đánh dấu tất cả đã đọc
        </Action>
      }
    >
      <State query={q} empty={!q.data?.content?.length}>
        {q.data?.content?.map((n) => (
          <article className="panel" key={n.notificationId}>
            <h3>{n.title}</h3>
            <p>{n.content}</p>
            {!n.isRead && (
              <Action
                onClick={() =>
                  api("/notifications/" + n.notificationId + "/read", "patch")
                }
                onDone={q.reload}
              >
                Đã đọc
              </Action>
            )}
          </article>
        ))}
      </State>
      <Pager query={q} page={page} setPage={setPage} />
    </Page>
  );
}

function ValuationPanel({ listingId }) {
  const q = useData("/valuations/listing/" + listingId);
  return <section className="panel">
    <h2>Ước tính giá dụng cụ</h2>
    <p>Khai báo tình trạng thực tế. Kết quả là giá tham khảo; báo cáo kiểm định hợp lệ được ưu tiên.</p>
    <Form fields={[
      {name:"accessoryCondition",label:"Tình trạng phụ kiện",options:[{value:"FULL",label:"Đầy đủ"},{value:"PARTIAL",label:"Thiếu một phần"},{value:"MANY_MISSING",label:"Thiếu nhiều"}]},
      {name:"repairLevel",label:"Mức sửa chữa",options:[{value:"NONE",label:"Chưa sửa chữa"},{value:"MINOR",label:"Sửa chữa nhẹ"},{value:"STRUCTURAL",label:"Sửa chữa kết cấu"}]}
    ]} initial={{accessoryCondition:"",repairLevel:""}} submit="Ước tính giá" onSubmit={v=>api("/valuations","post",{listingId,...v})} onDone={q.reload} />
    <State query={q}>{q.data?.map(v=><article className="panel" key={v.valuationId}>
      <strong>Giá ước tính: {money(v.estimatedPrice)}</strong>
      <p>Khoảng gợi ý: {money(v.suggestedMinPrice)} – {money(v.suggestedMaxPrice)}</p>
      <p>Giá tham chiếu: {money(v.referencePrice)} · {v.createdAt}</p>
      <details><summary>Chi tiết tính giá</summary><p>Tuổi: {v.ageFactor} · Tình trạng: {v.conditionFactor} · Bảo hành: {v.warrantyFactor} · Phụ kiện: {v.accessoryFactor} · Sửa chữa: {v.repairFactor} · Nhu cầu: {v.demandFactor}</p><p>{v.note}</p></details>
    </article>)}</State>
  </section>;
}
