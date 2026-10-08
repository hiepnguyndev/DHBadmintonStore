import { Link } from "react-router-dom";
import Button from "@mui/material/Button";
import Chip from "@mui/material/Chip";
import { useAuth } from "../context/AuthContext";
export default function HomePage() {
  const { user } = useAuth();
  return (
    <>
      <section className="hero">
        <div className="hero-copy">
          <span className="eyebrow">FOR YOUR NEXT GREAT GAME</span>
          <h1>
            Mỗi người chơi.
            <br />
            Một <em>phong cách.</em>
          </h1>
          <p>
            Không chỉ là dụng cụ. Đó là lựa chọn phù hợp với cách bạn bước vào
            sân và chơi hết mình.
          </p>
          <Button
            size="large"
            variant="contained"
            component={Link}
            to="/recommendations"
          >
            Tìm vợt phù hợp với bạn <span aria-hidden="true">↗</span>
          </Button>
          <div className="hero-note">DH BADMINTON STORE / EST. 2026</div>
        </div>
        <div className="court-art" aria-hidden="true">
          <div className="court-line court-line-a" />
          <div className="court-line court-line-b" />
          <div className="court-circle" />
          <span className="court-word">
            PLAY
            <br />
            YOUR
            <br />
            <em>WAY.</em>
          </span>
          <div className="court-caption">01 — THE COURT IS YOURS</div>
        </div>
      </section>
      <section className="intro-section">
        <div>
          <span className="eyebrow">MỘT KHỞI ĐẦU VỮNG CHẮC</span>
          <h2>
            Sẵn sàng cho
            <br />
            hành trình của bạn.
          </h2>
        </div>
        <p>
          Khám phá cửa hàng, tìm vợt theo hồ sơ người chơi và trao đổi dụng cụ
          trong cộng đồng cầu lông.
        </p>
      </section>
      <section className="feature-grid" aria-label="Các tính năng">
        <article>
          <span className="feature-number">01</span>
          <Chip
            size="small"
            label="Đã có trong phiên bản này"
            color="success"
            variant="outlined"
          />
          <h3>Tài khoản của bạn</h3>
          <p>
            Đăng ký, đăng nhập, cập nhật hồ sơ và quản lý địa chỉ tại một nơi.
          </p>
          <Button component={Link} to={user ? "/profile" : "/login"}>
            Khám phá tài khoản →
          </Button>
        </article>
        <article>
          <span className="feature-number">02</span>
          <h3>Chọn đúng dụng cụ</h3>
          <p>
            Danh mục sản phẩm và gợi ý theo trình độ, lối chơi, lực cổ tay và
            ngân sách.
          </p>
          <Button component={Link} to="/products">
            Khám phá cửa hàng →
          </Button>
        </article>
        <article>
          <span className="feature-number">03</span>
          <h3>Kết nối cộng đồng</h3>
          <p>
            Marketplace dành cho dụng cụ đã qua sử dụng, cùng thông tin tình
            trạng và kiểm định.
          </p>
          <Button component={Link} to="/marketplace">
            Khám phá Marketplace →
          </Button>
        </article>
      </section>
    </>
  );
}
