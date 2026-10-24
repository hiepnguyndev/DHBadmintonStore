import { Link } from "react-router-dom";
import { useChatbot } from "../context/ChatbotContext";
import { ArrowUpRight, SlidersHorizontal, Sparkles, Target } from "../components/icons";
import {
  api,
  useData,
  Page,
  State,
  Form,
  ProductCard,
} from "../components/CommerceUI";
export const playerFields = [
  {
    name: "skillLevel",
    label: "Trình độ",
    options: ["BEGINNER", "INTERMEDIATE", "ADVANCED"],
  },
  {
    name: "playStyle",
    label: "Lối chơi",
    options: ["ATTACK", "DEFENSE", "BALANCED", "SPEED", "CONTROL"],
  },
  {
    name: "playType",
    label: "Hình thức chơi",
    options: ["SINGLES", "DOUBLES", "BOTH"],
  },
  {
    name: "wristStrength",
    label: "Lực cổ tay",
    options: ["WEAK", "MEDIUM", "STRONG"],
  },
  { name: "dominantHand", label: "Tay thuận", options: ["RIGHT", "LEFT"] },
  {
    name: "preferredWeight",
    label: "Trọng lượng ưa thích",
    options: ["2U", "3U", "4U", "5U", "6U"],
  },
  { name: "balanceType", label: "Điểm cân bằng ưa thích", options: ["HEAD_HEAVY", "EVEN_BALANCE", "HEAD_LIGHT"] },
  { name: "shaftFlexibility", label: "Độ cứng thân vợt", options: ["FLEXIBLE", "MEDIUM", "STIFF", "EXTRA_STIFF"] },
  { name: "shirtSize", label: "Size áo", options: ["XS", "S", "M", "L", "XL", "XXL"] },
  { name: "pantsSize", label: "Size quần", options: ["XS", "S", "M", "L", "XL", "XXL"] },
  { name: "shoeSize", label: "Size giày EU", options: Array.from({length:14},(_,i)=>({value:String(i+35),label:String(i+35)})) },
  { name: "favoriteColors", label: "Màu sắc yêu thích", required: false },
  { name: "favoriteBrands", label: "Thương hiệu yêu thích", required: false },
  { name: "minBudget", label: "Ngân sách từ (₫)", type: "number", min: 0 },
  { name: "maxBudget", label: "Ngân sách đến (₫)", type: "number", min: 0 },
];
export function PlayerProfilePage() {
  const q = useData("/player-profile");
  const { openChatbot } = useChatbot();
  return (
    <Page
      title="Hồ sơ người chơi"
      subtitle="Hồ sơ hiện dùng để xếp hạng vợt theo trình độ, lối chơi, cổ tay, trọng lượng, độ cứng và ngân sách. Dùng DH Chatbot để nhập tiêu chí cho giày, quần áo và dụng cụ khác."
    >
      <div className="recommendation-intro"><Target size={28} /><div><h2>Bắt đầu từ lối chơi của bạn</h2><p>Một hồ sơ, những lựa chọn phù hợp với trình độ và thể lực.</p></div></div>
      {q.loading ? (
        <p role="status">Đang tải…</p>
      ) : q.error && q.errorCode !== "PROFILE_REQUIRED" ? (
        <State query={q} />
      ) : (
        <Form
          fields={playerFields}
          initial={
            q.data || {
              skillLevel: "BEGINNER",
              playStyle: "BALANCED",
              playType: "BOTH",
              wristStrength: "MEDIUM",
              dominantHand: "RIGHT",
              preferredWeight: "4U",
              balanceType: "EVEN_BALANCE",
              shaftFlexibility: "MEDIUM",
              shirtSize: "L",
              pantsSize: "L",
              shoeSize: "42",
              favoriteColors: "",
              favoriteBrands: "",
              minBudget: 500000,
              maxBudget: 2000000,
            }
          }
          submit="Lưu và xem gợi ý"
          onSubmit={(v) =>
            api(
              "/player-profile",
              q.data ? "put" : "post",
              Object.fromEntries(playerFields.map((f) => [f.name, v[f.name]])),
            )
          }
          onDone={() => { q.reload(); openChatbot("profile"); }}
        />
      )}
    </Page>
  );
}
export function RecommendationsPage() {
  const q = useData("/recommendations");
  return (
    <Page
      title="Vợt phù hợp với bạn"
      subtitle="Điểm phù hợp cho vợt, dựa trên trình độ, lối chơi, lực cổ tay, trọng lượng, độ cứng và ngân sách. Nhập tiêu chí về hãng, màu hoặc loại dụng cụ khác trong DH Chatbot."
      action={
        <Link className="button" to="/player-profile">
          <SlidersHorizontal size={16} />Điều chỉnh hồ sơ
        </Link>
      }
    >
      <div className="recommendation-intro"><Sparkles size={28} /><div><h2>Gợi ý vợt / Lựa chọn cá nhân hóa</h2><p>Điểm phù hợp đi kèm thông số, lý do lựa chọn và những điểm cần cân nhắc.</p></div></div>
      <State query={q} empty={!q.data?.length}>
        <div className="recommend-grid">
          {q.data?.map((r, index) => (
            <article className="recommend-card" key={r.productId}>
              <p className="eyebrow">
                GỢI Ý {String(index + 1).padStart(2, "0")}
              </p>
              <ProductCard product={r.product} score={r.matchScore} />
              <div className="match-breakdown">
                {Object.entries(r.breakdown).map(([key, v]) => (
                  <div key={key}>
                    <span>
                      {
                        {
                          skill: "Trình độ",
                          playStyle: "Lối chơi",
                          wrist: "Cổ tay",
                          weight: "Trọng lượng",
                          flex: "Độ cứng",
                          budget: "Ngân sách",
                        }[key]
                      }
                    </span>
                    <meter
                      min="0"
                      max={
                        {
                          skill: 20,
                          playStyle: 25,
                          wrist: 15,
                          weight: 15,
                          flex: 10,
                          budget: 15,
                        }[key]
                      }
                      value={v}
                    />
                    <strong>{v}</strong>
                  </div>
                ))}
              </div>
              <ul>
                {r.reasons.map((t) => (
                  <li key={t}>{t}</li>
                ))}
              </ul>
              {r.cautions.map((t) => (
                <p className="caution" key={t}>
                  {t}
                </p>
              ))}
            </article>
          ))}
        </div>
      </State>
      {q.error && (
        <Link className="button primary" to="/player-profile">
          Tạo hoặc cập nhật hồ sơ người chơi<ArrowUpRight size={16} />
        </Link>
      )}
    </Page>
  );
}
