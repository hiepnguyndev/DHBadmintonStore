import { useState } from "react";
import { Link, useParams } from "react-router-dom";
import { ArrowLeft } from "../components/icons";
import { Page, Pager, State, useData } from "../components/CommerceUI";
import { postCategory, postDate, postImage, ShopPostCard } from "../components/ShopPostCard";

export function NewsPage() {
  const [page, setPage] = useState(0);
  const [category, setCategory] = useState("");
  const query = useData(`/posts?page=${page}&size=12${category ? `&category=${category}` : ""}`);
  return <Page title="Tin từ DH" action={<Link className="text-link" to="/">Về trang chủ</Link>}>
    <div className="filters news-filters"><label>Chuyên mục<select value={category} onChange={event => { setCategory(event.target.value); setPage(0); }}><option value="">Tất cả</option><option value="NEWS">Tin của shop</option><option value="COMMUNITY">Cộng đồng DH</option></select></label></div>
    <State query={query} empty={!query.data?.content?.length} emptyTitle="Chưa có bài viết" emptyMessage="Tin mới từ DH sẽ được cập nhật tại đây.">
      <div className="community-grid">{query.data?.content?.map(post => <ShopPostCard key={post.id} post={post} />)}</div>
    </State><Pager query={query} page={page} setPage={setPage} />
  </Page>;
}

export function NewsDetailPage() {
  const { id } = useParams();
  const query = useData(`/posts/${id}`);
  const post = query.data;
  return <Page title={post ? <span translate="no">{post.title}</span> : "Bài viết"} action={<Link className="button" to="/news"><ArrowLeft size={16} />Tin từ DH</Link>}>
    <State query={query}><article className="shop-article">
      <div className="article-meta"><span className="badge badge-active">{postCategory(post?.category)}</span><span>DH Badminton Store</span><time dateTime={post?.publishedAt}>{postDate(post?.publishedAt)}</time></div>
      {post && <img className="article-cover" src={postImage(post)} alt={post.title} translate="no" />}
      <p className="article-summary" translate="no">{post?.summary}</p><div className="article-content" translate="no">{post?.content}</div>
    </article></State>
  </Page>;
}
