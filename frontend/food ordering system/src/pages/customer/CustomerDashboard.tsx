import { useNavigate } from "react-router-dom";

import CustomerHeader from "../../components/customer/CustomerHeader";

function CustomerDashboard() {
  const navigate = useNavigate();

  const firstName =
    localStorage.getItem("firstName") || "Customer";

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    localStorage.removeItem("userId");
    localStorage.removeItem("firstName");

    navigate("/login");
  };

  return (
    <div className="food-page">
      <CustomerHeader title="Food Ordering" />

      <main className="mx-auto max-w-6xl px-6 py-10">
        <section className="overflow-hidden rounded-[32px] bg-slate-950 px-8 py-10 text-white shadow-2xl shadow-slate-900/20">
          <div className="flex flex-col justify-between gap-6 md:flex-row md:items-center">
            <div>
              <p className="text-sm font-bold uppercase text-emerald-300">
                Customer dashboard
              </p>

              <h1 className="mt-2 text-4xl font-black">
                Welcome, {firstName}
              </h1>

              <p className="mt-3 max-w-2xl text-slate-300">
                Browse restaurants, add meals to your cart,
                and keep track of your orders.
              </p>
            </div>

            <button
              type="button"
              onClick={handleLogout}
              className="w-fit rounded-3xl border border-white/20 px-5 py-2 text-sm font-semibold text-white transition hover:bg-white/10"
            >
              Log out
            </button>
          </div>
        </section>

        <section className="mt-8 grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
          <button
            type="button"
            onClick={() =>
              navigate("/customer/restaurants")
            }
            className="food-card food-card-hover p-6 text-left"
          >
            <div className="food-badge bg-emerald-100 text-emerald-700">Food</div>

            <h2 className="mt-4 text-lg font-semibold text-slate-900">
              Restaurants
            </h2>

            <p className="mt-2 text-sm text-slate-500">
              Browse restaurants and their menus.
            </p>
          </button>

          <button
            type="button"
            onClick={() =>
              navigate("/customer/cart")
            }
            className="food-card food-card-hover p-6 text-left"
          >
            <div className="food-badge bg-amber-100 text-amber-700">Cart</div>

            <h2 className="mt-4 text-lg font-semibold text-slate-900">
              Cart
            </h2>

            <p className="mt-2 text-sm text-slate-500">
              Review and update your selected items.
            </p>
          </button>

          <button
            type="button"
            onClick={() =>
              navigate("/customer/orders")
            }
            className="food-card food-card-hover p-6 text-left"
          >
            <div className="food-badge bg-sky-100 text-sky-700">Orders</div>

            <h2 className="mt-4 text-lg font-semibold text-slate-900">
              Orders
            </h2>

            <p className="mt-2 text-sm text-slate-500">
              View your current and previous orders.
            </p>
          </button>

          <button
            type="button"
            onClick={() =>
              navigate("/customer/profile")
            }
            className="food-card food-card-hover p-6 text-left"
          >
            <div className="food-badge bg-slate-100 text-slate-700">Profile</div>

            <h2 className="mt-4 text-lg font-semibold text-slate-900">
              Profile
            </h2>

            <p className="mt-2 text-sm text-slate-500">
              View and manage your account details.
            </p>
          </button>
        </section>
      </main>
    </div>
  );
}

export default CustomerDashboard;
