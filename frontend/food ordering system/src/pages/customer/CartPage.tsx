import {
  useEffect,
  useState,
} from "react";

import { useNavigate } from "react-router-dom";

import CustomerHeader from "../../components/customer/CustomerHeader";
import { useCart } from "../../context/CartContext";

function CartPage() {
  const navigate = useNavigate();

  const {
    cart,
    loading,
    error,
    refreshCart,
    updateQuantity,
    removeItem,
  } = useCart();

  const [updatingItemId, setUpdatingItemId] =
    useState<string | null>(null);

  useEffect(() => {
    refreshCart();
  }, [refreshCart]);

  const handleQuantityChange = async (
    cartItemId: string,
    quantity: number
  ) => {
    try {
      setUpdatingItemId(cartItemId);

      await updateQuantity(
        cartItemId,
        quantity
      );
    } finally {
      setUpdatingItemId(null);
    }
  };

  const handleRemoveItem = async (
    cartItemId: string
  ) => {
    try {
      setUpdatingItemId(cartItemId);

      await removeItem(cartItemId);
    } finally {
      setUpdatingItemId(null);
    }
  };

  const formatPrice = (price: number) =>
    new Intl.NumberFormat("en-KE", {
      style: "currency",
      currency: "KES",
    }).format(price);

  return (
    <div className="min-h-screen bg-slate-100">
      <CustomerHeader />

      <main className="mx-auto max-w-6xl px-6 py-10">
        <div className="mb-8 flex flex-wrap items-center justify-between gap-4">
          <div>
            <h1 className="text-3xl font-bold text-slate-950">
              Your Cart
            </h1>

            <p className="mt-2 text-slate-500">
              Review and update your selected items.
            </p>
          </div>

          <button
            type="button"
            onClick={() =>
              navigate("/customer/restaurants")
            }
            className="rounded-3xl border border-slate-300 bg-white px-5 py-2 text-sm font-semibold text-slate-700"
          >
            Continue shopping
          </button>
        </div>

        {error && (
          <div className="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-red-700">
            {error}
          </div>
        )}

        {loading && !cart ? (
          <div className="py-20 text-center text-slate-500">
            Loading cart...
          </div>
        ) : !cart || cart.items.length === 0 ? (
          <section className="rounded-[24px] border border-slate-200 bg-white p-12 text-center">
            <div className="text-5xl">🛒</div>

            <h2 className="mt-5 text-xl font-semibold text-slate-900">
              Your cart is empty
            </h2>

            <p className="mt-2 text-slate-500">
              Browse restaurants and add some food.
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
        ) : (
          <div className="grid gap-8 lg:grid-cols-[1fr_320px]">
            <section className="space-y-4">
              {cart.items.map((item) => (
                <article
                  key={item.id}
                  className="flex flex-col gap-5 rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm sm:flex-row"
                >
                  {item.imageUrl ? (
                    <img
                      src={item.imageUrl}
                      alt={item.name}
                      className="h-32 w-full rounded-2xl object-cover sm:w-36"
                    />
                  ) : (
                    <div className="flex h-32 w-full items-center justify-center rounded-2xl bg-slate-100 text-slate-400 sm:w-36">
                      No image
                    </div>
                  )}

                  <div className="flex flex-1 flex-col justify-between">
                    <div>
                      <div className="flex items-start justify-between gap-4">
                        <div>
                          <h2 className="text-lg font-semibold text-slate-900">
                            {item.name}
                          </h2>

                          <p className="mt-1 text-sm text-slate-500">
                            {item.description ||
                              "No description available."}
                          </p>
                        </div>

                        <p className="font-semibold text-indigo-600">
                          {formatPrice(item.subtotal)}
                        </p>
                      </div>

                      <p className="mt-3 text-sm text-slate-500">
                        {formatPrice(item.unitPrice)} each
                      </p>
                    </div>

                    <div className="mt-5 flex flex-wrap items-center justify-between gap-4">
                      <div className="flex items-center rounded-3xl border border-slate-300">
                        <button
                          type="button"
                          disabled={
                            updatingItemId === item.id
                          }
                          onClick={() =>
                            handleQuantityChange(
                              item.id,
                              item.quantity - 1
                            )
                          }
                          className="px-4 py-2 text-lg disabled:opacity-50"
                        >
                          −
                        </button>

                        <span className="min-w-10 text-center font-semibold">
                          {item.quantity}
                        </span>

                        <button
                          type="button"
                          disabled={
                            updatingItemId === item.id
                          }
                          onClick={() =>
                            handleQuantityChange(
                              item.id,
                              item.quantity + 1
                            )
                          }
                          className="px-4 py-2 text-lg disabled:opacity-50"
                        >
                          +
                        </button>
                      </div>

                      <button
                        type="button"
                        disabled={
                          updatingItemId === item.id
                        }
                        onClick={() =>
                          handleRemoveItem(item.id)
                        }
                        className="text-sm font-semibold text-red-600 disabled:opacity-50"
                      >
                        Remove
                      </button>
                    </div>
                  </div>
                </article>
              ))}
            </section>

            <aside className="h-fit rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm">
              <h2 className="text-xl font-semibold text-slate-900">
                Order summary
              </h2>

              <div className="mt-6 space-y-4">
                <div className="flex justify-between text-sm text-slate-600">
                  <span>Total items</span>
                  <span>{cart.totalItems}</span>
                </div>

                <div className="border-t border-slate-200 pt-4">
                  <div className="flex justify-between text-lg font-semibold text-slate-950">
                    <span>Total</span>
                    <span>
                      {formatPrice(cart.totalAmount)}
                    </span>
                  </div>
                </div>
              </div>

              <button
                type="button"
                className="mt-6 w-full rounded-3xl bg-indigo-600 px-5 py-3 text-sm font-semibold text-white"
              >
                Proceed to checkout
              </button>
            </aside>
          </div>
        )}
      </main>
    </div>
  );
}

export default CartPage;