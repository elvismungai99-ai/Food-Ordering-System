import { useNavigate } from "react-router-dom";

import { useCart } from "../../context/CartContext";

interface CartSidebarProps {
  open: boolean;
  onClose: () => void;
}

function CartSidebar({
  open,
  onClose,
}: CartSidebarProps) {
  const navigate = useNavigate();

  const {
    cart,
    totalItems,
  } = useCart();

  if (!open) {
    return null;
  }

  return (
    <>
      <button
        type="button"
        aria-label="Close cart"
        onClick={onClose}
        className="fixed inset-0 z-40 bg-black/40"
      />

      <aside className="fixed right-0 top-0 z-50 flex h-full w-full max-w-md flex-col bg-white shadow-xl">
        <div className="flex items-center justify-between border-b border-slate-200 p-5">
          <h2 className="text-xl font-semibold text-slate-900">
            Cart ({totalItems})
          </h2>

          <button
            type="button"
            onClick={onClose}
            className="rounded-full px-3 py-2 text-slate-500 hover:bg-slate-100"
          >
            ✕
          </button>
        </div>

        <div className="flex-1 overflow-y-auto p-5">
          {!cart ||
          cart.items.length === 0 ? (
            <p className="text-center text-slate-500">
              Your cart is empty.
            </p>
          ) : (
            <div className="space-y-4">
              {cart.items.map((item) => (
                <div
                  key={item.id}
                  className="rounded-2xl border border-slate-200 p-4"
                >
                  <div className="flex justify-between gap-4">
                    <div>
                      <p className="font-semibold text-slate-900">
                        {item.name}
                      </p>

                      <p className="mt-1 text-sm text-slate-500">
                        Quantity:{" "}
                        {item.quantity}
                      </p>
                    </div>

                    <p className="font-semibold text-indigo-600">
                      KES{" "}
                      {item.subtotal.toFixed(
                        2
                      )}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="border-t border-slate-200 p-5">
          <button
            type="button"
            onClick={() => {
              onClose();
              navigate("/customer/cart");
            }}
            className="w-full rounded-3xl bg-indigo-600 px-5 py-3 text-sm font-semibold text-white"
          >
            View full cart
          </button>
        </div>
      </aside>
    </>
  );
}

export default CartSidebar;