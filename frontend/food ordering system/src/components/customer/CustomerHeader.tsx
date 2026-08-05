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
    <header className="sticky top-0 z-30 border-b border-emerald-100/80 bg-white/90 shadow-sm shadow-slate-200/50 backdrop-blur-xl">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
        <button
          type="button"
          onClick={() =>
            navigate("/customer/dashboard")
          }
          className="text-xl font-black text-emerald-700"
        >
          {title}
        </button>

        <nav className="flex items-center gap-3">
          <button
            type="button"
            onClick={() =>
              navigate("/")
            }
            className="food-button-secondary px-4 py-2 text-sm"
          >
            Home
          </button>

          <button
            type="button"
            onClick={() =>
              navigate("/customer/restaurants")
            }
            className="food-button-secondary px-4 py-2 text-sm"
          >
            Restaurants
          </button>

          <button
            type="button"
            onClick={() =>
              navigate("/customer/orders")
            }
            className="food-button-secondary px-4 py-2 text-sm"
          >
            Orders
          </button>

          <button
            type="button"
            onClick={() =>
              navigate("/customer/cart")
            }
            className="food-button-primary flex items-center gap-2 px-5 py-2 text-sm"
          >
            <span>Cart</span>

            <span className="flex min-h-6 min-w-6 items-center justify-center rounded-full bg-white px-1.5 text-xs font-bold text-emerald-700">
              {loading ? "…" : totalItems}
            </span>
          </button>
        </nav>
      </div>
    </header>
  );
}

export default CustomerHeader;
