import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { api, Page, Pager, State, useData } from "../components/CommerceUI";
import { postCategory, postDate, postImage, postStatus } from "../components/ShopPostCard";
import { Check, Eye, Pencil, Plus, Save, Search, UploadCloud, X } from "../components/icons";
import { errorMessage } from "../services/apiClient";

export default function AdminPostsPage() {
  const [page, setPage] = useState(0), [keyword, setKeyword] = useState(""), [search, setSearch] = useState("");
  const [status, setStatus] = useState("");
  const [selected, setSelected] = useState(null), [revision, setRevision] = useState(0);
  const [busy, setBusy] = useState(false), [message, setMessage] = useState("");
  const editor = useRef(null);
  const params = new URLSearchParams({ page, size: 12, keyword: search });
  if (status) params.set("status", status);
  const query = useData(`/admin/posts?${params}`);
  function choose(post) { setSelected(post); setRevision(value => value + 1); setMessage(""); }
  useEffect(() => {
    if (revision && window.matchMedia("(max-width: 1000px)").matches)
      editor.current?.scrollIntoView({ behavior: "smooth", block: "start" });
  }, [revision]);

  return <Page title="Bài viết của shop" action={<button disabled={busy} onClick={() => choose(null)}><Plus size={16} />Bài viết mới</button>}>
    <form className="filters" onSubmit={event => { event.preventDefault(); setSearch(keyword.trim()); setPage(0); }}>
      <label>Tìm bài viết<input type="search" value={keyword} maxLength={180} disabled={busy} onChange={event => setKeyword(event.target.value)} /></label>
      <label>Trạng thái bài viết<select value={status} disabled={busy} onChange={event => { setStatus(event.target.value); setPage(0); }}><option value="">Đang quản lý</option><option value="DRAFT">Bản nháp</option><option value="PUBLISHED">Đã đăng</option><option value="ARCHIVED">Đã lưu trữ</option></select></label>
      <button disabled={busy}><Search size={16} />Tìm kiếm</button>
      <Link className="button" to="/news"><Eye size={16} />Xem tin của shop</Link>
    </form>
    <div className="posts-workspace">
      <section aria-label="Danh sách bài viết"><State query={query} empty={!query.data?.content?.length} emptyTitle="Chưa có bài viết" emptyMessage="Không tìm thấy bài viết trong danh sách này.">
        {query.data?.content?.map(post => <div className={`admin-post-row${post.id === selected?.id ? " is-selected" : ""}`} key={post.id}>
          <img src={postImage(post)} alt={post.title} loading="lazy" translate="no" /><div><strong translate="no">{post.title}</strong><small>{postCategory(post.category)} · {postDate(post.updatedAt)}</small><span className={`badge${post.status === "PUBLISHED" ? " badge-active" : ""}`}>{postStatus(post.status)}</span></div>
          <button aria-label={`Sửa ${post.title}`} disabled={busy} onClick={() => choose(post)}><Pencil size={16} /></button>
        </div>)}
      </State><Pager query={query} page={page} setPage={setPage} /></section>
      <section className="post-editor" aria-label="Soạn bài viết" ref={editor}>
        {message && <p role="status" className="notice post-feedback"><Check size={16} />{message}</p>}
        <PostEditor key={`${selected?.id || "new"}-${revision}`} post={selected} busy={busy} setBusy={setBusy} onSaved={(saved, notice) => { setSelected(saved); setMessage(notice); query.reload(); }} />
      </section>
    </div>
  </Page>;
}

function PostEditor({ post, busy, setBusy, onSaved }) {
  const [values, setValues] = useState({ title: post?.title || "", summary: post?.summary || "", content: post?.content || "", category: post?.category || "NEWS" });
  const [cover, setCover] = useState(null), [preview, setPreview] = useState("");
  const [fileKey, setFileKey] = useState(0), [error, setError] = useState("");
  const [confirmation, setConfirmation] = useState("");
  const inFlight = useRef(false);
  useEffect(() => {
    if (!cover) { setPreview(""); return; }
    const url = URL.createObjectURL(cover); setPreview(url);
    return () => URL.revokeObjectURL(url);
  }, [cover]);
  function change(event) { setValues(current => ({ ...current, [event.target.name]: event.target.value })); }
  function chooseImage(event) {
    const file = event.target.files?.[0];
    if (!file) return;
    if (!["image/png", "image/jpeg"].includes(file.type) || file.size === 0 || file.size > 5 * 1024 * 1024) {
      setError("Chỉ hỗ trợ ảnh PNG/JPEG từ 1 byte đến 5 MB."); setCover(null); setFileKey(key => key + 1); return;
    }
    setCover(file); setError("");
  }
  async function save(intent) {
    if (inFlight.current) return;
    inFlight.current = true; setBusy(true); setError("");
    try {
      let saved;
      if (intent === "archive") saved = await api(`/admin/posts/${post.id}`, "delete");
      else {
        const data = new FormData();
        const status = intent === "publish" || intent === "save" && post?.status === "PUBLISHED" ? "PUBLISHED" : "DRAFT";
        data.append("post", new Blob([JSON.stringify({ ...values, status })], { type: "application/json" }));
        if (cover) data.append("cover", cover);
        saved = await api(`/admin/posts${post ? `/${post.id}` : ""}`, post ? "put" : "post", data);
      }
      setCover(null); setFileKey(key => key + 1); setConfirmation("");
      onSaved(saved, saved.status === "PUBLISHED" ? "Đã đăng bài viết lên cửa hàng." : saved.status === "ARCHIVED" ? "Đã lưu trữ bài viết." : "Đã lưu bản nháp.");
    } catch (err) { setError(errorMessage(err)); }
    finally { inFlight.current = false; setBusy(false); }
  }
  return <>
    <div className="post-editor-heading"><h2>{post ? "Sửa bài viết" : "Bài viết mới"}</h2><span className={`badge${post?.status === "PUBLISHED" ? " badge-active" : ""}`}>{postStatus(post?.status || "DRAFT")}</span></div>
    <form className="post-form" onSubmit={event => { event.preventDefault(); save(event.nativeEvent.submitter?.value || "save"); }}>
      <fieldset disabled={busy}>
        <label className="field"><span>Tiêu đề bài viết</span><input name="title" required maxLength={180} value={values.title} onChange={change} /></label>
        <label className="field"><span>Chuyên mục</span><select name="category" value={values.category} onChange={change}><option value="NEWS">Tin của shop</option><option value="COMMUNITY">Cộng đồng DH</option></select></label>
        <div className="post-cover-picker"><img src={preview || postImage(post || {})} alt="Ảnh bìa bài viết" /><label className="field"><span>Ảnh bìa PNG/JPEG, tối đa 5 MB</span><input key={fileKey} type="file" accept="image/png,image/jpeg" onChange={chooseImage} /></label></div>
        <label className="field"><span>Tóm tắt</span><textarea name="summary" required maxLength={400} rows={3} value={values.summary} onChange={change} translate="no" /></label>
        <label className="field"><span>Nội dung bài viết</span><textarea className="post-content-input" name="content" required maxLength={30000} rows={12} value={values.content} onChange={change} translate="no" /></label>
        <div className="post-actions"><button type="submit" value="save"><Save size={16} />{post?.status === "PUBLISHED" ? "Lưu thay đổi" : "Lưu bản nháp"}</button>
          {post?.status !== "PUBLISHED" && <button className="primary" type="submit" value="publish"><UploadCloud size={16} />Đăng bài</button>}
          {post?.status === "PUBLISHED" && <button type="button" onClick={() => setConfirmation("draft")}><X size={16} />Chuyển về bản nháp</button>}
          {post && post.status !== "ARCHIVED" && <button type="button" onClick={() => setConfirmation("archive")}><X size={16} />Lưu trữ</button>}
          {post?.status === "PUBLISHED" && <Link className="button" to={`/news/${post.id}`}><Eye size={16} />Xem bài viết</Link>}
        </div>
        {confirmation && <div className="post-confirmation" role="group" aria-label="Xác nhận gỡ bài viết"><p>{confirmation === "archive" ? "Lưu trữ bài viết và gỡ khỏi cửa hàng?" : "Lưu bản nháp và gỡ bài khỏi cửa hàng?"}</p><div className="post-actions">
          {confirmation === "archive" ? <button type="button" className="danger" onClick={() => save("archive")}>Lưu trữ bài viết</button> : <button type="submit" value="draft">Xác nhận chuyển về bản nháp</button>}
          <button type="button" onClick={() => setConfirmation("")}>Hủy</button>
        </div></div>}
      </fieldset>
    </form>{error && <p role="alert" className="notice error">{error}</p>}
  </>;
}
