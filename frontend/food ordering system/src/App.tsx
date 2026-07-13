import {
  BrowserRouter,
  Route,
  Routes,
} from "react-router-dom";

import HomePage from "./pages/auth/services/HomePage";
import LoginPage from "./pages/auth/services/LoginPage";
import RegisterPage from "./pages/auth/services/RegisterPage";

import CustomerDashboard from "./pages/customer/CustomerDashboard";
import CartPage from "./pages/customer/CartPage";
import OrdersPage from "./pages/customer/OrdersPage";
import ProfilePage from "./pages/customer/CustomerProfilePage";
import RestaurantPage from "./pages/customer/RestaurantPage";
import RestaurantDetailsPage from "./pages/customer/RestaurantDetailsPage";

import RestaurantDashboard from "./pages/restaurant/RestaurantDashboard";
import MenuPage from "./pages/restaurant/MenuPage";
import AnalyticsPage from "./pages/restaurant/AnalyticsPage";

import AdminDashboard from "./pages/admin/AdminDashboard";

import ProtectedRoute from "./routes/ProtectedRoute";
import { CartProvider } from "./context/CartContext";

function App() {
  return (
    <BrowserRouter>
      <CartProvider>
        <Routes>
          {/* Public routes */}
          <Route
            path="/"
            element={<HomePage />}
          />

          <Route
            path="/login"
            element={<LoginPage />}
          />

          <Route
            path="/register"
            element={<RegisterPage />}
          />

          {/* Customer routes */}
          <Route
            element={
              <ProtectedRoute
                allowedRoles={["CUSTOMER"]}
              />
            }
          >
            <Route
              path="/customer/dashboard"
              element={<CustomerDashboard />}
            />

            <Route
              path="/customer/restaurants"
              element={<RestaurantPage />}
            />

            <Route
              path="/customer/restaurants/:restaurantId/menu"
              element={<RestaurantDetailsPage />}
            />

            <Route
              path="/customer/restaurants/:restaurantId/details"
              element={<RestaurantDetailsPage />}
            />

            <Route
              path="/customer/cart"
              element={<CartPage />}
            />

            <Route
              path="/customer/orders"
              element={<OrdersPage />}
            />

            <Route
              path="/customer/profile"
              element={<ProfilePage />}
            />
          </Route>

          {/* Restaurant owner routes */}
          <Route
            element={
              <ProtectedRoute
                allowedRoles={["OWNER"]}
              />
            }
          >
            <Route
              path="/restaurant/dashboard"
              element={<RestaurantDashboard />}
            />

            <Route
              path="/restaurant/menu"
              element={<MenuPage />}
            />

            <Route
              path="/restaurant/analytics"
              element={<AnalyticsPage />}
            />
          </Route>

          {/* Administrator routes */}
          <Route
            element={
              <ProtectedRoute
                allowedRoles={["SUPER_ADMIN"]}
              />
            }
          >
            <Route
              path="/admin/dashboard"
              element={<AdminDashboard />}
            />
          </Route>

          <Route
            path="*"
            element={<HomePage />}
          />
        </Routes>
      </CartProvider>
    </BrowserRouter>
  );
}

export default App;