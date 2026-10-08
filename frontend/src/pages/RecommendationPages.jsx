import { Link, useNavigate } from "react-router-dom";
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
  {
    name: "preferredWeight",
    label: "Trọng lượng ưa thích",
    options: ["2U", "3U", "4U", "5U", "6U"],
  },
  { name: "minBudget", label: "Ngân sách từ (₫)", type: "number", min: 0 },
  { name: "maxBudget", label: "Ngân sách đến (₫)", type: "number", min: 0 },
];
export function PlayerProfilePage() {
  const q = useData("/player-profile"),
    navigate = useNavigate();
  return (
    <Page
      title="Hồ sơ người chơi"
      subtitle="Cho chúng tôi biết cách bạn chơi và ngân sách để tìm vợt phù hợp."
    >
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
              preferredWeight: "4U",
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
          onDone={() => navigate("/recommendations")}
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
      subtitle="Điểm phù hợp tổng hợp trình độ, lối chơi, lực cổ tay, trọng lượng, độ cứng và ngân sách."
      action={
        <Link className="button" to="/player-profile">
          Điều chỉnh hồ sơ
        </Link>
      }
    >
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
          Tạo hoặc cập nhật hồ sơ người chơi
        </Link>
      )}
    </Page>
  );
}
