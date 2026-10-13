import { Link } from "react-router-dom";
import { ArrowUpRight } from "./icons";
import { media, State, useData } from "./CommerceUI";

export const postCategory = category => category === "COMMUNITY" ? "Cộng đồng DH" : "Tin của shop";
export const postStatus = status => ({ DRAFT: "Bản nháp", PUBLISHED: "Đã đăng", ARCHIVED: "Đã lưu trữ" })[status] || status;
export const postDate = date => date ? new Date(date).toLocaleDateString("vi-VN", { timeZone: "Asia/Ho_Chi_Minh" }) : "";
export const postImage = post => post.coverImageUrl ? media(post.coverImageUrl) : `${import.meta.env.BASE_URL}images/badminton-match-pexels.jpg`;

export function ShopPostCard({ post }) {
  return <article className="community-story shop-story"><Link to={`/news/${post.id}`}>
    <div className="story-image"><img src={postImage(post)} alt={post.title} loading="lazy" translate="no" /></div>
    <div className="story-meta"><span>{postCategory(post.category)}</span><time dateTime={post.publishedAt}>{postDate(post.publishedAt)}</time></div>
    <h3 translate="no">{post.title}</h3><p translate="no">{post.summary}</p><span className="story-read">Đọc bài viết<ArrowUpRight size={16} /></span>
  </Link></article>;
}

export function ShopPostHighlights() {
  const query = useData("/posts?page=0&size=3");
  return <State query={query}>{query.data?.content?.map(post => <ShopPostCard key={post.id} post={post} />)}</State>;
}
