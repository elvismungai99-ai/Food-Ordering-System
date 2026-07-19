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
import RestaurantMenuPage from "./pages/customer/RestaurantMenuPage";
import RestaurantDetailsPage from "./pages/customer/RestaurantDetailsPage";
import CheckoutPage from "./pages/customer/CheckoutPage";
import PaymentSimulationPage from "./pages/customer/PaymentSimulationPage";
import RestaurantOrdersPage from "./pages/restaurant/RestaurantOrdersPage";
import OrderDetailPage from "./pages/customer/OrderDetailPage";
import RestaurantDashboard from "./pages/restaurant/RestaurantDashboard";
import MenuPage from "./pages/restaurant/MenuPage";
import AnalyticsPage from "./pages/restaurant/AnalyticsPage";

import AdminDashboard from "./pages/admin/AdminDashboard";

import ProtectedRoute from "./routes/ProtectedRoute";

import {
  CartProvider,
} from "./context/CartContext";

function App() {
  return (
    <BrowserRouter>
      <CartProvider>
        <Routes>

          {/* ========================= */}
          {/* PUBLIC ROUTES */}
          {/* ========================= */}

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


          {/* ========================= */}
          {/* CUSTOMER ROUTES */}
          {/* ========================= */}

          <Route
            element={
              <ProtectedRoute
                allowedRoles={[
                  "CUSTOMER",
                ]}
              />
            }
          >

            <Route
              path="/customer/orders/:orderId"
              element={
                <OrderDetailPage />
              }
            />

            <Route
              path="/customer/dashboard"
              element={
                <CustomerDashboard />
              }
            />

            <Route
              path="/customer/restaurants"
              element={
                <RestaurantPage />
              }
            />

            <Route
              path="/customer/restaurants/:restaurantId/menu"
              element={
                <RestaurantMenuPage />
              }
            />

            <Route
              path="/customer/restaurants/:restaurantId/details"
              element={
                <RestaurantDetailsPage />
              }
            />


            {/* ========================= */}
            {/* CART */}
            {/* ========================= */}

            <Route
              path="/customer/cart"
              element={
                <CartPage />
              }
            />


            {/* ========================= */}
            {/* CHECKOUT */}
            {/* ========================= */}

            <Route
              path="/customer/checkout"
              element={
                <CheckoutPage />
              }
            />


            {/* ========================= */}
            {/* PAYMENT SIMULATION */}
            {/* ========================= */}

            <Route
              path="/customer/payment"
              element={
                <PaymentSimulationPage />
              }
            />


            {/* ========================= */}
            {/* ORDERS */}
            {/* ========================= */}

            <Route
              path="/customer/orders"
              element={
                <OrdersPage />
              }
            />


            {/* ========================= */}
            {/* CUSTOMER PROFILE */}
            {/* ========================= */}

            <Route
              path="/customer/profile"
              element={
                <ProfilePage />
              }
            />

          </Route>


          {/* ========================= */}
          {/* RESTAURANT OWNER ROUTES */}
          {/* ========================= */}

          <Route
            element={
              <ProtectedRoute
                allowedRoles={[
                  "OWNER",
                ]}
              />
            }
          >

            <Route
              path="/restaurant/dashboard"
              element={
                <RestaurantDashboard />
              }
            />

            <Route
              path="/restaurant/menu"
              element={
                <MenuPage />
              }
            />

            <Route
              path="/restaurant/analytics"
              element={
                <AnalyticsPage />
              }
            />

            <Route
              path="/restaurant/orders"
              element={
                <RestaurantOrdersPage />
              }
            />

            <Route
              path="/restaurant/:restaurantId/details"
              element={
                <RestaurantDetailsPage />
              }
            />

          </Route>


          {/* ========================= */}
          {/* SUPER ADMIN ROUTES */}
          {/* ========================= */}

          <Route
            element={
              <ProtectedRoute
                allowedRoles={[
                  "SUPER_ADMIN",
                ]}
              />
            }
          >

            <Route
              path="/admin/dashboard"
              element={
                <AdminDashboard />
              }
            />

          </Route>


          {/* ========================= */}
          {/* FALLBACK ROUTE */}
          {/* ========================= */}

          <Route
            path="*"
            element={
              <HomePage />
            }
          />

        </Routes>
      </CartProvider>
    </BrowserRouter>
  );
}

export default App;