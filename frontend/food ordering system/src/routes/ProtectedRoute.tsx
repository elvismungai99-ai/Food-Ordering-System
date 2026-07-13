import {
  Navigate,
  Outlet,
} from "react-router-dom";

interface ProtectedRouteProps {
  allowedRoles: string[];
}

function ProtectedRoute({
  allowedRoles,
}: ProtectedRouteProps) {
  const token = localStorage.getItem("token");

  const storedRole =
    localStorage.getItem("role");

  const role = storedRole
    ?.replace("ROLE_", "")
    .toUpperCase();

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