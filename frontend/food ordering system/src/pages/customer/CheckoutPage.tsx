import {
  type FormEvent,
  useState,
} from "react";

import { useNavigate } from "react-router-dom";

import { useCart } from "../../context/CartContext";

function CheckoutPage() {
  const navigate = useNavigate();

  const { cart } = useCart();

  const [deliveryAddress, setDeliveryAddress] =
    useState("");

  const [error, setError] =
    useState("");

  const handleContinueToPayment = (
    event: FormEvent<HTMLFormElement>
  ) => {
    event.preventDefault();

    setError("");

    if (!cart || cart.items.length === 0) {
      setError("Your cart is empty.");
      return;
    }

    if (cart.hasPriceChanges) {
      setError(
        "Please accept the updated prices before checkout."
      );
      return;
    }

    if (cart.hasUnavailableItems) {
      setError(
        "Please remove unavailable items before checkout."
      );
      return;
    }

    if (!deliveryAddress.trim()) {
      setError(
        "Delivery address is required."
      );
      return;
    }

    sessionStorage.setItem(
      "checkoutDeliveryAddress",
      deliveryAddress.trim()
    );

    navigate("/customer/payment");
  };

  if (!cart) {
    return (
      <div className="min-h-screen bg-slate-100 p-8">
        <p className="text-center text-slate-500">
          Loading checkout...
        </p>
      </div>
    );
  }

  return (
    <main className="min-h-screen bg-slate-100 p-6 md:p-8">
      <div className="mx-auto max-w-3xl">

        <div className="mb-8">
          <h1 className="text-3xl font-bold text-slate-950">
            Checkout
          </h1>

          <p className="mt-2 text-slate-500">
            Confirm your delivery details before payment.
          </p>
        </div>

        {error && (
          <div className="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-red-700">
            {error}
          </div>
        )}

        <section className="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm">

          <div className="mb-6">
            <p className="text-sm text-slate-500">
              Items
            </p>

            <p className="text-xl font-semibold text-slate-900">
              {cart.totalItems}
            </p>
          </div>

          <div className="mb-6">
            <p className="text-sm text-slate-500">
              Total amount
            </p>

            <p className="text-2xl font-bold text-indigo-600">
              KES {cart.totalAmount}
            </p>
          </div>

          <form
            onSubmit={handleContinueToPayment}
          >
            <label className="block">

              <span className="text-sm font-medium text-slate-700">
                Delivery address
              </span>

              <textarea
                required
                rows={4}
                value={deliveryAddress}
                onChange={(event) =>
                  setDeliveryAddress(
                    event.target.value
                  )
                }
                placeholder="Enter your delivery address"
                className="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3 outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
              />

            </label>

            <div className="mt-6 flex gap-3">

              <button
                type="button"
                onClick={() =>
                  navigate(
                    "/customer/cart"
                  )
                }
                className="rounded-3xl border border-slate-300 px-5 py-3 text-sm font-semibold text-slate-700"
              >
                Back to cart
              </button>

              <button
                type="submit"
                className="flex-1 rounded-3xl bg-indigo-600 px-5 py-3 text-sm font-semibold text-white hover:bg-indigo-700"
              >
                Continue to payment
              </button>

            </div>
          </form>

        </section>
      </div>
    </main>
  );
}

export default CheckoutPage;