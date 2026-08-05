import {
  useEffect,
  useState,
} from "react";

import {
  useNavigate,
} from "react-router-dom";

import api from "../../api/axios";
import {
  getApiErrorMessage,
} from "../../utils/apiError";

interface Restaurant {
  id: string;
  ownerId?: string;

  name: string;
  description: string;
  address: string;

  openingTime: string;
  closingTime: string;

  status: string;
  category: string;
}

function RestaurantDashboard() {
  const navigate =
    useNavigate();

  const [
    restaurant,
    setRestaurant,
  ] = useState<Restaurant | null>(
    null
  );

  const [
    loading,
    setLoading,
  ] = useState(true);

  const [
    error,
    setError,
  ] = useState("");

  useEffect(() => {

    const loadRestaurant =
      async () => {

        try {

          setLoading(true);
          setError("");

          /*
           * Load the restaurant belonging
           * to the logged-in restaurant owner.
           */
          const response =
            await api.get<Restaurant>(
              "/restaurants/me"
            );

          const restaurantData =
            response.data;

          setRestaurant(
            restaurantData
          );

          /*
           * The RestaurantOrdersPage currently
           * uses this restaurant ID when calling:
           *
           * GET /api/orders/restaurant/{restaurantId}
           *
           * Store it after successfully loading
           * the owner's restaurant.
           */
          if (
            restaurantData?.id
          ) {

            localStorage.setItem(
              "restaurantId",
              restaurantData.id
            );

          }

        } catch (
          requestError
        ) {

          console.error(
            "Failed to load restaurant:",
            requestError
          );

          setError(
            getApiErrorMessage(
              requestError
            )
          );

        } finally {

          setLoading(false);

        }

      };

    loadRestaurant();

  }, []);


  // =========================================================
  // LOGOUT
  // =========================================================

  const handleLogout = () => {

    localStorage.removeItem(
      "token"
    );

    localStorage.removeItem(
      "role"
    );

    localStorage.removeItem(
      "userId"
    );

    localStorage.removeItem(
      "firstName"
    );

    localStorage.removeItem(
      "restaurantId"
    );

    navigate(
      "/login"
    );

  };


  if (loading) {

    return (

      <div className="food-page flex items-center justify-center">

        <p className="text-slate-500">
          Loading restaurant dashboard...
        </p>

      </div>

    );

  }


  return (

    <div className="food-page">

      {/* ===================================================== */}
      {/* HEADER */}
      {/* ===================================================== */}

      <header className="border-b border-emerald-100/80 bg-white/90 shadow-sm shadow-slate-200/50 backdrop-blur-xl">

        <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-5">

          <div>

            <h1 className="text-2xl font-black text-slate-950">
              Restaurant Dashboard
            </h1>

            {restaurant && (

              <p className="mt-1 text-sm text-slate-500">
                {restaurant.name}
              </p>

            )}

          </div>


          <div className="flex flex-wrap items-center gap-3">

            <button
              type="button"
              onClick={() =>
                navigate(
                  "/"
                )
              }
              className="food-button-secondary px-5 py-2 text-sm"
            >
              Home
            </button>

            <button
              type="button"
              onClick={
                handleLogout
              }
              className="food-button-secondary px-5 py-2 text-sm"
            >
              Logout
            </button>

          </div>

        </div>

      </header>


      {/* ===================================================== */}
      {/* MAIN CONTENT */}
      {/* ===================================================== */}

      <main className="mx-auto max-w-7xl px-6 py-10">


        {/* ERROR */}

        {error && (

          <div className="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">

            {error}

          </div>

        )}


        {/* RESTAURANT INFORMATION */}

        {restaurant && (

          <section className="food-card mb-8 p-6">

            <div className="flex flex-wrap items-start justify-between gap-4">

              <div>

                <h2 className="text-2xl font-semibold text-slate-950">
                  {restaurant.name}
                </h2>

                <p className="mt-2 max-w-2xl text-sm text-slate-500">
                  {restaurant.description}
                </p>

              </div>


              <span
                className={
                  `rounded-full px-4 py-2 text-sm font-semibold ${
                    restaurant.status
                      === "APPROVED"
                      ? "bg-emerald-100 text-emerald-700"
                      : restaurant.status === "PENDING_APPROVAL"
                        ? "bg-amber-100 text-amber-700"
                      : "bg-slate-100 text-slate-600"
                  }`
                }
              >

                {
                  restaurant.status
                }

              </span>

            </div>


            <div className="mt-6 grid gap-5 sm:grid-cols-2 lg:grid-cols-4">

              <div>

                <p className="text-xs uppercase tracking-wide text-slate-400">
                  Category
                </p>

                <p className="mt-1 font-medium text-slate-800">
                  {
                    restaurant.category
                    || "Not specified"
                  }
                </p>

              </div>


              <div>

                <p className="text-xs uppercase tracking-wide text-slate-400">
                  Address
                </p>

                <p className="mt-1 font-medium text-slate-800">
                  {
                    restaurant.address
                  }
                </p>

              </div>


              <div>

                <p className="text-xs uppercase tracking-wide text-slate-400">
                  Opens
                </p>

                <p className="mt-1 font-medium text-slate-800">
                  {
                    restaurant.openingTime
                  }
                </p>

              </div>


              <div>

                <p className="text-xs uppercase tracking-wide text-slate-400">
                  Closes
                </p>

                <p className="mt-1 font-medium text-slate-800">
                  {
                    restaurant.closingTime
                  }
                </p>

              </div>

            </div>

          </section>

        )}


        {/* ===================================================== */}
        {/* DASHBOARD ACTIONS */}
        {/* ===================================================== */}

        <section>

          <div className="mb-5">

            <h2 className="text-xl font-semibold text-slate-950">
              Restaurant Management
            </h2>

            <p className="mt-1 text-sm text-slate-500">
              Manage your restaurant, menu, orders and analytics.
            </p>

          </div>


          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">


            {/* RESTAURANT DETAILS */}

            <button
              type="button"
              onClick={() =>
                navigate(
                  "/restaurant/details"
                )
              }
              className="food-card food-card-hover p-6 text-left"
            >
              <div className="food-badge bg-emerald-100 text-emerald-700">
                Profile
              </div>

              <h3 className="mt-4 text-lg font-semibold text-slate-950">
                Restaurant Details
              </h3>

              <p className="mt-2 text-sm text-slate-500">
                View the profile, address, opening hours and current status.
              </p>

            </button>


            {/* MENU */}

            <button
              type="button"
              onClick={() =>
                navigate(
                  "/restaurant/menu"
                )
              }
              className="food-card food-card-hover p-6 text-left"
            >
              <div className="food-badge bg-amber-100 text-amber-700">
                Menu
              </div>

              <h3 className="mt-4 text-lg font-semibold text-slate-950">
                Manage Menu
              </h3>

              <p className="mt-2 text-sm text-slate-500">
                Add, edit and remove menu items and update prices.
              </p>

            </button>


            {/* ================================================= */}
            {/* ORDERS - NEW STATE MACHINE CONNECTION */}
            {/* ================================================= */}

            <button
              type="button"
              onClick={() =>
                navigate(
                  "/restaurant/orders"
                )
              }
              className="food-card food-card-hover p-6 text-left"
            >
              <div className="food-badge bg-sky-100 text-sky-700">
                Orders
              </div>

              <h3 className="mt-4 text-lg font-semibold text-slate-950">
                Manage Orders
              </h3>

              <p className="mt-2 text-sm text-slate-500">
                View incoming orders and move them through their order status.
              </p>

            </button>


            {/* ANALYTICS */}

            <button
              type="button"
              onClick={() =>
                navigate(
                  "/restaurant/analytics"
                )
              }
              className="food-card food-card-hover p-6 text-left"
            >
              <div className="food-badge bg-slate-100 text-slate-700">
                Analytics
              </div>

              <h3 className="mt-4 text-lg font-semibold text-slate-950">
                Analytics
              </h3>

              <p className="mt-2 text-sm text-slate-500">
                View restaurant performance and sales information.
              </p>

            </button>

          </div>

        </section>

      </main>

    </div>

  );
}

export default RestaurantDashboard;
