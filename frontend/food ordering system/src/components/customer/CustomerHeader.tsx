import { useNavigate } from "react-router-dom";

import { useCart } from "../../context/CartContext";

interface CustomerHeaderProps {
  title?: string;
}

function CustomerHeader({
  title = "Food Ordering",
}: CustomerHeaderProps) {
  const navigate = useNavigate();
  const { totalItems, loading } = useCart();

  return (
    <header className="border-b border-slate-200 bg-white">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
        <button
          type="button"
          onClick={() =>
            navigate("/customer/dashboard")
          }
          className="text-xl font-bold text-slate-950"
        >
          {title}
        </button>

        <nav className="flex items-center gap-3">
          <button
            type="button"
            onClick={() =>
              navigate("/customer/restaurants")
            }
            className="rounded-3xl px-4 py-2 text-sm font-semibold text-slate-700 transition hover:bg-slate-100"
          >
            Restaurants
          </button>

          <button
            type="button"
            onClick={() =>
              navigate("/customer/orders")
            }
            className="rounded-3xl px-4 py-2 text-sm font-semibold text-slate-700 transition hover:bg-slate-100"
          >
            Orders
          </button>

          <button
            type="button"
            onClick={() =>
              navigate("/customer/cart")
            }
            className="flex items-center gap-2 rounded-3xl bg-indigo-600 px-5 py-2 text-sm font-semibold text-white transition hover:bg-indigo-700"
          >
            <span aria-hidden="true">🛒</span>
            <span>Cart</span>

            <span className="flex min-h-6 min-w-6 items-center justify-center rounded-full bg-white px-1.5 text-xs font-bold text-indigo-600">
              {loading ? "…" : totalItems}
            </span>
          </button>
        </nav>
      </div>
    </header>
  );
}

export default CustomerHeader;