import { Navigate, Outlet, useLocation } from "react-router-dom";
import Alert from "@mui/material/Alert";
import { useAuth } from "../context/AuthContext";
export default function ProtectedRoute({ roles }) {
  const { user } = useAuth();
  const location = useLocation();
  if (!user)
    return <Navigate to="/login" state={{ from: location.pathname }} replace />;
  if (roles && !roles.includes(user.role))
    return (
      <Alert severity="error">Bạn không có quyền truy cập trang này.</Alert>
    );
  return <Outlet />;
}
export const AdminRoute = () => <ProtectedRoute roles={["ADMIN"]} />;
export const InspectorRoute = () => <ProtectedRoute roles={["INSPECTOR"]} />;
