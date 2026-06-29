import { BrowserRouter, Routes, Route } from "react-router-dom";

import HomePage from "./pages/auth/services/HomePage";
import LoginPage from "./pages/auth/services/LoginPage";
import RegisterPage from "./pages/auth/services/RegisterPage";

import CustomerDashboard from "./pages/customer/CustomerDashboard";
import CartPage from "./pages/customer/CartPage";
import OrdersPage from "./pages/customer/OrdersPage";
import ProfilePage from "./pages/customer/ProfilePage";
import RestaurantPage from "./pages/customer/RestaurantPage";

import RestaurantDashboard from "./pages/restaurant/RestaurantDashboard";
import MenuPage from "./pages/restaurant/MenuPage";
import AnalyticsPage from "./pages/restaurant/AnalyticsPage";

import ProtectedRoute from "./routes/ProtectedRoute";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public Routes */}
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        {/* Protected Customer Routes */}
        <Route element={<ProtectedRoute allowedRoles={["CUSTOMER"]} />}>
          <Route path="/customer/dashboard" element={<CustomerDashboard />} />
          <Route path="/customer/cart" element={<CartPage />} />
          <Route path="/customer/orders" element={<OrdersPage />} />
          <Route path="/customer/profile" element={<ProfilePage />} />
          <Route path="/customer/restaurants" element={<RestaurantPage />} />
        </Route>

        {/* Protected Restaurant Routes */}
        <Route element={<ProtectedRoute allowedRoles={["RESTAURANT_ADMIN"]} />}>
          <Route path="/restaurant/dashboard" element={<RestaurantDashboard />} />
          <Route path="/restaurant/menu" element={<MenuPage />} />
          <Route path="/restaurant/analytics" element={<AnalyticsPage />} />
        </Route>

        {/* Fallback */}
        <Route path="*" element={<HomePage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;