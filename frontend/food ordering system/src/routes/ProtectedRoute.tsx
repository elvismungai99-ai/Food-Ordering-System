import {
  Navigate,
  Outlet,
} from "react-router-dom";
import { getActiveAuthSession } from "../utils/auth";

interface ProtectedRouteProps {
  allowedRoles: string[];
}

function ProtectedRoute({
  allowedRoles,
}: ProtectedRouteProps) {
  const { token, role } = getActiveAuthSession();

  if (!token) {
    return (
      <Navigate
        to="/login"
        replace
      />
    );
  }

  if (
    !role ||
    !allowedRoles.includes(role)
  ) {
    return (
      <Navigate
        to="/"
        replace
      />
    );
  }

  return <Outlet />;
}

export default ProtectedRoute;