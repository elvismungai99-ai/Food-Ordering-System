import { useNavigate } from "react-router-dom";

import CustomerHeader from "../../components/customer/CustomerHeader";

function OrdersPage() {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-slate-100">
      <CustomerHeader />

      <main className="mx-auto max-w-6xl px-6 py-10">
        <div>
          <h1 className="text-3xl font-bold text-slate-950">
            Your Orders
          </h1>

          <p className="mt-2 text-slate-500">
            View and track your food orders.
          </p>
        </div>

        <section className="mt-8 rounded-[24px] border border-slate-200 bg-white p-12 text-center">
          <div className="text-5xl">📦</div>

          <h2 className="mt-5 text-xl font-semibold text-slate-900">
            No orders available
          </h2>

          <p className="mt-2 text-slate-500">
            Your placed orders will appear here.
          </p>

          <button
            type="button"
            onClick={() =>
              navigate("/customer/restaurants")
            }
            className="mt-6 rounded-3xl bg-indigo-600 px-6 py-3 text-sm font-semibold text-white"
          >
            Browse restaurants
          </button>
        </section>
      </main>
    </div>
  );
}

export default OrdersPage;