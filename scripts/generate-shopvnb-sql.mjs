import { readFile, writeFile, mkdir } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { createHash } from "node:crypto";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const source = path.resolve(process.argv[2] || path.join(root, "../CSDL/san_pham_shopvnb_200.json"));
const output = path.resolve(process.argv[3] || path.join(root, "database/04_shopvnb_200.sql"));
const items = JSON.parse(await readFile(source, "utf8"));
const categories = {
  vot: ["Vợt cầu lông", "racket"], giay: ["Giày cầu lông", "shoes"],
  ao: ["Trang phục", "apparel"], quan: ["Trang phục", "apparel"],
  tui: ["Túi & phụ kiện", "accessories"], balo: ["Túi & phụ kiện", "accessories"], phu_kien: ["Túi & phụ kiện", "accessories"],
};
const labels = {
  do_cung: "Độ cứng", vat_lieu_khung: "Vật liệu khung", vat_lieu_than: "Vật liệu thân",
  trong_luong: "Trọng lượng", diem_can_bang: "Điểm cân bằng", chieu_dai: "Chiều dài",
  swingweight: "Swingweight", chu_vi_can: "Chu vi cán", suc_cang: "Sức căng",
  chat_lieu: "Chất liệu", loai_ao: "Loại áo", kich_thuoc: "Kích thước", so_ngan: "Số ngăn", mau_sac_text: "Thông tin màu sắc",
};
const hash = text => createHash("sha256").update(text, "utf8").digest("hex");
// Hex literals remain correct regardless of MySQL NO_BACKSLASH_ESCAPES mode.
const sqlText = text => text == null ? "NULL" : `CONVERT(0x${Buffer.from(String(text), "utf8").toString("hex")} USING utf8mb4)`;
const number = value => {
  if (value == null) return "NULL";
  if (!Number.isSafeInteger(value) || value <= 0 || value > 9999999999999) throw new Error(`Invalid source price: ${value}`);
  return String(value);
};
if (!Array.isArray(items) || items.length !== 200) throw new Error("Expected exactly 200 source products");
const urls = new Set(), names = new Set();
const rows = items.map(item => {
  const url = new URL(item.url_nguon);
  if (url.protocol !== "https:" || url.hostname !== "shopvnb.com" || urls.has(item.url_nguon)) throw new Error("Invalid or duplicate source URL");
  urls.add(item.url_nguon);
  if (!item.ten_san_pham?.trim() || item.ten_san_pham.length > 255 || !item.thuong_hieu?.trim() || item.thuong_hieu.length > 100) throw new Error("Invalid name/brand");
  const nameKey = `${categories[item.danh_muc]?.[1]}|${item.thuong_hieu}|${item.ten_san_pham}`.normalize('NFC').toLowerCase();
  if (names.has(nameKey)) throw new Error("Duplicate source product name requires review");
  names.add(nameKey);
  const category = categories[item.danh_muc];
  if (!category) throw new Error(`Unsupported category: ${item.danh_muc}`);
  for (const key of ["mau_sac", "kich_co", "anh_san_pham"]) if (!Array.isArray(item[key])) throw new Error(`Expected array: ${key}`);
  for (const image of item.anh_san_pham) {
    const imageUrl = new URL(image);
    if (imageUrl.protocol !== "https:" || imageUrl.hostname !== "cdn.shopvnb.com" || image.length > 500) throw new Error("Invalid source image");
  }
  const key = hash(item.url_nguon);
  const lines = [item.mo_ta_ngan];
  if (item.gia_thap_nhat == null) lines.push("Chưa có giá xác minh. Cần admin nhập giá trước khi mở bán.");
  else if (item.gia_cao_nhat !== item.gia_thap_nhat) lines.push(`Khoảng giá nguồn: ${item.gia_thap_nhat} - ${item.gia_cao_nhat} VND; giá nhập là mức thấp nhất.`);
  for (const [attribute, value] of Object.entries(item.thong_so || {})) lines.push(`${labels[attribute] || attribute}: ${value}`);
  number(item.gia_thap_nhat);
  number(item.gia_cao_nhat);
  return { source_key: key, source_url: item.url_nguon, source_code: item.ma_nguon,
    product_name: item.ten_san_pham, product_slug: `shopvnb-${key}`, brand_name: item.thuong_hieu,
    brand_slug: `shopvnb-brand-${hash(item.thuong_hieu).slice(0, 20)}`, category_name: category[0], category_slug: category[1],
    price: item.gia_thap_nhat, description: lines.filter(Boolean).join("\n"), thumbnail_url: item.anh_san_pham[0] || null, raw_record: item };
});
const template = await readFile(path.join(root, "database/templates/shopvnb_import.sql"), "utf8");
await mkdir(path.dirname(output), { recursive: true });
const stage = `JSON_TABLE(@dh_source, '$[*]' COLUMNS (
    source_key CHAR(64) PATH '$.source_key', source_url VARCHAR(768) PATH '$.source_url', source_code VARCHAR(80) PATH '$.source_code',
    product_name VARCHAR(255) PATH '$.product_name', product_slug VARCHAR(255) PATH '$.product_slug',
    brand_name VARCHAR(100) PATH '$.brand_name', brand_slug VARCHAR(100) PATH '$.brand_slug',
    category_name VARCHAR(100) PATH '$.category_name', category_slug VARCHAR(100) PATH '$.category_slug',
    price DECIMAL(15,2) PATH '$.price', description TEXT PATH '$.description', thumbnail_url VARCHAR(500) PATH '$.thumbnail_url',
    raw_record JSON PATH '$.raw_record'))`;
const map = `JSON_TABLE(@dh_map, '$[*]' COLUMNS (source_key CHAR(64) PATH '$.source_key',
    product_id BIGINT PATH '$.product_id', brand_id BIGINT PATH '$.brand_id', category_id BIGINT PATH '$.category_id'))`;
await writeFile(output, template.replace("{{SOURCE_JSON}}", sqlText(JSON.stringify(rows)))
  .replaceAll("{{STAGE_TABLE}}", stage).replaceAll("{{MAP_TABLE}}", map), "utf8");
console.log(JSON.stringify({ output, products: items.length, missingPrice: items.filter(item => item.gia_thap_nhat == null).length,
  withImages: items.filter(item => item.anh_san_pham.length > 0).length, initialStock: 20 }));
