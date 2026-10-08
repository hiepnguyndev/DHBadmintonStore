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
  Field,
  ProductCard,
  Pager,
  Badge,
  money,
  media,
  labelOf,
} from "../components/CommerceUI";
export function ProductListPage() {
  const [page, setPage] = useState(0),
    [filter, setFilter] = useState({});
  const query = useData(
      "/products?" + new URLSearchParams({ ...filter, page, size: 12 }),
    ),
    brands = useData("/brands"),
    cats = useData("/categories");
  return (
    <Page
      title="Tìm dụng cụ cho đường cầu của bạn"
      subtitle="Sản phẩm mới từ cửa hàng. Lọc theo thông số và nhu cầu thi đấu."
      action={
        <Link className="button primary" to="/recommendations">
          Gợi ý dành cho tôi →
        </Link>
      }
    >
      <form
        className="filters"
        onSubmit={(e) => {
          e.preventDefault();
          setPage(0);
          setFilter(
            Object.fromEntries(
              [...new FormData(e.currentTarget)].filter(([, v]) => v !== ""),
            ),
          );
        }}
      >
        <label>
          Tìm kiếm
          <input name="keyword" placeholder="Tên sản phẩm" />
        </label>
        <label>
          Thương hiệu
          <select name="brandId">
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
          <select name="categoryId">
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
          <input name="minPrice" type="number" min="0" />
        </label>
        <label>
          Giá đến
          <input name="maxPrice" type="number" min="0" />
        </label>
        <label>
          Trọng lượng
          <select name="weight">
            <option value="">Tất cả</option>
            {["2U", "3U", "4U", "5U", "6U"].map((v) => (
              <option key={v}>{v}</option>
            ))}
          </select>
        </label>
        <label>
          Độ cứng
          <select name="flex">
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
          <select name="sort">
            <option value="createdAt,desc">Mới nhất</option>
            <option value="price,asc">Giá tăng dần</option>
            <option value="price,desc">Giá giảm dần</option>
          </select>
        </label>
        <button className="primary">Áp dụng</button>
      </form>
      <State query={query} empty={query.data?.content?.length === 0}>
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
export function ProductDetailPage() {
  const { id } = useParams(),
    query = useData("/products/" + id),
    reviews = useData("/products/" + id + "/reviews"),
    { user } = useAuth(),
    [quantity, setQuantity] = useState(1),
    [added, setAdded] = useState(false),
    [saved, setSaved] = useState(false);
  const p = query.data;
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
                <p>{p.description}</p>
                <p>Còn {p.stock} sản phẩm</p>
                {user ? (
                  <>
                    <Field
                      label="Số lượng"
                      type="number"
                      value={quantity}
                      min="1"
                      max={p.stock}
                      onChange={(e) => setQuantity(Number(e.target.value))}
                    />
                    {p.stock > 0 && (
                      <Action
                        onClick={() =>
                          api("/cart/items", "post", {
                            productId: p.id,
                            quantity,
                          })
                        }
                        onDone={() => setAdded(true)}
                      >
                        Thêm vào giỏ
                      </Action>
                    )}
                    <Action
                      onClick={() => api("/favorites/" + p.id, "post")}
                      onDone={() => setSaved(true)}
                    >
                      Yêu thích
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
                <Link className="text-link" to="/recommendations">
                  Vợt này có phù hợp với bạn?
                </Link>
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
          Tiếp tục mua sắm
        </Link>
      }
    >
      <State query={q} empty={!q.data?.items?.length}>
        {q.data?.items?.map((i) => (
          <div className="line-item panel" key={i.cartItemId}>
            <Link to={"/products/" + i.product.id}>
              {i.product.productName}
            </Link>
            <span>{money(i.unitPrice)}</span>
            <div className="quantity">
              <Action
                onClick={() =>
                  api("/cart/items/" + i.cartItemId, "put", {
                    quantity: i.quantity - 1,
                  })
                }
                onDone={q.reload}
              >
                −
              </Action>
              <span>{i.quantity}</span>
              <Action
                onClick={() =>
                  api("/cart/items/" + i.cartItemId, "put", {
                    quantity: i.quantity + 1,
                  })
                }
                onDone={q.reload}
              >
                +
              </Action>
            </div>
            <strong>{money(i.totalPrice)}</strong>
            <Action
              danger
              onClick={() => api("/cart/items/" + i.cartItemId, "delete")}
              onDone={q.reload}
            >
              Xóa
            </Action>
          </div>
        ))}
        {!!q.data?.items?.length && (
          <div className="panel summary">
            <h2>Tạm tính: {money(q.data.subtotalAmount)}</h2>
            <p>Phí giao hàng được xác nhận khi tạo đơn.</p>
            <Link className="button primary" to="/checkout">
              Đến thanh toán →
            </Link>
          </div>
        )}
      </State>
    </Page>
  );
}
export function CheckoutPage() {
  const addresses = useData("/addresses"),
    navigate = useNavigate();
  return (
    <Page
      title="Thanh toán cửa hàng"
      subtitle="Kiểm tra địa chỉ trước khi đặt hàng."
    >
      <State query={addresses}>
        {addresses.data?.length ? (
          <Form
            fields={[
              {
                name: "addressId",
                label: "Địa chỉ nhận hàng",
                options: addresses.data.map((a) => ({
                  value: a.addressId,
                  label:
                    a.receiverName +
                    " — " +
                    a.detailAddress +
                    ", " +
                    a.province,
                })),
              },
              {
                name: "paymentMethod",
                label: "Thanh toán",
                options: [
                  { value: "COD", label: "Thanh toán khi nhận hàng" },
                  {
                    value: "BANK_TRANSFER",
                    label: "Chuyển khoản mô phỏng (đồ án)",
                  },
                ],
              },
              {
                name: "note",
                label: "Ghi chú",
                type: "textarea",
                required: false,
                maxLength: 1000,
              },
            ]}
            initial={{
              addressId: addresses.data[0].addressId,
              paymentMethod: "COD",
              note: "",
            }}
            submit="Đặt hàng"
            onSubmit={(v) =>
              api("/orders", "post", { ...v, addressId: Number(v.addressId) })
            }
            onDone={(d) => navigate("/orders/" + d.order.orderId)}
          />
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
    <Page title={o?.orderCode || "Đơn hàng"}>
      <State query={q}>
        {o && (
          <>
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
            {q.data.items.map((i) => (
              <section className="panel" key={i.orderItemId}>
                <Link to={"/products/" + i.productId}>{i.productName}</Link>
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
