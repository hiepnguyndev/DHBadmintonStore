import { BrowserRouter, Routes, Route, Link } from "react-router-dom";
import { ThemeProvider, createTheme } from "@mui/material/styles";
import CssBaseline from "@mui/material/CssBaseline";
import Button from "@mui/material/Button";
import { AuthProvider } from "./context/AuthContext";
import Layout from "./components/Layout";
import AdminLayout from "./components/AdminLayout";
import ProtectedRoute from "./routes/ProtectedRoute";
import HomePage from "./pages/HomePage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import ProfilePage from "./pages/ProfilePage";
import AddressesPage from "./pages/AddressesPage";
import {
  ProductListPage,
  ProductDetailPage,
  CartPage,
  CheckoutPage,
  OrdersPage,
  OrderDetailPage,
  FavoritesPage,
} from "./pages/ShopPages";
import {
  PlayerProfilePage,
  RecommendationsPage,
} from "./pages/RecommendationPages";
import {
  MarketplacePage,
  ListingDetailPage,
  SellListingPage,
  MarketBuyPage,
  MarketOrdersPage,
  MarketOrderDetailPage,
  DisputeDetailPage,
  SellerProfilePage,
  NotificationsPage,
} from "./pages/MarketplacePages";
import {
  AuditLogsPage,
  AdminShipmentsPage,
  AdminPage,
  AdminProductsPage,
  AdminOrdersPage,
  AdminListingsPage,
  AdminDisputesPage,
  AdminUsersPage,
  InspectionsPage,
  InspectionReportPage,
  StatisticsPage,
} from "./pages/StaffPages";
const theme = createTheme({
  palette: {
    primary: { main: "#173e36" },
    background: { default: "#f7f8f4" },
    success: { main: "#32684b" },
  },
  typography: {
    fontFamily: 'Inter, "Segoe UI", Arial, sans-serif',
    button: { textTransform: "none", fontWeight: 600 },
  },
  shape: { borderRadius: 8 },
  components: {
    MuiButton: {
      styleOverrides: { root: { boxShadow: "none", padding: "10px 20px" } },
    },
    MuiTextField: { defaultProps: { variant: "outlined" } },
  },
});
export default function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <BrowserRouter>
        <AuthProvider>
          <Routes>
            <Route element={<Layout />}>
              <Route index element={<HomePage />} />
              <Route path="products" element={<ProductListPage />} />
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
                <Route path="shipments" element={<AdminShipmentsPage />} />
                <Route path="orders" element={<AdminOrdersPage />} />
                <Route path="listings" element={<AdminListingsPage />} />
                <Route path="disputes" element={<AdminDisputesPage />} />
                <Route path="users" element={<AdminUsersPage />} />
                <Route path="statistics" element={<StatisticsPage />} />
              </Route>
            </Route>
          </Routes>
        </AuthProvider>
      </BrowserRouter>
    </ThemeProvider>
  );
}
