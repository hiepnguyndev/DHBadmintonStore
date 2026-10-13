import { BrowserRouter, Routes, Route, Link } from "react-router-dom";
import { lazy, Suspense } from "react";
import { ThemeProvider, createTheme } from "@mui/material/styles";
import CssBaseline from "@mui/material/CssBaseline";
import Button from "@mui/material/Button";
import { AuthProvider } from "./context/AuthContext";
import { ChatbotProvider } from "./context/ChatbotContext";
import ChatbotWidget from "./components/ChatbotWidget";
import DocumentTranslator from "./components/DocumentTranslator";
const Layout = lazy(() => import("./components/Layout"));
const AdminLayout = lazy(() => import("./components/AdminLayout"));
import ProtectedRoute from "./routes/ProtectedRoute";
const namedLazy = (loader, name) => lazy(() => loader().then((module) => ({ default: module[name] })));
const shopPages = () => import("./pages/ShopPages");
const recommendationPages = () => import("./pages/RecommendationPages");
const marketplacePages = () => import("./pages/MarketplacePages");
const staffPages = () => import("./pages/StaffPages");
const HomePage = lazy(() => import("./pages/HomePage"));
const newsPages = () => import("./pages/NewsPages");
const NewsPage = namedLazy(newsPages, "NewsPage");
const NewsDetailPage = namedLazy(newsPages, "NewsDetailPage");
const AdminPostsPage = lazy(() => import("./pages/AdminPostsPage"));
const LoginPage = lazy(() => import("./pages/LoginPage"));
const RegisterPage = lazy(() => import("./pages/RegisterPage"));
const ProfilePage = lazy(() => import("./pages/ProfilePage"));
const AddressesPage = lazy(() => import("./pages/AddressesPage"));
const ProductListPage = namedLazy(shopPages, "ProductListPage");
const PromotionsPage = namedLazy(shopPages, "PromotionsPage");
const ProductDetailPage = namedLazy(shopPages, "ProductDetailPage");
const CartPage = namedLazy(shopPages, "CartPage");
const CheckoutPage = namedLazy(shopPages, "CheckoutPage");
const OrdersPage = namedLazy(shopPages, "OrdersPage");
const OrderDetailPage = namedLazy(shopPages, "OrderDetailPage");
const FavoritesPage = namedLazy(shopPages, "FavoritesPage");
const PlayerProfilePage = namedLazy(recommendationPages, "PlayerProfilePage");
const RecommendationsPage = namedLazy(recommendationPages, "RecommendationsPage");
const MarketplacePage = namedLazy(marketplacePages, "MarketplacePage");
const ListingDetailPage = namedLazy(marketplacePages, "ListingDetailPage");
const SellListingPage = namedLazy(marketplacePages, "SellListingPage");
const MarketBuyPage = namedLazy(marketplacePages, "MarketBuyPage");
const MarketOrdersPage = namedLazy(marketplacePages, "MarketOrdersPage");
const MarketOrderDetailPage = namedLazy(marketplacePages, "MarketOrderDetailPage");
const DisputeDetailPage = namedLazy(marketplacePages, "DisputeDetailPage");
const SellerProfilePage = namedLazy(marketplacePages, "SellerProfilePage");
const NotificationsPage = namedLazy(marketplacePages, "NotificationsPage");
const AuditLogsPage = namedLazy(staffPages, "AuditLogsPage");
const AdminShipmentsPage = namedLazy(staffPages, "AdminShipmentsPage");
const AdminPage = namedLazy(staffPages, "AdminPage");
const AdminProductsPage = namedLazy(staffPages, "AdminProductsPage");
const AdminPromotionsPage = lazy(() => import("./pages/AdminPromotionsPage"));
const AdminOrdersPage = namedLazy(staffPages, "AdminOrdersPage");
const AdminListingsPage = namedLazy(staffPages, "AdminListingsPage");
const AdminDisputesPage = namedLazy(staffPages, "AdminDisputesPage");
const AdminUsersPage = namedLazy(staffPages, "AdminUsersPage");
const InspectionsPage = namedLazy(staffPages, "InspectionsPage");
const InspectionReportPage = namedLazy(staffPages, "InspectionReportPage");
const StatisticsPage = namedLazy(staffPages, "StatisticsPage");
// Restore deep links routed through GitHub Pages' 404.html fallback.
const pagesRedirect = new URLSearchParams(window.location.search).get("redirect");
if (pagesRedirect) {
  const basePath = window.location.pathname.replace(/\/$/, "");
  window.history.replaceState(
    null,
    "",
    `${basePath}/${pagesRedirect.replace(/^\/+/, "")}`,
  );
}
const theme = createTheme({
  palette: {
    primary: { main: "#087c63", dark: "#065c49", contrastText: "#ffffff" },
    secondary: { main: "#bd4a73" },
    background: { default: "#f7f9f8", paper: "#ffffff" },
    text: { primary: "#182923", secondary: "#68766f" },
    success: { main: "#087c63" },
  },
  typography: {
    fontFamily: '"Segoe UI", Arial, sans-serif',
    allVariants: { letterSpacing: 0 },
    button: { textTransform: "none", fontWeight: 600, letterSpacing: 0 },
  },
  shape: { borderRadius: 8 },
  components: {
    MuiButton: {
      styleOverrides: { root: { boxShadow: "none", padding: "10px 18px", minHeight: 42, borderRadius: 6 }, contained: { "&:hover": { boxShadow: "none" } } },
    },
    MuiTextField: { defaultProps: { variant: "outlined", size: "small" } },
    MuiOutlinedInput: { styleOverrides: { root: { minHeight: 46, backgroundColor: "#fff", borderRadius: 6 }, notchedOutline: { borderColor: "#d9e2dc" } } },
    MuiChip: { styleOverrides: { root: { borderRadius: 5, fontSize: 11, fontWeight: 600 } } },
    MuiDialog: { styleOverrides: { paper: { borderRadius: 8 } } },
  },
});
export default function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <BrowserRouter basename={import.meta.env.BASE_URL}>
        <AuthProvider>
          <ChatbotProvider>
          <DocumentTranslator />
          <Suspense fallback={<div className="page-container" role="status">Đang tải trang…</div>}>
          <Routes>
            <Route element={<Layout />}>
              <Route index element={<HomePage />} />
              <Route path="products" element={<ProductListPage />} />
              <Route path="promotions" element={<PromotionsPage />} />
              <Route path="news" element={<NewsPage />} />
              <Route path="news/:id" element={<NewsDetailPage />} />
              <Route path="products/:id" element={<ProductDetailPage />} />
              <Route path="marketplace" element={<MarketplacePage />} />
              <Route path="marketplace/:id" element={<ListingDetailPage />} />
              <Route path="sellers/:id" element={<SellerProfilePage />} />
              <Route path="login" element={<LoginPage />} />
              <Route path="register" element={<RegisterPage />} />
              <Route element={<ProtectedRoute />}>
                <Route path="cart" element={<CartPage />} />
                <Route path="checkout" element={<CheckoutPage />} />
                <Route path="orders" element={<OrdersPage />} />
                <Route path="orders/:id" element={<OrderDetailPage />} />
                <Route path="favorites" element={<FavoritesPage />} />
                <Route path="player-profile" element={<PlayerProfilePage />} />
                <Route
                  path="recommendations"
                  element={<RecommendationsPage />}
                />
                <Route path="sell" element={<SellListingPage />} />
                <Route path="sell/:id" element={<SellListingPage />} />
                <Route path="my-listings" element={<MarketplacePage mine />} />
                <Route path="marketplace/:id/buy" element={<MarketBuyPage />} />
                <Route path="market-orders" element={<MarketOrdersPage />} />
                <Route
                  path="market-orders/:id"
                  element={<MarketOrderDetailPage />}
                />
                <Route path="disputes/:id" element={<DisputeDetailPage />} />
                <Route
                  path="my-inspections"
                  element={<InspectionsPage mine />}
                />
                <Route path="notifications" element={<NotificationsPage />} />
                <Route path="profile" element={<ProfilePage />} />
                <Route path="addresses" element={<AddressesPage />} />
              </Route>
              <Route
                element={<ProtectedRoute roles={["ADMIN", "INSPECTOR"]} />}
              >
                <Route path="inspector" element={<InspectionsPage />} />
                <Route
                  path="inspector/:id/report"
                  element={<InspectionReportPage />}
                />
              </Route>
              <Route
                path="*"
                element={
                  <div className="page-container">
                    <h1>Không tìm thấy trang</h1>
                    <Button component={Link} to="/">
                      Về trang chủ
                    </Button>
                  </div>
                }
              />
            </Route>
            <Route element={<ProtectedRoute roles={["ADMIN"]} />}>
              <Route path="admin" element={<AdminLayout />}>
                <Route index element={<AdminPage />} />
                <Route path="audit-logs" element={<AuditLogsPage />} />
                <Route path="products" element={<AdminProductsPage />} />
                <Route path="promotions" element={<AdminPromotionsPage />} />
                <Route path="posts" element={<AdminPostsPage />} />
                <Route path="shipments" element={<AdminShipmentsPage />} />
                <Route path="orders" element={<AdminOrdersPage />} />
                <Route path="listings" element={<AdminListingsPage />} />
                <Route path="disputes" element={<AdminDisputesPage />} />
                <Route path="users" element={<AdminUsersPage />} />
                <Route path="statistics" element={<StatisticsPage />} />
              </Route>
            </Route>
          </Routes>
          </Suspense>
          <ChatbotWidget />
          </ChatbotProvider>
        </AuthProvider>
      </BrowserRouter>
    </ThemeProvider>
  );
}
