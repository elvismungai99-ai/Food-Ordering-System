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
    <div className="min-h-screen bg-slate-100">
      <CustomerHeader title="Food Ordering" />

      <main className="mx-auto max-w-6xl px-6 py-10">
        <section className="rounded-[28px] bg-slate-950 px-8 py-10 text-white">
          <div className="flex flex-col justify-between gap-6 md:flex-row md:items-center">
            <div>
              <p className="text-sm font-medium text-indigo-300">
                Customer dashboard
              </p>

              <h1 className="mt-2 text-4xl font-bold">
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
            className="rounded-[24px] border border-slate-200 bg-white p-6 text-left shadow-sm transition hover:-translate-y-1 hover:shadow-md"
          >
            <div className="text-3xl">🍽️</div>

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
            className="rounded-[24px] border border-slate-200 bg-white p-6 text-left shadow-sm transition hover:-translate-y-1 hover:shadow-md"
          >
            <div className="text-3xl">🛒</div>

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
            className="rounded-[24px] border border-slate-200 bg-white p-6 text-left shadow-sm transition hover:-translate-y-1 hover:shadow-md"
          >
            <div className="text-3xl">📦</div>

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
            className="rounded-[24px] border border-slate-200 bg-white p-6 text-left shadow-sm transition hover:-translate-y-1 hover:shadow-md"
          >
            <div className="text-3xl">👤</div>

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