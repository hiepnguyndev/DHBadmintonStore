import { Link } from "react-router-dom";
import { ArrowUpRight, RotateCw, ShieldCheck, Tag, Target, Truck, Users } from "../components/icons";
import { ProductCard, State, useData } from "../components/CommerceUI";
import { ShopPostHighlights } from "../components/ShopPostCard";

const categories = [["Vợt cầu lông", "racket", "01"], ["Giày cầu lông", "shoes", "02"], ["Trang phục", "apparel", "03"], ["Túi & phụ kiện", "accessories", "04"]];
const paths = [[Users, "Marketplace", "Dụng cụ tốt, hành trình mới", "/marketplace", "pink"], [ShieldCheck, "Kiểm định dụng cụ", "An tâm trước mỗi giao dịch", "/my-inspections", "blue"]];
const stories = [
  {
    category: "Tin thi đấu", date: "2026-09-10", dateLabel: "10/09/2026",
    title: "Takahashi và Nakade giành danh hiệu Super 750 đầu tiên",
    summary: "Cặp đôi Nhật Bản tạo dấu ấn tại China Masters 2026 ngay trong lần đầu dự giải Super 750.",
    image: "news-china-masters-2026.jpg",
    href: "https://www.yonex.com/news/badminton/takahashi-and-nakade-complete-rapid-rise-with-first-super-750-title/",
  },
  {
    category: "Tin dụng cụ", date: "2026-09-03", dateLabel: "03/09/2026",
    title: "Bộ sưu tập Viktor Axelsen 2026–2027",
    summary: "Yonex giới thiệu trang phục, giày, túi và phụ kiện trong bộ sưu tập mới lấy cảm hứng từ Viktor Axelsen.",
    image: "news-axelsen-collection-detail-2026.jpg", fit: "contain",
    href: "https://www.yonex.com/news/popular/refine-whats-yours-viktor-axelsen-collection-2026-2027/",
  },
];

export default function HomePage() {
  const products = useData("/products?page=0&size=8");
  const offers = useData("/products?onSale=true&page=0&size=4");
  return <div className="store-home">
    <section className="match-hero"><img className="match-hero-image" src={`${import.meta.env.BASE_URL}images/badminton-player-pexels.jpg`} alt="Người chơi cầu lông trong sân thi đấu" fetchPriority="high" /><div className="hero-inner"><span className="hero-label"><span /> DH BADMINTON / THE COURT EDIT</span><h1>Chọn đúng dụng cụ.<br /><span>Đánh trọn từng điểm.</span></h1><p>Từ cây vợt đầu tiên đến set đấu quyết định,<br />DH giúp bạn vào sân với sự tự tin.</p><div className="hero-actions"><Link to="/products" className="button primary">Khám phá cửa hàng<ArrowUpRight size={18} /></Link><Link className="hero-secondary" to="/promotions">Xem ưu đãi hôm nay<ArrowUpRight size={15} /></Link></div><div className="hero-bottom"><span><ShieldCheck size={15} />CHÍNH HÃNG</span><span><Target size={15} />CHỌN THEO LỐI CHƠI</span><span>DH / 2026</span></div></div></section>
    <section className="home-service-strip" aria-label="Cam kết mua sắm"><div className="content-width">{[[Truck, "Giao hàng toàn quốc", "Theo dõi đơn rõ ràng"], [ShieldCheck, "Sản phẩm chọn lọc", "Thông tin minh bạch"], [RotateCw, "Hỗ trợ sau mua", "Đồng hành cùng bạn"]].map(([Icon, title, text]) => <div className="service-item" key={title}><Icon size={22} /><span><strong>{title}</strong><small>{text}</small></span></div>)}</div></section>
    <section className="home-categories content-width"><div className="section-heading"><div><span className="eyebrow">THE GEAR</span><h2>Chuẩn bị cho trận tiếp theo</h2></div><Link className="text-link" to="/products">Tất cả sản phẩm<ArrowUpRight size={17} /></Link></div><div className="category-grid">{categories.map(([label, category, number]) => <Link key={category} to={`/products?category=${category}`} className="category-item"><div className="category-image"><img src={`${import.meta.env.BASE_URL}images/gear-${category === "accessories" ? "bags" : category}.jpg`} alt={label} loading="lazy" /></div><div><small>{number}</small><h3>{label}</h3><ArrowUpRight size={18} /></div></Link>)}</div></section>
    <section className="home-offers" aria-labelledby="home-offers-title"><div className="content-width">
      <div className="section-heading"><div><span className="eyebrow"><Tag size={14} />ƯU ĐÃI TẠI DH</span><h2 id="home-offers-title">Sản phẩm ưu đãi</h2></div><Link className="text-link" to="/promotions">Tất cả ưu đãi<ArrowUpRight size={17} /></Link></div>
      <State query={offers} empty={!offers.data?.content?.length} emptyTitle="Chưa có sản phẩm ưu đãi" emptyMessage="Ưu đãi mới sẽ được cập nhật tại đây."><div className="product-grid">{offers.data?.content?.map(product => <ProductCard key={product.id} product={product} />)}</div></State>
    </div></section>
    <section className="home-products content-width"><div className="section-heading"><div><span className="eyebrow">CURATED FOR THE COURT</span><h2>Khám phá dụng cụ</h2></div><Link className="text-link" to="/products">Xem cửa hàng<ArrowUpRight size={17} /></Link></div><State query={products} empty={!products.data?.content?.length} emptyTitle="Bộ sưu tập đang được cập nhật" emptyMessage="Các sản phẩm mới sẽ sớm có mặt tại đây."><div className="product-grid">{products.data?.content?.map(product => <ProductCard key={product.id} product={product} />)}</div></State></section>
    <section className="home-paths content-width">{paths.map(([Icon, title, text, to, color]) => <Link to={to} className={`path-item ${color}`} key={to}><Icon size={24} /><span><small>{title}</small><h3>{text}</h3></span><ArrowUpRight size={22} /></Link>)}</section>
    <section className="home-community" aria-labelledby="home-community-title"><div className="content-width">
      <div className="section-heading"><div><span className="eyebrow">BEYOND THE COURT</span><h2 id="home-community-title">Cộng đồng & Tin tức</h2></div><div className="community-links"><Link className="text-link" to="/news">Tin từ shop<ArrowUpRight size={17} /></Link><a className="text-link" href="https://www.yonex.com/news/badminton/" target="_blank" rel="noopener noreferrer">Tin từ Yonex<ArrowUpRight size={17} /></a></div></div>
      <div className="community-grid">
        <ShopPostHighlights />
        <article className="community-story"><Link to="/marketplace"><div className="story-image"><img src={`${import.meta.env.BASE_URL}images/badminton-match-pexels.jpg`} alt="Người chơi cầu lông trên sân" loading="lazy" /></div><div className="story-meta"><span>Cộng đồng DH</span><Users size={15} /></div><h3>Dụng cụ cũ, hành trình mới</h3><p>Khám phá dụng cụ đã qua sử dụng từ người chơi khác, hoặc tìm chủ mới cho cây vợt của bạn.</p><span className="story-read">Khám phá Marketplace<ArrowUpRight size={16} /></span></Link></article>
        {stories.map(story => <article className="community-story" key={story.href}><a href={story.href} target="_blank" rel="noopener noreferrer"><div className="story-image"><img src={`${import.meta.env.BASE_URL}images/${story.image}`} style={{ objectFit: story.fit || "cover" }} alt={story.title} loading="lazy" /></div><div className="story-meta"><span>{story.category}</span><time dateTime={story.date}>{story.dateLabel}</time></div><h3>{story.title}</h3><p>{story.summary}</p><span className="story-read">Đọc tại Yonex<ArrowUpRight size={16} /></span></a></article>)}
      </div>
    </div></section>
  </div>;
}
